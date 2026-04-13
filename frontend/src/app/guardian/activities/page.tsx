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
  const { user, isAuthenticated, loading: authLoading, logout } = useAuth()
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
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Actividades Disponibles</h1>
            {fromCache && (
              <p className="text-sm text-yellow-600">📴 Datos del caché (modo offline)</p>
            )}
          </div>
          <button
            onClick={() => logout()}
            className="text-gray-600 hover:text-gray-900 font-medium"
          >
            Cerrar sesión
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-6xl mx-auto px-4 py-8">
        {error && (
          <div className="mb-6 p-4 bg-red-100 border border-red-400 text-red-700 rounded">
            {error}
          </div>
        )}

        {loading && !activities.length && (
          <div className="text-center py-12">
            <p className="text-gray-600">Cargando actividades...</p>
          </div>
        )}

        {activities.length === 0 && !loading && (
          <div className="text-center py-12">
            <p className="text-gray-600">No hay actividades disponibles en este momento.</p>
          </div>
        )}

        {activities.length > 0 && (
          <>
            <div className="mb-6">
              <p className="text-gray-600">
                Encontramos {activities.length} actividad{activities.length !== 1 ? 'es' : ''}
                {!isOnline ? ' (offline)' : ''}
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
        <div className="mt-12 pt-8 border-t border-gray-200">
          <div className="flex gap-4">
            <button
              onClick={() => router.push('/guardian/tracking')}
              className="text-blue-600 hover:text-blue-800 font-medium"
            >
              Ver inscripciones →
            </button>
          </div>
        </div>
      </main>
    </div>
  )
}
