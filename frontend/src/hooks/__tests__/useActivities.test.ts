import { renderHook, act, waitFor } from '@testing-library/react'
import { useActivities } from '../useActivities'
import apiClient from '@/services/apiClient'
import { cacheService } from '@/services/cacheService'

jest.mock('@/services/apiClient')
jest.mock('@/services/cacheService')

// Mock navigator.onLine
Object.defineProperty(window.navigator, 'onLine', {
  writable: true,
  value: true,
})

describe('useActivities', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    ;(window.navigator as any).onLine = true
    ;(cacheService.get as jest.Mock).mockReturnValue(null)
    ;(cacheService.set as jest.Mock).mockImplementation(() => {})
  })

  it('should fetch activities from API on mount when online', async () => {
    const mockActivities = [
      {
        id: '1',
        name: 'Football',
        description: 'Football training',
        totalSpots: 20,
        availableSpots: 15,
        status: 'PUBLISHED',
        institutionId: 'inst-1',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    ]

    ;(apiClient.get as jest.Mock).mockResolvedValue({ data: mockActivities })

    const { result } = renderHook(() => useActivities())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.activities).toHaveLength(1)
    expect(result.current.activities[0].name).toBe('Football')
    expect(result.current.fromCache).toBe(false)
    expect(cacheService.set).toHaveBeenCalledWith(
      'activities',
      mockActivities,
      expect.any(Number)
    )
  })

  it('should refetch activities when refetch is called', async () => {
    const mockActivities = [
      {
        id: '1',
        name: 'Football',
        description: 'Football training',
        totalSpots: 20,
        availableSpots: 15,
        status: 'PUBLISHED',
        institutionId: 'inst-1',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    ]

    ;(apiClient.get as jest.Mock).mockResolvedValue({ data: mockActivities })

    const { result } = renderHook(() => useActivities())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    await act(async () => {
      await result.current.refetch()
    })

    expect(apiClient.get).toHaveBeenCalledWith('/api/activities', {
      params: { status: 'PUBLISHED' },
    })
  })

  it('should use cache when offline on mount', async () => {
    ;(window.navigator as any).onLine = false
    const cachedActivities = [
      {
        id: '1',
        name: 'Art Class',
        description: 'Art training',
        totalSpots: 10,
        availableSpots: 5,
        status: 'PUBLISHED',
        institutionId: 'inst-1',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    ]

    ;(cacheService.get as jest.Mock).mockReturnValue(cachedActivities)

    const { result } = renderHook(() => useActivities())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.activities).toHaveLength(1)
    expect(result.current.activities[0].name).toBe('Art Class')
    expect(result.current.fromCache).toBe(true)
  })

  it('should fallback to cache on API error', async () => {
    const cachedActivities = [
      {
        id: '2',
        name: 'Music Class',
        description: 'Music training',
        totalSpots: 15,
        availableSpots: 10,
        status: 'PUBLISHED',
        institutionId: 'inst-1',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    ]

    ;(apiClient.get as jest.Mock).mockRejectedValue({
      response: { status: 500, data: { message: 'Server error' } },
    })
    ;(cacheService.get as jest.Mock).mockReturnValue(cachedActivities)

    const { result } = renderHook(() => useActivities())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.activities).toHaveLength(1)
    expect(result.current.fromCache).toBe(true)
    expect(result.current.error).toBeNull()
  })

  it('should set error when API fails and no cache available', async () => {
    ;(apiClient.get as jest.Mock).mockRejectedValue({
      response: { status: 500, data: { message: 'Server error' } },
    })
    ;(cacheService.get as jest.Mock).mockReturnValue(null)

    const { result } = renderHook(() => useActivities())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.error).toBeTruthy()
    expect(result.current.activities).toHaveLength(0)
    expect(result.current.fromCache).toBe(false)
  })

  it('should set error when offline and no cache available', async () => {
    ;(window.navigator as any).onLine = false
    ;(cacheService.get as jest.Mock).mockReturnValue(null)

    const { result } = renderHook(() => useActivities())

    expect(result.current.error).toBeTruthy()
    expect(result.current.loading).toBe(false)
    expect(result.current.activities).toEqual([])
  })

  it('should handle multiple activities', async () => {
    const mockActivities = [
      {
        id: '1',
        name: 'Football',
        description: 'Football training',
        totalSpots: 20,
        availableSpots: 15,
        status: 'PUBLISHED',
        institutionId: 'inst-1',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
      {
        id: '2',
        name: 'Basketball',
        description: 'Basketball training',
        totalSpots: 15,
        availableSpots: 8,
        status: 'PUBLISHED',
        institutionId: 'inst-1',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    ]

    ;(apiClient.get as jest.Mock).mockResolvedValue({ data: mockActivities })

    const { result } = renderHook(() => useActivities())

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.activities).toHaveLength(2)
    expect(result.current.activities.map((a) => a.name)).toEqual(['Football', 'Basketball'])
  })
})
