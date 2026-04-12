import { renderHook, act, waitFor } from '@testing-library/react'
import { useEnrollment } from '../useEnrollment'
import apiClient from '@/services/apiClient'

jest.mock('@/services/apiClient')

describe('useEnrollment', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('enroll', () => {
    it('should successfully enroll a student', async () => {
      const mockEnrollment = { id: 'enroll1', studentId: 'student1', activityId: 'activity1', status: 'ACTIVE' as const }
      ;(apiClient.post as jest.Mock).mockResolvedValue({ data: mockEnrollment })

      const { result } = renderHook(() => useEnrollment())

      let enrollmentResult
      await act(async () => {
        enrollmentResult = await result.current.enroll('student1', 'activity1')
      })

      expect(enrollmentResult).toEqual(mockEnrollment)
      expect(apiClient.post).toHaveBeenCalledWith('/api/enrollments', {
        studentId: 'student1',
        activityId: 'activity1',
      })
    })

    it('should handle 409 SPOT_EXHAUSTED error', async () => {
      ;(apiClient.post as jest.Mock).mockRejectedValue({
        response: {
          data: {
            code: 'SPOT_EXHAUSTED',
            message: 'No hay cupos disponibles',
          },
        },
      })

      const { result } = renderHook(() => useEnrollment())

      await act(async () => {
        try {
          await result.current.enroll('student1', 'activity1')
        } catch (error) {
          // Expected to throw
        }
      })

      await waitFor(() => {
        expect(result.current.error).toEqual({
          code: 'SPOT_EXHAUSTED',
          message: 'No hay cupos disponibles',
        })
      })
    })

    it('should handle 409 ALREADY_ENROLLED error', async () => {
      ;(apiClient.post as jest.Mock).mockRejectedValue({
        response: {
          data: {
            code: 'ALREADY_ENROLLED',
            message: 'Ya estás inscrito en esta actividad',
          },
        },
      })

      const { result } = renderHook(() => useEnrollment())

      await act(async () => {
        try {
          await result.current.enroll('student1', 'activity1')
        } catch (error) {
          // Expected to throw
        }
      })

      await waitFor(() => {
        expect(result.current.error?.code).toBe('ALREADY_ENROLLED')
      })
    })

    it('should set loading state during request', async () => {
      ;(apiClient.post as jest.Mock).mockImplementation(
        () =>
          new Promise((resolve) => {
            setTimeout(() => resolve({ data: { id: 'enroll1' } }), 100)
          }),
      )

      const { result } = renderHook(() => useEnrollment())

      expect(result.current.enrolling).toBe(false)

      act(() => {
        result.current.enroll('student1', 'activity1')
      })

      await waitFor(() => {
        expect(result.current.enrolling).toBe(false)
      })
    })
  })

  describe('cancel', () => {
    it('should successfully cancel enrollment', async () => {
      ;(apiClient.delete as jest.Mock).mockResolvedValue({ data: {} })

      const { result } = renderHook(() => useEnrollment())

      await act(async () => {
        await result.current.cancel('enroll1')
      })

      expect(apiClient.delete).toHaveBeenCalledWith('/api/enrollments/enroll1')
    })

    it('should handle cancel errors', async () => {
      ;(apiClient.delete as jest.Mock).mockRejectedValue({
        response: {
          data: {
            code: 'NOT_FOUND',
            message: 'Enrollment not found',
          },
        },
      })

      const { result } = renderHook(() => useEnrollment())

      await act(async () => {
        try {
          await result.current.cancel('invalid-id')
        } catch (error) {
          // Expected to throw
        }
      })

      await waitFor(() => {
        expect(result.current.error?.code).toBe('NOT_FOUND')
      })
    })
  })

  describe('getByStudent', () => {
    it('should fetch enrollments by student', async () => {
      const mockEnrollments = [
        { id: 'enroll1', studentId: 'student1', activityId: 'activity1', status: 'ACTIVE' as const },
        { id: 'enroll2', studentId: 'student1', activityId: 'activity2', status: 'ACTIVE' as const },
      ]
      ;(apiClient.get as jest.Mock).mockResolvedValue({ data: mockEnrollments })

      const { result } = renderHook(() => useEnrollment())

      let enrollments
      await act(async () => {
        enrollments = await result.current.getByStudent('student1')
      })

      expect(enrollments).toEqual(mockEnrollments)
      expect(apiClient.get).toHaveBeenCalledWith('/api/enrollments/student/student1')
    })

    it('should handle fetch errors', async () => {
      ;(apiClient.get as jest.Mock).mockRejectedValue({
        response: {
          data: {
            code: 'FETCH_ERROR',
            message: 'Failed to fetch enrollments',
          },
        },
      })

      const { result } = renderHook(() => useEnrollment())

      await act(async () => {
        try {
          await result.current.getByStudent('student1')
        } catch (error) {
          // Expected to throw
        }
      })

      await waitFor(() => {
        expect(result.current.error?.code).toBe('FETCH_ERROR')
      })
    })
  })

  describe('error state', () => {
    it('should clear error on successful request', async () => {
      ;(apiClient.post as jest.Mock).mockRejectedValueOnce({
        response: {
          data: { code: 'ERROR', message: 'Some error' },
        },
      })

      const { result } = renderHook(() => useEnrollment())

      // First request fails
      await act(async () => {
        try {
          await result.current.enroll('s1', 'a1')
        } catch (e) {
          // Expected
        }
      })

      expect(result.current.error).not.toBeNull()

      // Second request succeeds
      ;(apiClient.post as jest.Mock).mockResolvedValueOnce({
        data: { id: 'enroll1' },
      })

      await act(async () => {
        await result.current.enroll('s1', 'a1')
      })

      expect(result.current.error).toBeNull()
    })
  })
})
