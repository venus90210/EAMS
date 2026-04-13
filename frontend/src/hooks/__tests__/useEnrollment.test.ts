import { renderHook, act, waitFor } from '@testing-library/react'
import { useEnrollment } from '../useEnrollment'
import apiClient from '@/services/apiClient'

jest.mock('@/services/apiClient')

describe('useEnrollment', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should enroll successfully', async () => {
    const mockEnrollment = {
      id: 'enrollment-1',
      studentId: 'student-1',
      activityId: 'activity-1',
      status: 'ACTIVE',
    }

    ;(apiClient.post as jest.Mock).mockResolvedValue({ data: mockEnrollment })

    const { result } = renderHook(() => useEnrollment())

    await act(async () => {
      await result.current.enroll('student-1', 'activity-1')
    })

    expect(apiClient.post).toHaveBeenCalledWith('/api/enrollments', {
      studentId: 'student-1',
      activityId: 'activity-1',
    })
    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('should handle spot exhausted error (409)', async () => {
    ;(apiClient.post as jest.Mock).mockRejectedValue({
      response: {
        status: 409,
        data: {
          code: 'SPOT_EXHAUSTED',
          message: 'No spots available',
        },
      },
    })

    const { result } = renderHook(() => useEnrollment())

    await act(async () => {
      try {
        await result.current.enroll('student-1', 'activity-1')
      } catch (err) {
        // Expected error
      }
    })

    await waitFor(() => {
      expect(result.current.error).toBeTruthy()
    })

    expect(result.current.error?.code).toBe('SPOT_EXHAUSTED')
    expect(result.current.error?.message).toBe('Cupos agotados en esta actividad')
  })

  it('should handle already enrolled error (409)', async () => {
    ;(apiClient.post as jest.Mock).mockRejectedValue({
      response: {
        status: 409,
        data: {
          code: 'ALREADY_ENROLLED',
          message: 'Already enrolled',
        },
      },
    })

    const { result } = renderHook(() => useEnrollment())

    await act(async () => {
      try {
        await result.current.enroll('student-1', 'activity-1')
      } catch (err) {
        // Expected error
      }
    })

    await waitFor(() => {
      expect(result.current.error?.code).toBe('ALREADY_ENROLLED')
    })

    expect(result.current.error?.message).toBe('Ya estás inscrito en esta actividad')
  })

  it('should handle active enrollment exists error (409)', async () => {
    ;(apiClient.post as jest.Mock).mockRejectedValue({
      response: {
        status: 409,
        data: {
          code: 'ACTIVE_ENROLLMENT_EXISTS',
          message: 'Active enrollment exists',
        },
      },
    })

    const { result } = renderHook(() => useEnrollment())

    await act(async () => {
      try {
        await result.current.enroll('student-1', 'activity-1')
      } catch (err) {
        // Expected error
      }
    })

    await waitFor(() => {
      expect(result.current.error?.code).toBe('ACTIVE_ENROLLMENT_EXISTS')
    })

    expect(result.current.error?.message).toBe(
      'Ya tienes una inscripción activa en esta actividad'
    )
  })

  it('should initialize with default state', () => {
    const { result } = renderHook(() => useEnrollment())

    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('should clear error on successful request after failure', async () => {
    // First call fails
    ;(apiClient.post as jest.Mock).mockRejectedValueOnce({
      response: {
        status: 409,
        data: {
          code: 'SPOT_EXHAUSTED',
          message: 'No spots',
        },
      },
    })

    const { result } = renderHook(() => useEnrollment())

    await act(async () => {
      try {
        await result.current.enroll('student-1', 'activity-1')
      } catch (err) {
        // Expected
      }
    })

    expect(result.current.error).not.toBeNull()

    // Second call succeeds
    ;(apiClient.post as jest.Mock).mockResolvedValueOnce({
      data: { id: 'enrollment-1' },
    })

    await act(async () => {
      await result.current.enroll('student-1', 'activity-2')
    })

    expect(result.current.error).toBeNull()
  })
})
