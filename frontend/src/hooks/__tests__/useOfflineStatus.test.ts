import { renderHook, waitFor } from '@testing-library/react'
import { useOfflineStatus } from '../useOfflineStatus'
import { cacheService } from '@/services/cacheService'

jest.mock('@/services/cacheService')

describe('useOfflineStatus', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    ;(cacheService.getAge as jest.Mock).mockReturnValue(null)
  })

  it('should return initial online status', () => {
    const { result } = renderHook(() => useOfflineStatus())

    expect(result.current.isOnline).toBe(navigator.onLine)
  })

  it('should update when going offline', async () => {
    const { result } = renderHook(() => useOfflineStatus())

    // Simulate going offline
    Object.defineProperty(navigator, 'onLine', {
      configurable: true,
      value: false,
    })

    window.dispatchEvent(new Event('offline'))

    await waitFor(() => {
      expect(result.current.isOnline).toBe(false)
    })
  })

  it('should update when coming back online', async () => {
    Object.defineProperty(navigator, 'onLine', {
      configurable: true,
      value: false,
    })

    const { result } = renderHook(() => useOfflineStatus())

    Object.defineProperty(navigator, 'onLine', {
      configurable: true,
      value: true,
    })

    window.dispatchEvent(new Event('online'))

    await waitFor(() => {
      expect(result.current.isOnline).toBe(true)
    })
  })

  it('should check cache age', () => {
    ;(cacheService.getAge as jest.Mock).mockReturnValue(1000)

    const { result } = renderHook(() => useOfflineStatus())

    expect(result.current.cacheAge).toBe(1000)
  })

  it('should detect expired cache', () => {
    const expiredAge = 49 * 60 * 60 * 1000 // 49 hours

    ;(cacheService.getAge as jest.Mock).mockReturnValue(expiredAge)

    const { result } = renderHook(() => useOfflineStatus())

    expect(result.current.cacheExpired).toBe(true)
  })

  it('should not mark cache as expired when within 48 hours', () => {
    const validAge = 24 * 60 * 60 * 1000 // 24 hours

    ;(cacheService.getAge as jest.Mock).mockReturnValue(validAge)

    const { result } = renderHook(() => useOfflineStatus())

    expect(result.current.cacheExpired).toBe(false)
  })

  it('should cleanup event listeners on unmount', () => {
    const removeEventListenerSpy = jest.spyOn(window, 'removeEventListener')

    const { unmount } = renderHook(() => useOfflineStatus())

    unmount()

    expect(removeEventListenerSpy).toHaveBeenCalledWith('online', expect.any(Function))
    expect(removeEventListenerSpy).toHaveBeenCalledWith('offline', expect.any(Function))

    removeEventListenerSpy.mockRestore()
  })
})
