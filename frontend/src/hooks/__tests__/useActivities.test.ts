import { renderHook, act, waitFor } from '@testing-library/react'
import { useActivities } from '../useActivities'
import apiClient from '@/services/apiClient'
import { cacheService } from '@/services/cacheService'

jest.mock('@/services/apiClient')
jest.mock('@/services/cacheService')

describe('useActivities', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should fetch activities from API', async () => {
    const mockActivities = [
      {
        id: '1',
        name: 'Football',
        description: 'Football training',
        totalSpots: 20,
        availableSpots: 15,
        status: 'PUBLISHED',
      },
    ]

    ;(apiClient.get as jest.Mock).mockResolvedValue({ data: mockActivities })
    ;(cacheService.set as jest.Mock).mockImplementation(() => {})

    const { result } = renderHook(() => useActivities())

    await act(async () => {
      await result.current.fetchActivities()
    })

    await waitFor(() => {
      expect(result.current.activities).toHaveLength(1)
    })

    expect(result.current.activities[0].name).toBe('Football')
    expect(cacheService.set).toHaveBeenCalledWith('activities', mockActivities, expect.any(Number))
  })

  it('should fetch activities from cache when offline', async () => {
    const cachedActivities = [
      {
        id: '1',
        name: 'Art Class',
        description: 'Art training',
        totalSpots: 10,
        availableSpots: 5,
        status: 'PUBLISHED',
      },
    ]

    ;(apiClient.get as jest.Mock).mockRejectedValue({
      response: { status: 0 }, // Offline error
    })
    ;(cacheService.get as jest.Mock).mockReturnValue(cachedActivities)

    const { result } = renderHook(() => useActivities())

    await act(async () => {
      await result.current.fetchActivities()
    })

    await waitFor(() => {
      expect(result.current.activities).toHaveLength(1)
    })

    expect(result.current.activities[0].name).toBe('Art Class')
  })

  it('should handle API errors gracefully', async () => {
    const errorMessage = 'Failed to fetch activities'

    ;(apiClient.get as jest.Mock).mockRejectedValue({
      response: {
        status: 500,
        data: { message: errorMessage },
      },
    })
    ;(cacheService.get as jest.Mock).mockReturnValue(null)

    const { result } = renderHook(() => useActivities())

    await act(async () => {
      try {
        await result.current.fetchActivities()
      } catch (err) {
        // Expected error
      }
    })

    await waitFor(() => {
      expect(result.current.error).toBeTruthy()
    })
  })

  it('should initialize with empty activities array', () => {
    const { result } = renderHook(() => useActivities())

    expect(result.current.activities).toEqual([])
    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
  })
})
