import { renderHook, act, waitFor } from '@testing-library/react'
import { useAdminActivities } from '../useAdminActivities'
import apiClient from '@/services/apiClient'

jest.mock('@/services/apiClient')

const mockActivity = {
  id: 'activity-1',
  name: 'Fútbol',
  description: 'Actividad de fútbol',
  totalSpots: 20,
  availableSpots: 10,
  status: 'DRAFT' as const,
  institutionId: 'inst-1',
  createdAt: new Date(),
  updatedAt: new Date(),
}

describe('useAdminActivities', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should initialize with empty state', () => {
    const { result } = renderHook(() => useAdminActivities())

    expect(result.current.activities).toEqual([])
    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('should fetch activities successfully', async () => {
    const activities = [mockActivity, { ...mockActivity, id: 'activity-2', name: 'Arte' }]
    ;(apiClient.get as jest.Mock).mockResolvedValue({ data: activities })

    const { result } = renderHook(() => useAdminActivities())

    await act(async () => {
      await result.current.fetchActivities()
    })

    await waitFor(() => {
      expect(result.current.activities).toEqual(activities)
      expect(result.current.loading).toBe(false)
    })

    expect(apiClient.get).toHaveBeenCalledWith('/api/activities')
  })

  it('should handle fetch activities error', async () => {
    ;(apiClient.get as jest.Mock).mockRejectedValue({
      response: { data: { message: 'Fetch failed' } },
    })

    const { result } = renderHook(() => useAdminActivities())

    await act(async () => {
      try {
        await result.current.fetchActivities()
      } catch (err) {
        // Expected
      }
    })

    await waitFor(() => {
      expect(result.current.error).toBeTruthy()
    })
  })

  it('should create activity successfully', async () => {
    ;(apiClient.post as jest.Mock).mockResolvedValue({ data: mockActivity })

    const { result } = renderHook(() => useAdminActivities())

    const input = {
      name: 'Fútbol',
      description: 'Actividad de fútbol',
      totalSpots: 20,
    }

    await act(async () => {
      await result.current.createActivity(input)
    })

    await waitFor(() => {
      expect(result.current.activities).toContainEqual(
        expect.objectContaining({
          name: 'Fútbol',
          availableSpots: 20, // Should equal totalSpots
        }),
      )
    })

    expect(apiClient.post).toHaveBeenCalledWith('/api/activities', expect.objectContaining(input))
  })

  it('should update activity successfully', async () => {
    const updatedActivity = { ...mockActivity, name: 'Fútbol Avanzado' }
    ;(apiClient.put as jest.Mock).mockResolvedValue({ data: updatedActivity })

    const { result } = renderHook(() => useAdminActivities())

    // Start with an activity
    await act(async () => {
      result.current.activities = [mockActivity]
    })

    const input = { name: 'Fútbol Avanzado', description: 'description', totalSpots: 20 }

    await act(async () => {
      await result.current.updateActivity('activity-1', input)
    })

    expect(apiClient.put).toHaveBeenCalledWith('/api/activities/activity-1', expect.objectContaining(input))
  })

  it('should publish activity successfully', async () => {
    const publishedActivity = { ...mockActivity, status: 'PUBLISHED' as const }
    ;(apiClient.put as jest.Mock).mockResolvedValue({ data: publishedActivity })

    const { result } = renderHook(() => useAdminActivities())

    await act(async () => {
      result.current.activities = [mockActivity]
    })

    await act(async () => {
      await result.current.publishActivity('activity-1')
    })

    expect(apiClient.put).toHaveBeenCalledWith('/api/activities/activity-1', {
      status: 'PUBLISHED',
    })
  })

  it('should disable activity successfully', async () => {
    const disabledActivity = { ...mockActivity, status: 'DISABLED' as const }
    ;(apiClient.put as jest.Mock).mockResolvedValue({ data: disabledActivity })

    const { result } = renderHook(() => useAdminActivities())

    await act(async () => {
      result.current.activities = [mockActivity]
    })

    await act(async () => {
      await result.current.disableActivity('activity-1')
    })

    expect(apiClient.put).toHaveBeenCalledWith('/api/activities/activity-1', {
      status: 'DISABLED',
    })
  })

  it('should delete activity successfully', async () => {
    ;(apiClient.delete as jest.Mock).mockResolvedValue({ data: { success: true } })

    const { result } = renderHook(() => useAdminActivities())

    await act(async () => {
      result.current.activities = [mockActivity, { ...mockActivity, id: 'activity-2' }]
    })

    expect(result.current.activities.length).toBe(2)

    await act(async () => {
      await result.current.deleteActivity('activity-1')
    })

    await waitFor(() => {
      expect(result.current.activities).toEqual([expect.objectContaining({ id: 'activity-2' })])
    })

    expect(apiClient.delete).toHaveBeenCalledWith('/api/activities/activity-1')
  })

  it('should handle create activity error', async () => {
    ;(apiClient.post as jest.Mock).mockRejectedValue({
      response: { data: { message: 'Validation failed' } },
    })

    const { result } = renderHook(() => useAdminActivities())

    const input = { name: '', description: '', totalSpots: 0 }

    await act(async () => {
      try {
        await result.current.createActivity(input)
      } catch (err) {
        // Expected
      }
    })

    await waitFor(() => {
      expect(result.current.error).toBeTruthy()
    })
  })
})
