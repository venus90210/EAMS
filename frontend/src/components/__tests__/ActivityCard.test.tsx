import { render, screen, fireEvent } from '@testing-library/react'
import { ActivityCard } from '../activities/ActivityCard'
import { Activity } from '@/types'

const mockActivity: Activity = {
  id: 'activity1',
  name: 'Fútbol Juvenil',
  description: 'Actividad de fútbol para estudiantes',
  totalSpots: 20,
  availableSpots: 5,
  status: 'PUBLISHED',
  schedule: {
    dayOfWeek: 'MONDAY',
    startTime: '16:00:00',
    endTime: '18:00:00',
  },
  institutionId: 'inst1',
  createdAt: new Date(),
  updatedAt: new Date(),
}

describe('ActivityCard', () => {
  it('should render activity information', () => {
    render(<ActivityCard activity={mockActivity} />)

    expect(screen.getByText('Fútbol Juvenil')).toBeInTheDocument()
    expect(screen.getByText(/Actividad de fútbol/)).toBeInTheDocument()
    expect(screen.getByText(/✓ 5 cupos/)).toBeInTheDocument()
  })

  it('should show schedule information', () => {
    render(<ActivityCard activity={mockActivity} />)

    expect(screen.getByText(/MONDAY/)).toBeInTheDocument()
    expect(screen.getByText(/16:00:00/)).toBeInTheDocument()
  })

  it('should render enroll button when online', () => {
    render(<ActivityCard activity={mockActivity} offlineMode={false} />)

    const button = screen.getAllByRole('button')[0]
    expect(button).toBeInTheDocument()
    expect(button).not.toBeDisabled()
  })

  it('should disable enroll button in offline mode', () => {
    render(<ActivityCard activity={mockActivity} offlineMode={true} />)

    const button = screen.getAllByRole('button')[0]
    expect(button).toBeInTheDocument()
    expect(button).toBeDisabled()
  })

  it('should disable enroll button when no spots available', () => {
    const noSpotsActivity = { ...mockActivity, availableSpots: 0 }
    render(<ActivityCard activity={noSpotsActivity} offlineMode={false} />)

    const button = screen.getAllByRole('button')[0]
    expect(button).toBeInTheDocument()
    expect(button).toBeDisabled()
  })

  it('should show offline warning in offline mode', () => {
    render(<ActivityCard activity={mockActivity} offlineMode={true} />)

    expect(screen.getByText(/No disponible sin conexión/i)).toBeInTheDocument()
  })

  it('should call onEnroll callback when button clicked', () => {
    const mockOnEnroll = jest.fn()
    render(<ActivityCard activity={mockActivity} onEnroll={mockOnEnroll} />)

    fireEvent.click(screen.getByRole('button', { name: /Inscribirse/i }))

    expect(mockOnEnroll).toHaveBeenCalledWith('activity1')
  })

  it('should show loading state', () => {
    render(<ActivityCard activity={mockActivity} loading={true} />)

    expect(screen.getByRole('button')).toHaveTextContent('⏳ Inscribiendo...')
  })

  it('should show "Sin cupos" text when no spots available', () => {
    const noSpotsActivity = { ...mockActivity, availableSpots: 0 }
    render(<ActivityCard activity={noSpotsActivity} />)

    expect(screen.getByRole('button')).toHaveTextContent('Sin cupos')
  })

  it('should display correct spot color when available', () => {
    const { container } = render(<ActivityCard activity={mockActivity} />)

    const spotBadge = container.querySelector('.bg-green-100')
    expect(spotBadge).toBeInTheDocument()
  })

  it('should display correct spot color when no spots', () => {
    const noSpotsActivity = { ...mockActivity, availableSpots: 0 }
    const { container } = render(<ActivityCard activity={noSpotsActivity} />)

    const spotBadge = container.querySelector('.bg-red-100')
    expect(spotBadge).toBeInTheDocument()
  })

  it('should not render description if not provided', () => {
    const noDescActivity = { ...mockActivity, description: undefined }
    const { container } = render(<ActivityCard activity={noDescActivity} />)

    const paragraphs = container.querySelectorAll('p')
    expect(paragraphs.length).toBeLessThan(3) // Should not have description paragraph
  })
})
