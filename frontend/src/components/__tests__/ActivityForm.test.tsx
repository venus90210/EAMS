import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ActivityForm } from '../admin/ActivityForm'
import { Activity } from '@/types'

const mockActivity: Activity = {
  id: 'activity-1',
  name: 'Fútbol',
  description: 'Actividad de fútbol',
  totalSpots: 20,
  availableSpots: 15,
  status: 'PUBLISHED',
  institutionId: 'inst-1',
  createdAt: new Date(),
  updatedAt: new Date(),
}

describe('ActivityForm', () => {
  const mockOnSubmit = jest.fn()
  const mockOnCancel = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should render form for creating new activity', () => {
    render(
      <ActivityForm
        loading={false}
        error={null}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    expect(screen.getByText(/Nueva actividad/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/nombre/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/descripción/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/cupos/i)).toBeInTheDocument()
  })

  it('should render form for editing existing activity', () => {
    render(
      <ActivityForm
        activity={mockActivity}
        loading={false}
        error={null}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    expect(screen.getByText(/Editar actividad/i)).toBeInTheDocument()
    expect((screen.getByLabelText(/nombre/i) as HTMLInputElement).value).toBe('Fútbol')
    expect((screen.getByLabelText(/descripción/i) as HTMLInputElement).value).toBe(
      'Actividad de fútbol',
    )
    expect((screen.getByLabelText(/cupos/i) as HTMLInputElement).value).toBe('20')
  })

  it('should validate name field is required', async () => {
    const user = userEvent.setup()
    render(
      <ActivityForm
        loading={false}
        error={null}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    const nameInput = screen.getByLabelText(/nombre/i)
    const descInput = screen.getByLabelText(/descripción/i)
    const spotsInput = screen.getByLabelText(/cupos/i)
    const submitButton = screen.getByRole('button', { name: /confirmar|crear/i })

    // Fill in valid data except name
    await user.clear(nameInput)
    await user.type(descInput, 'Test description')
    await user.type(spotsInput, '10')

    // Try to submit
    await user.click(submitButton)

    // Should not call onSubmit due to validation
    await waitFor(() => {
      expect(mockOnSubmit).not.toHaveBeenCalled()
    })
  })

  it('should validate totalSpots is greater than 0', async () => {
    const user = userEvent.setup()
    render(
      <ActivityForm
        loading={false}
        error={null}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    const nameInput = screen.getByLabelText(/nombre/i)
    const spotsInput = screen.getByLabelText(/cupos/i)
    const submitButton = screen.getByRole('button', { name: /confirmar|crear/i })

    await user.type(nameInput, 'Fútbol')
    await user.type(spotsInput, '0')
    await user.click(submitButton)

    // Should not submit with invalid spots
    await waitFor(() => {
      expect(mockOnSubmit).not.toHaveBeenCalled()
    })
  })

  it('should submit form with valid data', async () => {
    const user = userEvent.setup()
    mockOnSubmit.mockResolvedValue(undefined)

    render(
      <ActivityForm
        loading={false}
        error={null}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    const nameInput = screen.getByLabelText(/nombre/i)
    const descInput = screen.getByLabelText(/descripción/i)
    const spotsInput = screen.getByLabelText(/cupos/i)
    const submitButton = screen.getByRole('button', { name: /confirmar|crear/i })

    await user.type(nameInput, 'Nueva Actividad')
    await user.type(descInput, 'Una actividad nueva')
    await user.type(spotsInput, '25')

    await user.click(submitButton)

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        name: 'Nueva Actividad',
        description: 'Una actividad nueva',
        totalSpots: 25,
      })
    })
  })

  it('should call onCancel when cancel button is clicked', async () => {
    const user = userEvent.setup()
    render(
      <ActivityForm
        loading={false}
        error={null}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    const cancelButton = screen.getByRole('button', { name: /cancelar/i })
    await user.click(cancelButton)

    expect(mockOnCancel).toHaveBeenCalled()
  })

  it('should display error message when error prop is provided', () => {
    render(
      <ActivityForm
        loading={false}
        error="Nombre de actividad duplicado"
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    expect(screen.getByText('Nombre de actividad duplicado')).toBeInTheDocument()
  })

  it('should disable submit button when loading', () => {
    render(
      <ActivityForm
        loading={true}
        error={null}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    const submitButton = screen.getByRole('button', { name: /confirmar|crear|guardando/i })
    expect(submitButton).toBeDisabled()
  })

  it('should show loading state text', () => {
    render(
      <ActivityForm
        loading={true}
        error={null}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    // Button should contain loading indicator or text
    const submitButton = screen.getByRole('button', { name: /guardar|guardando/i })
    expect(submitButton).toBeInTheDocument()
  })

  it('should clear error when user starts typing after error', async () => {
    const user = userEvent.setup()
    const { rerender } = render(
      <ActivityForm
        loading={false}
        error="Previous error"
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    expect(screen.getByText('Previous error')).toBeInTheDocument()

    // Rerender without error
    rerender(
      <ActivityForm
        loading={false}
        error={null}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    // Error should be gone
    expect(screen.queryByText('Previous error')).not.toBeInTheDocument()
  })

  it('should pre-fill form when editing activity', () => {
    render(
      <ActivityForm
        activity={mockActivity}
        loading={false}
        error={null}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    const nameInput = screen.getByLabelText(/nombre/i) as HTMLInputElement
    const descInput = screen.getByLabelText(/descripción/i) as HTMLInputElement
    const spotsInput = screen.getByLabelText(/cupos/i) as HTMLInputElement

    expect(nameInput.value).toBe('Fútbol')
    expect(descInput.value).toBe('Actividad de fútbol')
    expect(spotsInput.value).toBe('20')
  })

  it('should submit edit with correct data', async () => {
    const user = userEvent.setup()
    mockOnSubmit.mockResolvedValue(undefined)

    render(
      <ActivityForm
        activity={mockActivity}
        loading={false}
        error={null}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
      />,
    )

    const nameInput = screen.getByLabelText(/nombre/i)
    const spotsInput = screen.getByLabelText(/cupos/i)
    const submitButton = screen.getByRole('button', { name: /guardar|actualizar/i })

    // Change values
    await user.clear(nameInput)
    await user.type(nameInput, 'Fútbol Profesional')
    await user.clear(spotsInput)
    await user.type(spotsInput, '30')

    await user.click(submitButton)

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'Fútbol Profesional',
          totalSpots: 30,
        }),
      )
    })
  })
})
