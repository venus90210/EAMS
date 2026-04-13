'use client'

export const dynamic = 'force-dynamic'

import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useActivities } from '@/hooks/useActivities'
import { useOfflineStatus } from '@/hooks/useOfflineStatus'
import { ActivityCard } from '@/components/activities/ActivityCard'
import { authService } from '@/services/authService'

export default function ActivitiesPage() {
  const router = useRouter()
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const { activities, loading, error, fromCache } = useActivities()
  const { isOnline } = useOfflineStatus()

  if (authLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-gray-600">Cargando...</p>
      </div>
    )
  }

  // If not authenticated in context, but we have a token stored, wait for context to update
  if (!isAuthenticated && authService.getAccessToken()) {
    console.log('[ActivitiesPage] Token exists but context not updated yet, waiting...')
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p className="text-gray-600">Cargando sesión...</p>
      </div>
    )
  }

  if (!isAuthenticated || user?.role !== 'GUARDIAN') {
    console.log('[ActivitiesPage] Not authenticated or wrong role, redirecting to login')
    router.push('/login')
    return null
  }

  const handleEnroll = (activityId: string) => {
    router.push(`/guardian/enroll?activityId=${activityId}`)
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--background)' }}>
      {/* Header */}
      <header style={{ backgroundColor: 'var(--surface)', borderBottom: `1px solid var(--border)` }}>
        <div className="max-w-6xl mx-auto px-4 py-6 flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold" style={{ color: 'var(--text)' }}>
              🎯 Actividades Disponibles
            </h1>
            <p style={{ color: 'var(--muted)' }} className="mt-1">
              Explora y participa en nuestras actividades
            </p>
            {fromCache && (
              <p className="text-sm font-medium mt-2" style={{ color: 'var(--accent)' }}>
                📴 Datos en caché (sin conexión)
              </p>
            )}
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-6xl mx-auto px-4 py-8">
        {error && (
          <div className="mb-6 p-4 rounded-lg" style={{ backgroundColor: '#fee', borderLeft: `4px solid var(--error, #dc2626)` }}>
            <p style={{ color: '#991b1b' }} className="font-medium">
              ⚠️ {error}
            </p>
          </div>
        )}

        {loading && !activities.length && (
          <div className="text-center py-12">
            <p style={{ color: 'var(--muted)' }}>Cargando actividades...</p>
          </div>
        )}

        {activities.length === 0 && !loading && (
          <div className="text-center py-12">
            <p style={{ color: 'var(--muted)' }}>No hay actividades disponibles en este momento.</p>
          </div>
        )}

        {activities.length > 0 && (
          <>
            <div className="mb-8">
              <p style={{ color: 'var(--muted)' }} className="text-sm">
                Se encontraron <span className="font-bold" style={{ color: 'var(--primary)' }}>{activities.length}</span> actividad{activities.length !== 1 ? 'es' : ''}
                {!isOnline && ' (sin conexión)'}
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {activities.map((activity) => (
                <ActivityCard
                  key={activity.id}
                  activity={activity}
                  onEnroll={handleEnroll}
                  offlineMode={!isOnline}
                />
              ))}
            </div>
          </>
        )}

        {/* Navigation */}
        <div className="mt-12 pt-8" style={{ borderTop: `1px solid var(--border)` }}>
          <button
            onClick={() => router.push('/guardian/tracking')}
            className="btn-secondary"
          >
            Ver mis inscripciones →
          </button>
        </div>
      </main>
    </div>
  )
}
