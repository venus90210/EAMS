import { cacheService } from '../cacheService'

describe('cacheService', () => {
  beforeEach(() => {
    localStorage.clear()
    jest.clearAllTimers()
  })

  describe('set and get', () => {
    it('should store and retrieve data', () => {
      const data = { id: '1', name: 'Test Activity' }
      cacheService.set('activities', data)

      expect(cacheService.get('activities')).toEqual(data)
    })

    it('should store with custom TTL', () => {
      const data = { value: 'test' }
      cacheService.set('key', data, 60000) // 1 minute

      const cached = cacheService.get('key')
      expect(cached).toEqual(data)
    })

    it('should return null for non-existent keys', () => {
      expect(cacheService.get('nonexistent')).toBeNull()
    })
  })

  describe('expiration', () => {
    it('should return null for expired data', () => {
      jest.useFakeTimers()
      const data = { id: '1' }
      cacheService.set('test', data, 1000) // 1 second

      // Move time forward past expiration
      jest.advanceTimersByTime(2000)

      expect(cacheService.get('test')).toBeNull()
      jest.useRealTimers()
    })

    it('should return data before expiration', () => {
      jest.useFakeTimers()
      const data = { id: '1' }
      cacheService.set('test', data, 10000) // 10 seconds

      jest.advanceTimersByTime(5000) // 5 seconds

      expect(cacheService.get('test')).toEqual(data)
      jest.useRealTimers()
    })
  })

  describe('has', () => {
    it('should return true for valid cached data', () => {
      cacheService.set('key', { data: 'value' })
      expect(cacheService.has('key')).toBe(true)
    })

    it('should return false for non-existent or expired data', () => {
      expect(cacheService.has('nonexistent')).toBe(false)
    })
  })

  describe('getAge', () => {
    it('should return age of cached data', () => {
      jest.useFakeTimers()
      const now = Date.now()
      jest.setSystemTime(now)

      cacheService.set('test', { data: 'value' })

      jest.advanceTimersByTime(5000)

      const age = cacheService.getAge('test')
      expect(age).toBeGreaterThanOrEqual(5000)
      expect(age).toBeLessThan(6000)

      jest.useRealTimers()
    })

    it('should return null for non-existent data', () => {
      expect(cacheService.getAge('nonexistent')).toBeNull()
    })
  })

  describe('isExpired', () => {
    it('should return true for expired data', () => {
      jest.useFakeTimers()
      cacheService.set('test', { data: 'value' }, 1000)

      jest.advanceTimersByTime(2000)

      expect(cacheService.isExpired('test', 1000)).toBe(true)
      jest.useRealTimers()
    })

    it('should return false for valid data', () => {
      jest.useFakeTimers()
      cacheService.set('test', { data: 'value' }, 10000)

      jest.advanceTimersByTime(5000)

      expect(cacheService.isExpired('test', 10000)).toBe(false)
      jest.useRealTimers()
    })
  })

  describe('remove', () => {
    it('should remove cached data', () => {
      cacheService.set('test', { data: 'value' })
      expect(cacheService.has('test')).toBe(true)

      cacheService.remove('test')
      expect(cacheService.has('test')).toBe(false)
    })

    it('should not error when removing non-existent keys', () => {
      expect(() => cacheService.remove('nonexistent')).not.toThrow()
    })
  })

  describe('clear', () => {
    it('should remove all cached data', () => {
      cacheService.set('key1', { data: '1' })
      cacheService.set('key2', { data: '2' })

      expect(cacheService.has('key1')).toBe(true)
      expect(cacheService.has('key2')).toBe(true)

      cacheService.clear()

      expect(cacheService.has('key1')).toBe(false)
      expect(cacheService.has('key2')).toBe(false)
    })
  })
})
