import { authService } from '../authService'

describe('authService', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear()
    // Reset in-memory token
    authService.clearTokens()
  })

  describe('storeTokens', () => {
    it('should store tokens correctly', () => {
      const tokens = { accessToken: 'access123', refreshToken: 'refresh123' }
      authService.storeTokens(tokens)

      expect(authService.getAccessToken()).toBe('access123')
      expect(authService.getRefreshToken()).toBe('refresh123')
    })

    it('should store refresh token in localStorage', () => {
      const tokens = { accessToken: 'access123', refreshToken: 'refresh123' }
      authService.storeTokens(tokens)

      expect(localStorage.getItem('eams_refresh_token')).toBe('refresh123')
    })
  })

  describe('getAccessToken', () => {
    it('should return null when no token is stored', () => {
      expect(authService.getAccessToken()).toBeNull()
    })

    it('should return access token after storing', () => {
      authService.storeTokens({ accessToken: 'token123', refreshToken: 'refresh123' })
      expect(authService.getAccessToken()).toBe('token123')
    })
  })

  describe('getRefreshToken', () => {
    it('should return null when no token is stored', () => {
      expect(authService.getRefreshToken()).toBeNull()
    })

    it('should return refresh token from localStorage', () => {
      authService.storeTokens({ accessToken: 'access123', refreshToken: 'refresh123' })
      expect(authService.getRefreshToken()).toBe('refresh123')
    })
  })

  describe('isAuthenticated', () => {
    it('should return false when no tokens are stored', () => {
      expect(authService.isAuthenticated()).toBe(false)
    })

    it('should return true when access token is stored', () => {
      authService.storeTokens({ accessToken: 'token123', refreshToken: 'refresh123' })
      expect(authService.isAuthenticated()).toBe(true)
    })
  })

  describe('setAccessToken', () => {
    it('should update access token', () => {
      authService.setAccessToken('new-token')
      expect(authService.getAccessToken()).toBe('new-token')
    })
  })

  describe('clearTokens', () => {
    it('should clear all tokens', () => {
      authService.storeTokens({ accessToken: 'access123', refreshToken: 'refresh123' })
      authService.clearTokens()

      expect(authService.getAccessToken()).toBeNull()
      expect(authService.getRefreshToken()).toBeNull()
      expect(authService.isAuthenticated()).toBe(false)
    })

    it('should remove refresh token from localStorage', () => {
      authService.storeTokens({ accessToken: 'access123', refreshToken: 'refresh123' })
      authService.clearTokens()

      expect(localStorage.getItem('eams_refresh_token')).toBeNull()
    })
  })
})
