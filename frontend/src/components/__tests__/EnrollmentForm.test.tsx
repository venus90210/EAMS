import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { EnrollmentForm } from '../enrollment/EnrollmentForm'
import { useEnrollment } from '@/hooks/useEnrollment'
import { Student, Activity } from '@/types'

jest.mock('@/hooks/useEnrollment')

const mockStudent: Student = {
  id: 'student-1',
  firstName: 'Juan',
  lastName: 'Pérez',
  grade: '6A',
  institutionId: 'inst-1',
}

const mockStudent2: Student = {
  id: 'student-2',
  firstName: 'María',
  lastName: 'García',
  grade: '6B',
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
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
  schedule: {
    dayOfWeek: 'MONDAY',
    startTime: '15:00',
    endTime: '16:30',
  },
}

describe('EnrollmentForm', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    ;(useEnrollment as jest.Mock).mockReturnValue({
      enroll: jest.fn().mockResolvedValue(undefined),
      loading: false,
      error: null,
      cancel: jest.fn(),
    })
  })

  it('should render enrollment form', () => {
    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={jest.fn()}
        onCancel={jest.fn()}
      />
    )

    expect(screen.getByRole('combobox')).toBeInTheDocument()
    expect(screen.getAllByText(/Football/).length).toBeGreaterThan(0)
  })

  it('should render student select', () => {
    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={jest.fn()}
        onCancel={jest.fn()}
      />
    )

    expect(screen.getByRole('combobox')).toBeInTheDocument()
  })

  it('should disable button when no students available', () => {
    render(
      <EnrollmentForm
        students={[]}
        activity={mockActivity}
        onSuccess={jest.fn()}
        onCancel={jest.fn()}
      />
    )

    const button = screen.getByRole('button', { name: /siguiente/i })
    expect(button).toBeDisabled()
  })

  it('should show activity spots information', () => {
    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={jest.fn()}
        onCancel={jest.fn()}
      />
    )

    expect(screen.getByText(/15.*20/)).toBeInTheDocument()
  })

  it('should call onCancel when cancel button is clicked', async () => {
    const mockOnCancel = jest.fn()

    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={jest.fn()}
        onCancel={mockOnCancel}
      />
    )

    const cancelButton = screen.getByRole('button', { name: /cancelar/i })
    await userEvent.click(cancelButton)

    expect(mockOnCancel).toHaveBeenCalled()
  })

  it('should have next button', async () => {
    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={jest.fn()}
        onCancel={jest.fn()}
      />
    )

    const nextButton = screen.getByRole('button', { name: /siguiente/i })
    expect(nextButton).toBeInTheDocument()
  })

  it('should support multiple students', () => {
    render(
      <EnrollmentForm
        students={[mockStudent, mockStudent2]}
        activity={mockActivity}
        onSuccess={jest.fn()}
        onCancel={jest.fn()}
      />
    )

    const select = screen.getByRole('combobox')
    expect(select).toBeInTheDocument()
  })

  it('should display activity name in title', () => {
    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={jest.fn()}
        onCancel={jest.fn()}
      />
    )

    const texts = screen.getAllByText(/Football/)
    expect(texts.length).toBeGreaterThan(0)
  })

  it('should show all required elements', () => {
    render(
      <EnrollmentForm
        students={[mockStudent]}
        activity={mockActivity}
        onSuccess={jest.fn()}
        onCancel={jest.fn()}
      />
    )

    expect(screen.getByRole('combobox')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /siguiente/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /cancelar/i })).toBeInTheDocument()
  })
})
