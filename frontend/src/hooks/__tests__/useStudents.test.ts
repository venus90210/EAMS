import { renderHook, waitFor } from '@testing-library/react'
import { useStudents } from '../useStudents'
import { useAuth } from '../useAuth'
import apiClient from '@/services/apiClient'

jest.mock('@/hooks/useAuth')
jest.mock('@/services/apiClient')

const mockStudents = [
  {
    id: 'student-1',
    firstName: 'Juan',
    lastName: 'Pérez',
    grade: '8°',
  },
  {
    id: 'student-2',
    firstName: 'María',
    lastName: 'García',
    grade: '9°',
  },
]

describe('useStudents', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should initialize with empty state when no user', () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: null,
      isAuthenticated: false,
    })

    const { result } = renderHook(() => useStudents())

    expect(result.current.students).toEqual([])
    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('should fetch students when user is available', async () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: { id: 'guardian-1', name: 'Ana Martinez', role: 'GUARDIAN' },
      isAuthenticated: true,
    })

    ;(apiClient.get as jest.Mock).mockResolvedValue({ data: mockStudents })

    const { result } = renderHook(() => useStudents())

    await waitFor(() => {
      expect(result.current.students).toEqual(mockStudents)
      expect(result.current.loading).toBe(false)
    })

    expect(apiClient.get).toHaveBeenCalledWith('/api/users/guardians/guardian-1/students')
  })

  it('should handle fetch students error', async () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: { id: 'guardian-1', name: 'Ana Martinez', role: 'GUARDIAN' },
      isAuthenticated: true,
    })

    ;(apiClient.get as jest.Mock).mockRejectedValue({
      response: { data: { message: 'Unauthorized' } },
    })

    const { result } = renderHook(() => useStudents())

    await waitFor(() => {
      expect(result.current.error).toBeTruthy()
      expect(result.current.students).toEqual([])
    })
  })

  it('should not fetch when user is null', async () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: null,
      isAuthenticated: false,
    })

    renderHook(() => useStudents())

    // Should not call apiClient
    expect(apiClient.get).not.toHaveBeenCalled()
  })

  it('should set loading state during fetch', async () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: { id: 'guardian-1', name: 'Ana Martinez', role: 'GUARDIAN' },
      isAuthenticated: true,
    })

    ;(apiClient.get as jest.Mock).mockImplementation(
      () =>
        new Promise((resolve) =>
          setTimeout(() => resolve({ data: mockStudents }), 50),
        ),
    )

    const { result } = renderHook(() => useStudents())

    // Loading should eventually be false
    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })
  })

  it('should return multiple students', async () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: { id: 'guardian-1', name: 'Ana Martinez', role: 'GUARDIAN' },
      isAuthenticated: true,
    })

    const threeStudents = [...mockStudents, { id: 'student-3', firstName: 'Carlos', lastName: 'López', grade: '10°' }]
    ;(apiClient.get as jest.Mock).mockResolvedValue({ data: threeStudents })

    const { result } = renderHook(() => useStudents())

    await waitFor(() => {
      expect(result.current.students.length).toBe(3)
    })
  })

  it('should handle empty students list', async () => {
    ;(useAuth as jest.Mock).mockReturnValue({
      user: { id: 'guardian-1', name: 'Ana Martinez', role: 'GUARDIAN' },
      isAuthenticated: true,
    })

    ;(apiClient.get as jest.Mock).mockResolvedValue({ data: [] })

    const { result } = renderHook(() => useStudents())

    await waitFor(() => {
      expect(result.current.students).toEqual([])
    })
  })
})
