/**
 * Simple cache service with TTL support
 * Uses localStorage for persistence
 */

const CACHE_PREFIX = 'eams_cache_'
const DEFAULT_TTL_MS = 48 * 60 * 60 * 1000 // 48 hours

interface CachedData<T> {
  data: T
  timestamp: number // when it was cached
  ttl: number // time to live in ms
}

export const cacheService = {
  /**
   * Set a value in cache with optional TTL
   */
  set<T>(key: string, data: T, ttlMs: number = DEFAULT_TTL_MS): void {
    if (typeof window === 'undefined') return

    const cacheKey = `${CACHE_PREFIX}${key}`
    const cachedData: CachedData<T> = {
      data,
      timestamp: Date.now(),
      ttl: ttlMs,
    }

    try {
      localStorage.setItem(cacheKey, JSON.stringify(cachedData))
    } catch (error) {
      console.warn(`Failed to cache ${key}:`, error)
    }
  },

  /**
   * Get a value from cache if it hasn't expired
   */
  get<T>(key: string): T | null {
    if (typeof window === 'undefined') return null

    const cacheKey = `${CACHE_PREFIX}${key}`

    try {
      const cached = localStorage.getItem(cacheKey)
      if (!cached) return null

      const parsed: CachedData<T> = JSON.parse(cached)
      const age = Date.now() - parsed.timestamp

      // Check if expired
      if (age > parsed.ttl) {
        localStorage.removeItem(cacheKey)
        return null
      }

      return parsed.data
    } catch (error) {
      console.warn(`Failed to retrieve cache for ${key}:`, error)
      return null
    }
  },

  /**
   * Check if a key exists and is not expired
   */
  has(key: string): boolean {
    return this.get(key) !== null
  },

  /**
   * Get the age of cached data in milliseconds
   */
  getAge(key: string): number | null {
    if (typeof window === 'undefined') return null

    const cacheKey = `${CACHE_PREFIX}${key}`

    try {
      const cached = localStorage.getItem(cacheKey)
      if (!cached) return null

      const parsed: CachedData<any> = JSON.parse(cached)
      return Date.now() - parsed.timestamp
    } catch (error) {
      return null
    }
  },

  /**
   * Check if cache is older than max age
   */
  isExpired(key: string, maxAgeMs: number = DEFAULT_TTL_MS): boolean {
    const age = this.getAge(key)
    if (age === null) return true
    return age > maxAgeMs
  },

  /**
   * Remove a specific key from cache
   */
  remove(key: string): void {
    if (typeof window === 'undefined') return

    const cacheKey = `${CACHE_PREFIX}${key}`
    try {
      localStorage.removeItem(cacheKey)
    } catch (error) {
      console.warn(`Failed to remove cache for ${key}:`, error)
    }
  },

  /**
   * Clear all cached data
   */
  clear(): void {
    if (typeof window === 'undefined') return

    try {
      const keys = Object.keys(localStorage)
      keys.forEach((key) => {
        if (key.startsWith(CACHE_PREFIX)) {
          localStorage.removeItem(key)
        }
      })
    } catch (error) {
      console.warn('Failed to clear cache:', error)
    }
  },
}
