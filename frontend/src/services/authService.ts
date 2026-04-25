import { TokenPair } from '@/types'

const ACCESS_TOKEN_KEY = 'eams_access_token'
const REFRESH_TOKEN_KEY = 'eams_refresh_token'

let accessTokenInMemory: string | null = null

/**
 * Store tokens securely:
 * - Access token: stored in memory (lost on page refresh)
 * - Refresh token: stored in localStorage (persisted across sessions)
 */
export const authService = {
  /**
   * Store tokens after successful login
   */
  storeTokens(pair: TokenPair): void {
    accessTokenInMemory = pair.accessToken
    if (typeof window !== 'undefined') {
      localStorage.setItem(REFRESH_TOKEN_KEY, pair.refreshToken)
    }
  },

  /**
   * Get the current access token
   */
  getAccessToken(): string | null {
    return accessTokenInMemory
  },

  /**
   * Get the refresh token from localStorage
   */
  getRefreshToken(): string | null {
    if (typeof window === 'undefined') return null
    return localStorage.getItem(REFRESH_TOKEN_KEY)
  },

  /**
   * Check if user is authenticated (has access token)
   */
  isAuthenticated(): boolean {
    return accessTokenInMemory !== null
  },

  /**
   * Update access token (after refresh)
   */
  setAccessToken(token: string): void {
    accessTokenInMemory = token
  },

  /**
   * Clear all tokens (logout)
   */
  clearTokens(): void {
    accessTokenInMemory = null
    if (typeof window !== 'undefined') {
      localStorage.removeItem(REFRESH_TOKEN_KEY)
    }
  },

  /**
   * Restore tokens on page load (if refresh token exists)
   */
  restoreTokensFromStorage(): TokenPair | null {
    if (typeof window === 'undefined') return null

    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
    if (!refreshToken) return null

    return { accessToken: '', refreshToken }
  },
}
