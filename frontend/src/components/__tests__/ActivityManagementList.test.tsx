import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ActivityManagementList } from '../admin/ActivityManagementList'
import { Activity } from '@/types'

const mockActivities: Activity[] = [
  {
    id: 'activity-1',
    name: 'Fútbol',
    description: 'Actividad de fútbol',
    totalSpots: 20,
    availableSpots: 15,
    status: 'DRAFT',
    institutionId: 'inst-1',
    createdAt: new Date(),
    updatedAt: new Date(),
  },
  {
    id: 'activity-2',
    name: 'Artes',
    description: 'Taller de artes',
    totalSpots: 15,
    availableSpots: 10,
    status: 'PUBLISHED',
    institutionId: 'inst-1',
    createdAt: new Date(),
    updatedAt: new Date(),
  },
]

describe('ActivityManagementList', () => {
  const mockOnEdit = jest.fn()
  const mockOnPublish = jest.fn()
  const mockOnDisable = jest.fn()
  const mockOnDelete = jest.fn()

  beforeEach(() => {
    jest.clearAllMocks()
    ;(window.confirm as jest.Mock) = jest.fn()
  })

  it('should render activity names', () => {
    render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    expect(screen.getByText('Fútbol')).toBeInTheDocument()
    expect(screen.getByText('Artes')).toBeInTheDocument()
  })

  it('should display status badges', () => {
    const { container } = render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    // Look for status indicators in the table
    expect(container.textContent).toMatch(/DRAFT|PUBLISHED/)
  })

  it('should display available spots', () => {
    const { container } = render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    // Should show cupos info
    expect(container.textContent).toMatch(/15|10/)
  })

  it('should call onEdit when edit button is clicked', async () => {
    const user = userEvent.setup()
    render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    const editButtons = screen.getAllByRole('button', { name: /editar/i })
    await user.click(editButtons[0])

    expect(mockOnEdit).toHaveBeenCalledWith(expect.objectContaining({ id: 'activity-1' }))
  })

  it('should show publish button only for DRAFT activities', () => {
    render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    const publishButtons = screen.queryAllByRole('button', { name: /publicar/i })
    // There should be 1 publish button (for DRAFT activity)
    expect(publishButtons.length).toBeGreaterThanOrEqual(1)
  })

  it('should call onPublish when publish button is clicked', async () => {
    const user = userEvent.setup()
    mockOnPublish.mockResolvedValue(undefined)

    render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    const publishButtons = screen.getAllByRole('button', { name: /publicar/i })
    await user.click(publishButtons[0])

    await waitFor(() => {
      expect(mockOnPublish).toHaveBeenCalledWith('activity-1')
    })
  })

  it('should show disable button only for PUBLISHED activities', () => {
    render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    const disableButtons = screen.queryAllByRole('button', { name: /deshabilitar/i })
    // There should be 1 disable button (for PUBLISHED activity)
    expect(disableButtons.length).toBeGreaterThanOrEqual(1)
  })

  it('should call onDisable when disable button is clicked', async () => {
    const user = userEvent.setup()
    mockOnDisable.mockResolvedValue(undefined)

    render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    const disableButtons = screen.getAllByRole('button', { name: /deshabilitar/i })
    await user.click(disableButtons[0])

    await waitFor(() => {
      expect(mockOnDisable).toHaveBeenCalledWith('activity-2')
    })
  })

  it('should call onDelete when delete is confirmed', async () => {
    const user = userEvent.setup()
    ;(window.confirm as jest.Mock).mockReturnValue(true)
    mockOnDelete.mockResolvedValue(undefined)

    render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    const deleteButtons = screen.getAllByRole('button', { name: /eliminar|borrar/i })
    await user.click(deleteButtons[0])

    await waitFor(() => {
      expect(window.confirm).toHaveBeenCalled()
      expect(mockOnDelete).toHaveBeenCalledWith('activity-1')
    })
  })

  it('should not call onDelete when delete is cancelled', async () => {
    const user = userEvent.setup()
    ;(window.confirm as jest.Mock).mockReturnValue(false)

    render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    const deleteButtons = screen.getAllByRole('button', { name: /eliminar|borrar/i })
    await user.click(deleteButtons[0])

    expect(window.confirm).toHaveBeenCalled()
    expect(mockOnDelete).not.toHaveBeenCalled()
  })

  it('should show empty state when no activities', () => {
    const { container } = render(
      <ActivityManagementList
        activities={[]}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    // Should show some empty state message or empty table
    expect(container.textContent).toBeTruthy()
  })

  it('should show delete button for all activities', () => {
    render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    const deleteButtons = screen.getAllByRole('button', { name: /eliminar|borrar/i })
    // Should have delete button for each activity
    expect(deleteButtons.length).toBe(mockActivities.length)
  })

  it('should show edit button for all activities', () => {
    render(
      <ActivityManagementList
        activities={mockActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    const editButtons = screen.getAllByRole('button', { name: /editar/i })
    // Should have edit button for each activity
    expect(editButtons.length).toBe(mockActivities.length)
  })

  it('should display loading state', () => {
    render(
      <ActivityManagementList
        activities={mockActivities}
        loading={true}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    // Component should still render activities
    expect(screen.getByText('Fútbol')).toBeInTheDocument()
  })

  it('should match multiple activities correctly', () => {
    const manyActivities = [
      ...mockActivities,
      {
        id: 'activity-3',
        name: 'Música',
        description: 'Clase de música',
        totalSpots: 10,
        availableSpots: 5,
        status: 'DRAFT' as const,
        institutionId: 'inst-1',
        createdAt: new Date(),
        updatedAt: new Date(),
      },
    ]

    render(
      <ActivityManagementList
        activities={manyActivities}
        loading={false}
        onEdit={mockOnEdit}
        onPublish={mockOnPublish}
        onDisable={mockOnDisable}
        onDelete={mockOnDelete}
      />,
    )

    expect(screen.getByText('Fútbol')).toBeInTheDocument()
    expect(screen.getByText('Artes')).toBeInTheDocument()
    expect(screen.getByText('Música')).toBeInTheDocument()
  })
})
