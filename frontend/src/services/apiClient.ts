import axios, { AxiosInstance, AxiosError } from 'axios'
import { authService } from './authService'
import { TokenPair } from '@/types'

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3000'

// Flag to prevent multiple refresh attempts simultaneously
let isRefreshing = false
let refreshSubscribers: ((token: string) => void)[] = []

/**
 * Create axios instance with base config
 */
const axiosInstance: AxiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * Request interceptor: add Authorization header
 */
axiosInstance.interceptors.request.use(
  (config) => {
    const token = authService.getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

/**
 * Subscribe to token refresh: retry failed requests after refresh
 */
const subscribeTokenRefresh = (onRefreshed: (token: string) => void): (() => void) => {
  refreshSubscribers.push(onRefreshed)
  return () => {
    refreshSubscribers = refreshSubscribers.filter((sub) => sub !== onRefreshed)
  }
}

/**
 * Notify all subscribers of token refresh
 */
const onRefreshed = (token: string): void => {
  refreshSubscribers.forEach((callback) => callback(token))
  refreshSubscribers = []
}

/**
 * Response interceptor: handle 401 errors with token refresh
 */
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as any

    // If error is not 401 or is a refresh request, don't retry
    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error)
    }

    // If already refreshing, wait for new token
    if (isRefreshing) {
      return new Promise((resolve) => {
        subscribeTokenRefresh((token: string) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          resolve(axiosInstance(originalRequest))
        })
      })
    }

    // Start refresh
    isRefreshing = true
    originalRequest._retry = true

    try {
      const refreshToken = authService.getRefreshToken()
      if (!refreshToken) {
        throw new Error('No refresh token available')
      }

      // Call refresh endpoint
      const response = await axios.post<TokenPair>(`${API_URL}/auth/refresh`, {
        refreshToken,
      })

      const { accessToken, refreshToken: newRefreshToken } = response.data

      // Update tokens
      authService.storeTokens({ accessToken, refreshToken: newRefreshToken })

      // Update current request
      originalRequest.headers.Authorization = `Bearer ${accessToken}`

      // Notify subscribers
      onRefreshed(accessToken)

      // Retry original request
      return axiosInstance(originalRequest)
    } catch (refreshError) {
      // Refresh failed - clear tokens and redirect to login
      authService.clearTokens()
      // Redirect to login (will be handled by AuthContext/useAuth)
      if (typeof window !== 'undefined') {
        window.location.href = '/login'
      }
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  },
)

export default axiosInstance
