'use client'

import { useEffect, useState } from 'react'
import { cacheService } from '@/services/cacheService'

const CACHE_MAX_AGE_MS = 48 * 60 * 60 * 1000 // 48 hours

export interface OfflineStatus {
  isOnline: boolean
  cacheAge: number | null // milliseconds
  cacheExpired: boolean // cache > 48 hours old
}

/**
 * Hook to detect offline status and cache age
 */
export function useOfflineStatus(): OfflineStatus {
  const [isOnline, setIsOnline] = useState(true)
  const [cacheAge, setCacheAge] = useState<number | null>(null)
  const [cacheExpired, setCacheExpired] = useState(false)

  useEffect(() => {
    // Only run on client side
    if (typeof window === 'undefined') {
      return
    }

    // Set initial state
    setIsOnline(navigator.onLine)
    const activitiesCacheAge = cacheService.getAge('activities')
    setCacheAge(activitiesCacheAge)
    setCacheExpired(activitiesCacheAge ? activitiesCacheAge > CACHE_MAX_AGE_MS : false)

    // Event listeners
    const handleOnline = () => {
      setIsOnline(true)
    }

    const handleOffline = () => {
      setIsOnline(false)
    }

    // Listen for online/offline events
    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    // Check cache age periodically
    const cacheCheckInterval = setInterval(() => {
      const age = cacheService.getAge('activities')
      setCacheAge(age)
      setCacheExpired(age ? age > CACHE_MAX_AGE_MS : false)
    }, 60000) // Check every minute

    return () => {
      window.removeEventListener('online', handleOnline)
      window.removeEventListener('offline', handleOffline)
      clearInterval(cacheCheckInterval)
    }
  }, [])

  return { isOnline, cacheAge, cacheExpired }
}
