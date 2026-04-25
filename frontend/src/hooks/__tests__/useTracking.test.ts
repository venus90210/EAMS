import { renderHook, waitFor } from '@testing-library/react'
import { useTracking } from '../useTracking'
import { useAuth } from '../useAuth'
import apiClient from '@/services/apiClient'

jest.mock('@/hooks/useAuth')
jest.mock('@/services/apiClient')

const mockEnrollments = [
  {
    id: 'enroll-1',
    studentId: 'student-1',
    studentName: 'Juan Pérez',
    activityId: 'activity-1',
    activityName: 'Fútbol',
    status: 'ACTIVE' as const,
    enrolledAt: '2026-04-01',
  },
  {
    id: 'enroll-2',
    studentId: 'student-1',
    studentName: 'Juan Pérez',
    activityId: 'activity-2',
    activityName: 'Artes',
    status: 'ACTIVE' as const,
    enrolledAt: '2026-04-02',
  },
]

const mockAttendance = [
  {
    id: 'att-1',
    enrollmentId: 'enroll-1',
    date: '2026-04-10',
    present: true,
  },
  {
    id: 'att-2',
    enrollmentId: 'enroll-1',
    date: '2026-04-12',
    present: false,
  },
  {
    id: 'att-3',
    enrollmentId: 'enroll-2',
    date: '2026-04-11',
    present: true,
  },
]

describe('useTracking', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should initialize with empty data when no user', () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: null,
      isAuthenticated: false,
    })

    const { result } = renderHook(() => useTracking())

    expect(result.current.data).toEqual([])
    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('should fetch and group tracking data by student', async () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: { id: 'guardian-1', name: 'Ana Martinez', role: 'GUARDIAN' },
      isAuthenticated: true,
    })

    ;(apiClient.get as jest.Mock)
      .mockResolvedValueOnce({ data: mockEnrollments })
      .mockResolvedValueOnce({ data: mockAttendance })

    const { result } = renderHook(() => useTracking())

    await waitFor(() => {
      expect(result.current.data.length).toBe(1) // Only one unique student
    })

    const studentData = result.current.data[0]
    expect(studentData.studentId).toBe('student-1')
    expect(studentData.studentName).toBe('Juan Pérez')
    expect(studentData.enrollments.length).toBe(2) // Two enrollments
    expect(studentData.attendance.length).toBe(3) // Three attendance records
  })

  it('should correctly match attendance to enrollments by studentId', async () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: { id: 'guardian-1', name: 'Ana Martinez', role: 'GUARDIAN' },
      isAuthenticated: true,
    })

    ;(apiClient.get as jest.Mock)
      .mockResolvedValueOnce({ data: mockEnrollments })
      .mockResolvedValueOnce({ data: mockAttendance })

    const { result } = renderHook(() => useTracking())

    await waitFor(() => {
      expect(result.current.data.length).toBe(1)
    })

    const studentData = result.current.data[0]
    // Attendance should be grouped under this student
    expect(studentData.attendance.every((a) =>
      mockEnrollments
        .filter((e) => e.studentId === studentData.studentId)
        .some((e) => e.id === a.enrollmentId)
    )).toBe(true)
  })

  it('should handle fetch enrollments error', async () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: { id: 'guardian-1', name: 'Ana Martinez', role: 'GUARDIAN' },
      isAuthenticated: true,
    })

    ;(apiClient.get as jest.Mock).mockRejectedValueOnce({
      response: { data: { message: 'Unauthorized' } },
    })

    const { result } = renderHook(() => useTracking())

    await waitFor(() => {
      expect(result.current.error).toBeTruthy()
      expect(result.current.data).toEqual([])
    })
  })

  it('should not fetch when user is null', () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: null,
      isAuthenticated: false,
    })

    renderHook(() => useTracking())

    // Should not call apiClient
    expect(apiClient.get).not.toHaveBeenCalled()
  })

  it('should handle multiple students with grouped data', async () => {
    const multiStudentEnrollments = [
      ...mockEnrollments,
      {
        id: 'enroll-3',
        studentId: 'student-2',
        studentName: 'María García',
        activityId: 'activity-1',
        activityName: 'Fútbol',
        status: 'ACTIVE' as const,
        enrolledAt: '2026-04-03',
      },
    ]

    ;(useAuth as jest.Mock).mockReturnValue({
      user: { id: 'guardian-1', name: 'Ana Martinez', role: 'GUARDIAN' },
      isAuthenticated: true,
    })

    ;(apiClient.get as jest.Mock)
      .mockResolvedValueOnce({ data: multiStudentEnrollments })
      .mockResolvedValueOnce({ data: mockAttendance })

    const { result } = renderHook(() => useTracking())

    await waitFor(() => {
      expect(result.current.data.length).toBe(2) // Two unique students
    })

    const student1 = result.current.data.find((d) => d.studentId === 'student-1')
    const student2 = result.current.data.find((d) => d.studentId === 'student-2')

    expect(student1).toBeDefined()
    expect(student2).toBeDefined()
    expect(student1?.enrollments.length).toBe(2)
    expect(student2?.enrollments.length).toBe(1)
  })

  it('should handle empty attendance list', async () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: { id: 'guardian-1', name: 'Ana Martinez', role: 'GUARDIAN' },
      isAuthenticated: true,
    })

    ;(apiClient.get as jest.Mock)
      .mockResolvedValueOnce({ data: mockEnrollments })
      .mockResolvedValueOnce({ data: [] })

    const { result } = renderHook(() => useTracking())

    await waitFor(() => {
      expect(result.current.data.length).toBe(1)
    })

    const studentData = result.current.data[0]
    expect(studentData.attendance).toEqual([])
  })

  it('should call correct endpoints for data', async () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: { id: 'guardian-1', name: 'Ana Martinez', role: 'GUARDIAN' },
      isAuthenticated: true,
    })

    ;(apiClient.get as jest.Mock)
      .mockResolvedValueOnce({ data: mockEnrollments })
      .mockResolvedValueOnce({ data: mockAttendance })

    renderHook(() => useTracking())

    await waitFor(() => {
      expect(apiClient.get).toHaveBeenCalledWith('/api/enrollments/guardian/guardian-1')
      expect(apiClient.get).toHaveBeenCalledWith('/api/attendance/guardians/guardian-1')
    })
  })
})
