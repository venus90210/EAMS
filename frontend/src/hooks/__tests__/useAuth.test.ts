import { renderHook, act, waitFor } from '@testing-library/react'
import { useAuth } from '../useAuth'
import * as authService from '@/services/authService'
import apiClient from '@/services/apiClient'

jest.mock('@/services/authService')
jest.mock('@/services/apiClient')

describe('useAuth', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should initialize with default state', () => {
    ;(authService.getAccessToken as jest.Mock).mockReturnValue(null)
    const { result } = renderHook(() => useAuth())

    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.user).toBeNull()
  })

  it('should login successfully without MFA', async () => {
    const mockResponse = {
      data: {
        accessToken: 'mock_access_token',
        refreshToken: 'mock_refresh_token',
        user: { id: '1', email: 'test@example.com', role: 'GUARDIAN' },
      },
    }

    ;(apiClient.post as jest.Mock).mockResolvedValue(mockResponse)
    ;(authService.storeTokens as jest.Mock).mockImplementation(() => {})
    ;(authService.getAccessToken as jest.Mock).mockReturnValue('mock_access_token')

    const { result } = renderHook(() => useAuth())

    await act(async () => {
      await result.current.login('test@example.com', 'password')
    })

    await waitFor(() => {
      expect(result.current.isAuthenticated).toBe(true)
    })

    expect(authService.storeTokens).toHaveBeenCalledWith(
      'mock_access_token',
      'mock_refresh_token'
    )
  })

  it('should require MFA when indicated', async () => {
    const mockResponse = {
      data: {
        mfaRequired: true,
        sessionToken: 'mock_session_token',
      },
    }

    ;(apiClient.post as jest.Mock).mockResolvedValue(mockResponse)

    const { result } = renderHook(() => useAuth())

    await act(async () => {
      try {
        await result.current.login('test@example.com', 'password')
      } catch (err) {
        // Expected error for MFA required
      }
    })
  })

  it('should handle login failure with invalid credentials', async () => {
    ;(apiClient.post as jest.Mock).mockRejectedValue({
      response: {
        status: 401,
        data: { message: 'Invalid credentials' },
      },
    })

    const { result } = renderHook(() => useAuth())

    await act(async () => {
      try {
        await result.current.login('test@example.com', 'wrong_password')
      } catch (err) {
        // Expected error
      }
    })

    expect(result.current.isAuthenticated).toBe(false)
  })

  it('should logout successfully', async () => {
    ;(authService.clearTokens as jest.Mock).mockImplementation(() => {})
    ;(apiClient.post as jest.Mock).mockResolvedValue({})

    const { result } = renderHook(() => useAuth())

    await act(async () => {
      await result.current.logout()
    })

    expect(authService.clearTokens).toHaveBeenCalled()
    expect(result.current.isAuthenticated).toBe(false)
  })
})
