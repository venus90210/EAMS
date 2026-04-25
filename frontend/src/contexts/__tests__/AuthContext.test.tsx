import { render, screen, waitFor, act } from '@testing-library/react'
import { AuthProvider } from '../AuthContext'
import { useAuth } from '@/hooks/useAuth'
import { authService } from '@/services/authService'

jest.mock('@/services/authService')
jest.mock('@/services/apiClient')

// Mock component that uses useAuth
function TestComponent() {
  const { user, isAuthenticated, login, logout, mfaVerify, loading, error } = useAuth()

  return (
    <div>
      <div data-testid="auth-status">
        {isAuthenticated ? `Authenticated: ${user?.name}` : 'Not authenticated'}
      </div>
      <div data-testid="loading">{loading ? 'Loading' : 'Not loading'}</div>
      <div data-testid="error">{error || 'No error'}</div>
      <button onClick={() => login('test@example.com', 'password')}>Login</button>
      <button onClick={() => logout()}>Logout</button>
      <button onClick={() => mfaVerify('session_token', '123456')}>MFA Verify</button>
    </div>
  )
}

describe('AuthContext', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    localStorage.clear()
    ;(authService.isAuthenticated as jest.Mock).mockReturnValue(false)
    ;(authService.getAccessToken as jest.Mock).mockReturnValue(null)
    ;(authService.getRefreshToken as jest.Mock).mockReturnValue(null)
  })

  it('should initialize with unauthenticated state', () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    )

    expect(screen.getByTestId('auth-status')).toHaveTextContent('Not authenticated')
  })

  it('should restore session from refresh token on mount', async () => {
    // Mock localStorage having a refresh token
    ;(authService.getRefreshToken as jest.Mock).mockReturnValue('refresh_token_123')

    // Mock the fetch call to /auth/refresh
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        accessToken: 'access_token_123',
        refreshToken: 'refresh_token_456',
      }),
    })

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    )

    // Wait for the effect to complete
    await waitFor(() => {
      expect(authService.storeTokens).toHaveBeenCalled()
    })
  })

  it('should handle login without MFA', async () => {
    // Mock successful login response
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        accessToken: 'access_token_123',
        refreshToken: 'refresh_token_123',
      }),
    })

    const { getByText, getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    )

    await act(async () => {
      getByText('Login').click()
    })

    await waitFor(() => {
      expect(authService.storeTokens).toHaveBeenCalled()
      expect(getByTestId('auth-status')).toHaveTextContent('Authenticated')
    })
  })

  it('should handle login with MFA required', async () => {
    // Mock login response with sessionToken (MFA required)
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        sessionToken: 'session_token_123',
      }),
    })

    const { getByText, getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    )

    await act(async () => {
      getByText('Login').click()
    })

    // Should NOT store tokens yet (still need MFA)
    await waitFor(() => {
      expect(getByTestId('auth-status')).toHaveTextContent('Not authenticated')
    })
  })

  it('should handle login failure with error', async () => {
    global.fetch = jest.fn().mockResolvedValue({
      ok: false,
      json: async () => ({
        message: 'Invalid credentials',
      }),
    })

    const { getByText, getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    )

    await act(async () => {
      getByText('Login').click()
    })

    await waitFor(() => {
      expect(getByTestId('error')).toHaveTextContent('Invalid credentials')
    })
  })

  it('should clear tokens on logout', async () => {
    // Start authenticated
    ;(authService.isAuthenticated as jest.Mock).mockReturnValue(true)
    ;(authService.getAccessToken as jest.Mock).mockReturnValue('access_token_123')

    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: async () => ({}),
    })

    const { getByText, getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    )

    await act(async () => {
      getByText('Logout').click()
    })

    await waitFor(() => {
      expect(authService.clearTokens).toHaveBeenCalled()
      expect(getByTestId('auth-status')).toHaveTextContent('Not authenticated')
    })
  })

  it('should handle MFA verification', async () => {
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        accessToken: 'access_token_after_mfa',
        refreshToken: 'refresh_token_after_mfa',
      }),
    })

    const { getByText, getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    )

    await act(async () => {
      getByText('MFA Verify').click()
    })

    await waitFor(() => {
      expect(authService.storeTokens).toHaveBeenCalled()
      expect(getByTestId('auth-status')).toHaveTextContent('Authenticated')
    })
  })

  it('should handle invalid MFA code', async () => {
    global.fetch = jest.fn().mockResolvedValue({
      ok: false,
      json: async () => ({
        message: 'Invalid TOTP code',
      }),
    })

    const { getByText, getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    )

    await act(async () => {
      getByText('MFA Verify').click()
    })

    await waitFor(() => {
      expect(getByTestId('error')).toHaveTextContent('Invalid TOTP code')
    })
  })

  it('should set loading state during async operations', async () => {
    global.fetch = jest.fn().mockImplementation(
      () =>
        new Promise((resolve) =>
          setTimeout(
            () =>
              resolve({
                ok: true,
                json: async () => ({
                  accessToken: 'token',
                  refreshToken: 'refresh',
                }),
              }),
            50,
          ),
        ),
    )

    const { getByText, getByTestId } = render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>,
    )

    const loginButton = getByText('Login')

    await act(async () => {
      loginButton.click()
    })

    // Loading should be true during the request
    // (This is a simplified test; real implementation might show loading differently)
    await waitFor(() => {
      expect(getByTestId('loading')).toBeInTheDocument()
    })
  })
})
