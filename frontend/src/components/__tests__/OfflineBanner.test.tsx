import { render, screen, waitFor } from '@testing-library/react'
import { OfflineBanner } from '../shared/OfflineBanner'
import { useOfflineStatus } from '@/hooks/useOfflineStatus'

jest.mock('@/hooks/useOfflineStatus')

describe('OfflineBanner', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should not render when online and cache is fresh', () => {
    ;(useOfflineStatus as jest.Mock).mockReturnValue({
      isOnline: true,
      cacheAge: 1000,
      cacheExpired: false,
    })

    const { container } = render(<OfflineBanner />)
    expect(container.firstChild).toBeNull()
  })

  it('should render offline banner when offline', () => {
    ;(useOfflineStatus as jest.Mock).mockReturnValue({
      isOnline: false,
      cacheAge: 5000,
      cacheExpired: false,
    })

    render(<OfflineBanner />)

    expect(screen.getByText(/Modo offline/i)).toBeInTheDocument()
  })

  it('should show cache age in minutes', () => {
    const fifteenMinutesInMs = 15 * 60 * 1000

    ;(useOfflineStatus as jest.Mock).mockReturnValue({
      isOnline: false,
      cacheAge: fifteenMinutesInMs,
      cacheExpired: false,
    })

    render(<OfflineBanner />)

    expect(screen.getByText(/15m/i)).toBeInTheDocument()
  })

  it('should show cache age in hours and minutes', () => {
    const twoHoursFifteenMinutesInMs = (2 * 60 + 15) * 60 * 1000

    ;(useOfflineStatus as jest.Mock).mockReturnValue({
      isOnline: false,
      cacheAge: twoHoursFifteenMinutesInMs,
      cacheExpired: false,
    })

    render(<OfflineBanner />)

    expect(screen.getByText(/2h 15m/i)).toBeInTheDocument()
  })

  it('should render expired cache warning', () => {
    ;(useOfflineStatus as jest.Mock).mockReturnValue({
      isOnline: true,
      cacheAge: 49 * 60 * 60 * 1000, // 49 hours
      cacheExpired: true,
    })

    render(<OfflineBanner />)

    expect(screen.getByText(/más de 48 horas/i)).toBeInTheDocument()
  })

  it('should render with red background when cache expired', () => {
    ;(useOfflineStatus as jest.Mock).mockReturnValue({
      isOnline: true,
      cacheAge: 49 * 60 * 60 * 1000,
      cacheExpired: true,
    })

    const { container } = render(<OfflineBanner />)
    const banner = container.querySelector('div[class*="bg-red-600"]')

    expect(banner).toBeInTheDocument()
  })

  it('should render with yellow background when offline', () => {
    ;(useOfflineStatus as jest.Mock).mockReturnValue({
      isOnline: false,
      cacheAge: 5000,
      cacheExpired: false,
    })

    const { container } = render(<OfflineBanner />)
    const banner = container.querySelector('div[class*="bg-yellow-600"]')

    expect(banner).toBeInTheDocument()
  })

  it('should handle unknown cache age gracefully', () => {
    ;(useOfflineStatus as jest.Mock).mockReturnValue({
      isOnline: false,
      cacheAge: null,
      cacheExpired: false,
    })

    render(<OfflineBanner />)

    expect(screen.getByText(/desconocida/i)).toBeInTheDocument()
  })
})
