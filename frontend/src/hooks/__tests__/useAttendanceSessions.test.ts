import { renderHook, act, waitFor } from '@testing-library/react'
import { useAttendanceSessions } from '../useAttendanceSessions'
import apiClient from '@/services/apiClient'

jest.mock('@/services/apiClient')

describe('useAttendanceSessions', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should initialize with default state', () => {
    const { result } = renderHook(() => useAttendanceSessions())

    expect(result.current.session).toBeNull()
    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('should open attendance session successfully', async () => {
    const mockSession = {
      id: 'session-1',
      activityId: 'activity-1',
      date: '2026-04-13',
      students: [
        { id: 'enroll-1', studentName: 'Juan Pérez' },
        { id: 'enroll-2', studentName: 'María García' },
      ],
    }

    ;(apiClient.post as jest.Mock).mockResolvedValue({ data: mockSession })

    const { result } = renderHook(() => useAttendanceSessions())

    await act(async () => {
      await result.current.openSession('activity-1')
    })

    await waitFor(() => {
      expect(result.current.session).toEqual(mockSession)
      expect(result.current.loading).toBe(false)
    })

    expect(apiClient.post).toHaveBeenCalledWith('/api/attendance/sessions', {
      activityId: 'activity-1',
    })
  })

  it('should handle open session error', async () => {
    ;(apiClient.post as jest.Mock).mockRejectedValue({
      response: {
        status: 500,
        data: { message: 'Server error' },
      },
    })

    const { result } = renderHook(() => useAttendanceSessions())

    await act(async () => {
      try {
        await result.current.openSession('activity-1')
      } catch (err) {
        // Expected
      }
    })

    await waitFor(() => {
      expect(result.current.error).toBeTruthy()
      expect(result.current.session).toBeNull()
    })
  })

  it('should record attendance successfully', async () => {
    const mockSession = {
      id: 'session-1',
      activityId: 'activity-1',
      date: '2026-04-13',
      students: [
        { id: 'enroll-1', studentName: 'Juan Pérez', present: false },
        { id: 'enroll-2', studentName: 'María García', present: false },
      ],
    }

    ;(apiClient.post as jest.Mock)
      .mockResolvedValueOnce({ data: mockSession })
      .mockResolvedValueOnce({ data: { success: true } })

    const { result } = renderHook(() => useAttendanceSessions())

    // First, open session
    await act(async () => {
      await result.current.openSession('activity-1')
    })

    // Then record attendance
    await act(async () => {
      await result.current.recordAttendance('session-1', 'enroll-1', true, 'Llegó tarde')
    })

    await waitFor(() => {
      expect(apiClient.post).toHaveBeenCalledWith('/api/attendance/records', {
        sessionId: 'session-1',
        enrollmentId: 'enroll-1',
        present: true,
        observations: 'Llegó tarde',
      })
    })

    // Session should still be set
    expect(result.current.session).not.toBeNull()
  })

  it('should handle record attendance error', async () => {
    const mockSession = {
      id: 'session-1',
      activityId: 'activity-1',
      date: '2026-04-13',
      students: [{ id: 'enroll-1', studentName: 'Juan Pérez' }],
    }

    ;(apiClient.post as jest.Mock)
      .mockResolvedValueOnce({ data: mockSession })
      .mockRejectedValueOnce({
        response: {
          status: 400,
          data: { message: 'Invalid attendance record' },
        },
      })

    const { result } = renderHook(() => useAttendanceSessions())

    await act(async () => {
      await result.current.openSession('activity-1')
    })

    await act(async () => {
      try {
        await result.current.recordAttendance('session-1', 'enroll-1', true)
      } catch (err) {
        // Expected
      }
    })

    await waitFor(() => {
      expect(result.current.error).toBeTruthy()
    })
  })

  it('should handle record attendance without observations', async () => {
    const mockSession = {
      id: 'session-1',
      activityId: 'activity-1',
      date: '2026-04-13',
      students: [{ id: 'enroll-1', studentName: 'Juan Pérez' }],
    }

    ;(apiClient.post as jest.Mock)
      .mockResolvedValueOnce({ data: mockSession })
      .mockResolvedValueOnce({ data: { success: true } })

    const { result } = renderHook(() => useAttendanceSessions())

    await act(async () => {
      await result.current.openSession('activity-1')
    })

    await act(async () => {
      await result.current.recordAttendance('session-1', 'enroll-1', false)
    })

    expect(apiClient.post).toHaveBeenCalledWith('/api/attendance/records', {
      sessionId: 'session-1',
      enrollmentId: 'enroll-1',
      present: false,
    })
  })
})
