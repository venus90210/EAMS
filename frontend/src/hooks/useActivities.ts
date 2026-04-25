'use client'

import { useEffect, useState } from 'react'
import { Activity } from '@/types'
import apiClient from '@/services/apiClient'
import { cacheService } from '@/services/cacheService'

export interface UseActivitiesResult {
  activities: Activity[]
  loading: boolean
  error: string | null
  refetch: () => Promise<void>
  fromCache: boolean
}

/**
 * Hook to fetch and cache activities
 * Supports offline mode by serving from cache
 */
export function useActivities(): UseActivitiesResult {
  const [activities, setActivities] = useState<Activity[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [fromCache, setFromCache] = useState(false)

  const fetchActivities = async () => {
    try {
      setLoading(true)
      setError(null)
      setFromCache(false)

      const response = await apiClient.get<Activity[]>('/api/activities', {
        params: { status: 'PUBLISHED' },
      })

      setActivities(response.data)
      // Cache for 48 hours
      cacheService.set('activities', response.data, 48 * 60 * 60 * 1000)
    } catch (err: any) {
      // Try to get from cache on error
      const cached = cacheService.get<Activity[]>('activities')
      if (cached) {
        setActivities(cached)
        setFromCache(true)
      } else {
        setError(err.response?.data?.message || 'Failed to fetch activities')
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // Try online first
    if (navigator.onLine) {
      fetchActivities()
    } else {
      // Offline - use cache
      const cached = cacheService.get<Activity[]>('activities')
      if (cached) {
        setActivities(cached)
        setFromCache(true)
        setLoading(false)
      } else {
        setError('No cached activities available offline')
        setLoading(false)
      }
    }
  }, [])

  return { activities, loading, error, refetch: fetchActivities, fromCache }
}
