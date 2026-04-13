import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { LoginForm } from '../auth/LoginForm'
import { useAuth } from '@/hooks/useAuth'

jest.mock('@/hooks/useAuth')

describe('LoginForm', () => {
  const mockLogin = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
    ;(useAuth as jest.Mock).mockReturnValue({
      login: mockLogin,
      isAuthenticated: false,
      user: null,
      logout: jest.fn(),
      mfaVerify: jest.fn(),
      refreshSilently: jest.fn(),
      loading: false,
      error: null,
    })
  })

  it('should render form fields', () => {
    render(<LoginForm />)

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/contraseña/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /iniciar sesión/i })).toBeInTheDocument()
  })

  it('should render heading', () => {
    render(<LoginForm />)
    expect(screen.getByText('EAMS Login')).toBeInTheDocument()
  })

  it('should call login with form data on valid submission', async () => {
    mockLogin.mockResolvedValue(null)
    const user = userEvent.setup()
    const mockOnSuccess = jest.fn()

    render(<LoginForm onSuccess={mockOnSuccess} />)

    await user.type(screen.getByLabelText(/email/i), 'test@example.com')
    await user.type(screen.getByLabelText(/contraseña/i), 'password123')
    await user.click(screen.getByRole('button', { name: /iniciar sesión/i }))

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'password123')
    })
  })

  it('should call onSuccess when MFA is not required', async () => {
    mockLogin.mockResolvedValue(null)
    const user = userEvent.setup()
    const mockOnSuccess = jest.fn()

    render(<LoginForm onSuccess={mockOnSuccess} />)

    await user.type(screen.getByLabelText(/email/i), 'test@example.com')
    await user.type(screen.getByLabelText(/contraseña/i), 'password123')
    await user.click(screen.getByRole('button', { name: /iniciar sesión/i }))

    await waitFor(() => {
      expect(mockOnSuccess).toHaveBeenCalled()
    })
  })

  it('should call onMfaRequired when sessionToken is returned', async () => {
    mockLogin.mockResolvedValue('session_token_123')
    const user = userEvent.setup()
    const mockOnMfaRequired = jest.fn()

    render(<LoginForm onMfaRequired={mockOnMfaRequired} />)

    await user.type(screen.getByLabelText(/email/i), 'test@example.com')
    await user.type(screen.getByLabelText(/contraseña/i), 'password123')
    await user.click(screen.getByRole('button', { name: /iniciar sesión/i }))

    await waitFor(() => {
      expect(mockOnMfaRequired).toHaveBeenCalledWith('session_token_123')
    })
  })

  it('should display error on login failure', async () => {
    mockLogin.mockRejectedValue({
      response: { data: { message: 'Invalid credentials' } },
    })
    const user = userEvent.setup()

    render(<LoginForm />)

    await user.type(screen.getByLabelText(/email/i), 'test@example.com')
    await user.type(screen.getByLabelText(/contraseña/i), 'wrongpassword')
    await user.click(screen.getByRole('button', { name: /iniciar sesión/i }))

    await waitFor(() => {
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument()
    })
  })

  it('should disable button during submission', async () => {
    mockLogin.mockImplementation(
      () =>
        new Promise((resolve) => {
          setTimeout(() => resolve(null), 200)
        }),
    )
    const user = userEvent.setup()

    render(<LoginForm />)

    await user.type(screen.getByLabelText(/email/i), 'test@example.com')
    await user.type(screen.getByLabelText(/contraseña/i), 'password123')
    const submitButton = screen.getByRole('button', { name: /iniciar sesión/i })

    await user.click(submitButton)
    expect(submitButton).toBeDisabled()
  })
})
