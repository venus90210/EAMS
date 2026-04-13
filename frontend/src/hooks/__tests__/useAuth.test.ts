import { renderHook } from '@testing-library/react'
import { useAuth } from '../useAuth'
import { renderHookWithAuth } from '@/test-utils'

describe('useAuth', () => {
  it('should return auth context functions', () => {
    const { result } = renderHookWithAuth(() => useAuth())

    expect(typeof result.current.login).toBe('function')
    expect(typeof result.current.logout).toBe('function')
    expect(typeof result.current.mfaVerify).toBe('function')
    expect(typeof result.current.refreshSilently).toBe('function')
    expect(typeof result.current.isAuthenticated).toBe('boolean')
  })

  it('should have user null initially', () => {
    const { result } = renderHookWithAuth(() => useAuth())

    expect(result.current.user).toBeNull()
  })

  it('should have loading property', () => {
    const { result } = renderHookWithAuth(() => useAuth())

    expect(typeof result.current.loading).toBe('boolean')
  })

  it('should have error null initially', () => {
    const { result } = renderHookWithAuth(() => useAuth())

    expect(result.current.error).toBeNull()
  })

  it('should have isAuthenticated false when user is null', () => {
    const { result } = renderHookWithAuth(() => useAuth())

    expect(result.current.isAuthenticated).toBe(false)
  })
})
