import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { EnrollmentForm } from '../enrollment/EnrollmentForm'
import { Student, Activity } from '@/types'

const mockStudent: Student = {
  id: 'student-1',
  firstName: 'Juan',
  lastName: 'Pérez',
  grade: '6A',
  institutionId: 'inst-1',
}

const mockActivity: Activity = {
  id: 'activity-1',
  name: 'Football',
  description: 'Football training',
  totalSpots: 20,
  availableSpots: 15,
  status: 'PUBLISHED',
  institutionId: 'inst-1',
  createdBy: 'admin-1',
  schedule: {
    dayOfWeek: 'MONDAY',
    startTime: '15:00',
    endTime: '16:30',
  },
}

describe('EnrollmentForm', () => {
  it('should render form with student dropdown', () => {
    const mockOnSuccess = jest.fn()
    const mockOnCancel = jest.fn()

    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={mockOnSuccess}
        onCancel={mockOnCancel}
      />
    )

    expect(screen.getByText('Inscribir a Football')).toBeInTheDocument()
    expect(screen.getByLabelText('Selecciona un hijo')).toBeInTheDocument()
  })

  it('should show activity details', () => {
    const mockOnSuccess = jest.fn()
    const mockOnCancel = jest.fn()

    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={mockOnSuccess}
        onCancel={mockOnCancel}
      />
    )

    expect(screen.getByText('Football')).toBeInTheDocument()
    expect(screen.getByText('15/20')).toBeInTheDocument()
  })

  it('should submit form and show confirmation', async () => {
    const mockOnSuccess = jest.fn()
    const mockOnCancel = jest.fn()

    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={mockOnSuccess}
        onCancel={mockOnCancel}
      />
    )

    const select = screen.getByLabelText('Selecciona un hijo')
    await userEvent.selectOptions(select, mockStudent.id)

    const submitButton = screen.getByText('Siguiente')
    fireEvent.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText('Confirmar inscripción')).toBeInTheDocument()
    })
  })

  it('should disable submit button when no students available', () => {
    const mockOnSuccess = jest.fn()
    const mockOnCancel = jest.fn()

    render(
      <EnrollmentForm
        students={[]}
        activity={mockActivity}
        onSuccess={mockOnSuccess}
        onCancel={mockOnCancel}
      />
    )

    const submitButton = screen.getByText('Siguiente')
    expect(submitButton).toBeDisabled()
  })

  it('should show confirmation screen with enrollment details', async () => {
    const mockOnSuccess = jest.fn()
    const mockOnCancel = jest.fn()

    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={mockOnSuccess}
        onCancel={mockOnCancel}
      />
    )

    const select = screen.getByLabelText('Selecciona un hijo')
    await userEvent.selectOptions(select, mockStudent.id)

    const submitButton = screen.getByText('Siguiente')
    fireEvent.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText(mockStudent.firstName + ' ' + mockStudent.lastName)).toBeInTheDocument()
      expect(screen.getByText('Football')).toBeInTheDocument()
      expect(screen.getByText('MONDAY 15:00 - 16:30')).toBeInTheDocument()
    })
  })

  it('should show error message when provided', async () => {
    const mockOnSuccess = jest.fn()
    const mockOnCancel = jest.fn()
    const errorMessage = 'Cupos agotados'

    const { rerender } = render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={mockOnSuccess}
        onCancel={mockOnCancel}
      />
    )

    const select = screen.getByLabelText('Selecciona un hijo')
    await userEvent.selectOptions(select, mockStudent.id)

    fireEvent.click(screen.getByText('Siguiente'))

    await waitFor(() => {
      rerender(
        <EnrollmentForm
          students={[mockStudent]}
          activity={mockActivity}
          onSuccess={mockOnSuccess}
          onCancel={mockOnCancel}
          error={{ code: 'SPOT_EXHAUSTED', message: errorMessage }}
        />
      )
    })

    expect(screen.getByText(errorMessage)).toBeInTheDocument()
  })

  it('should call onCancel when cancel button is clicked', async () => {
    const mockOnSuccess = jest.fn()
    const mockOnCancel = jest.fn()

    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={mockOnSuccess}
        onCancel={mockOnCancel}
      />
    )

    const cancelButton = screen.getByText('Cancelar')
    fireEvent.click(cancelButton)

    expect(mockOnCancel).toHaveBeenCalled()
  })
})
