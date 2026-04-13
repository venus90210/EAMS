'use client'

import React, { createContext, useCallback, useEffect, useState } from 'react'
import { LoginResponse, Role, TokenPair, User } from '@/types'
import apiClient from '@/services/apiClient'
import { authService } from '@/services/authService'

export interface AuthContextType {
  user: User | null
  loading: boolean
  error: string | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<string | null>
  mfaVerify: (sessionToken: string, code: string) => Promise<void>
  logout: () => Promise<void>
  refreshSilently: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  /**
   * Initialize auth state on mount
   */
  useEffect(() => {
    const initAuth = async () => {
      try {
        const refreshToken = authService.getRefreshToken()
        if (!refreshToken) {
          setLoading(false)
          return
        }

        // Try to refresh and restore session
        await refreshSilently()
      } catch (err) {
        console.error('Failed to restore session:', err)
        authService.clearTokens()
      } finally {
        setLoading(false)
      }
    }

    initAuth()
  }, [])

  /**
   * Decode JWT payload without verification (client-side only for UX)
   */
  const decodeToken = (token: string): User | null => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return {
        id: payload.sub,
        email: '',
        role: payload.role as Role,
        institutionId: payload.institutionId,
        createdAt: new Date(),
        updatedAt: new Date(),
      }
    } catch {
      return null
    }
  }

  /**
   * Login with email and password.
   * Returns sessionToken if MFA is required, null otherwise.
   */
  const login = useCallback(async (email: string, password: string): Promise<string | null> => {
    try {
      setError(null)
      const response = await apiClient.post<LoginResponse>('/auth/login', {
        email,
        password,
      })

      if (response.data.mfaRequired && response.data.sessionToken) {
        return response.data.sessionToken
      }

      if (response.data.tokens) {
        authService.storeTokens(response.data.tokens)
        const decoded = decodeToken(response.data.tokens.accessToken)
        if (decoded) setUser(decoded)
      }

      return null
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed')
      throw err
    }
  }, [])

  /**
   * Verify MFA code
   */
  const mfaVerify = useCallback(async (sessionToken: string, code: string) => {
    try {
      setError(null)
      const response = await apiClient.post<TokenPair>('/auth/mfa/verify', {
        sessionToken,
        code,
      })

      authService.storeTokens(response.data)
      const decoded = decodeToken(response.data.accessToken)
      if (decoded) setUser(decoded)
    } catch (err: any) {
      setError(err.response?.data?.message || 'MFA verification failed')
      throw err
    }
  }, [decodeToken])

  /**
   * Logout
   */
  const logout = useCallback(async () => {
    try {
      const refreshToken = authService.getRefreshToken()
      if (refreshToken) {
        await apiClient.post('/auth/logout', { refreshToken })
      }
    } catch (err) {
      console.error('Logout request failed:', err)
    } finally {
      authService.clearTokens()
      setUser(null)
      setError(null)
    }
  }, [])

  /**
   * Refresh access token silently
   */
  const refreshSilently = useCallback(async () => {
    try {
      const refreshToken = authService.getRefreshToken()
      if (!refreshToken) throw new Error('No refresh token')

      const response = await apiClient.post<TokenPair>('/auth/refresh', {
        refreshToken,
      })

      authService.storeTokens(response.data)
      const decoded = decodeToken(response.data.accessToken)
      if (decoded) setUser(decoded)
    } catch (err) {
      authService.clearTokens()
      setUser(null)
      throw err
    }
  }, [decodeToken])

  const value: AuthContextType = {
    user,
    loading,
    error,
    isAuthenticated: user !== null,
    login,
    mfaVerify,
    logout,
    refreshSilently,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuthContext(): AuthContextType {
  const context = React.useContext(AuthContext)
  if (!context) {
    throw new Error('useAuthContext must be used within AuthProvider')
  }
  return context
}
