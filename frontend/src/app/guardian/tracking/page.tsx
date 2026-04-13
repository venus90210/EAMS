'use client'

export const dynamic = 'force-dynamic'

import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useTracking } from '@/hooks/useTracking'
import { authService } from '@/services/authService'

export default function TrackingPage() {
  const router = useRouter()
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const { data, loading } = useTracking()

  if (authLoading) {
    return <div className="flex items-center justify-center min-h-screen"><p>Cargando...</p></div>
  }

  // If not authenticated in context, but we have a token stored, wait for context to update
  if (!isAuthenticated && authService.getAccessToken()) {
    return <div className="flex items-center justify-center min-h-screen"><p>Cargando sesión...</p></div>
  }

  if (!isAuthenticated || user?.role !== 'GUARDIAN') {
    router.push('/login')
    return null
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold">Seguimiento de inscripciones</h1>
          <button
            onClick={() => router.push('/guardian/activities')}
            className="text-gray-600 hover:text-gray-900 font-medium"
          >
            ← Volver a actividades
          </button>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-8">
        {loading ? (
          <div className="text-center py-12"><p className="text-gray-600">Cargando seguimiento...</p></div>
        ) : data.length === 0 ? (
          <div className="text-center py-12"><p className="text-gray-600">No tienes inscripciones aún</p></div>
        ) : (
          <div className="space-y-8">
            {data.map(tracking => (
              <div key={tracking.studentId} className="bg-white rounded-lg shadow overflow-hidden">
                <div className="bg-blue-50 px-6 py-4">
                  <h2 className="text-xl font-bold text-blue-900">{tracking.studentName}</h2>
                </div>

                <div className="p-6">
                  {tracking.enrollments.length === 0 ? (
                    <p className="text-gray-500">Sin inscripciones</p>
                  ) : (
                    <div className="space-y-4">
                      {tracking.enrollments.map(enrollment => {
                        const attendanceRecords = tracking.attendance.filter(
                          a => a.enrollmentId === enrollment.id
                        )
                        const presentCount = attendanceRecords.filter(a => a.present).length

                        return (
                          <div
                            key={enrollment.id}
                            className="border rounded-lg p-4 bg-gray-50"
                          >
                            <div className="flex justify-between items-start mb-2">
                              <h3 className="text-lg font-semibold text-gray-900">
                                {enrollment.activityName}
                              </h3>
                              <span className={`px-2 py-1 rounded text-sm font-medium ${
                                enrollment.status === 'ACTIVE'
                                  ? 'bg-green-100 text-green-800'
                                  : enrollment.status === 'COMPLETED'
                                  ? 'bg-blue-100 text-blue-800'
                                  : 'bg-red-100 text-red-800'
                              }`}>
                                {enrollment.status}
                              </span>
                            </div>

                            <p className="text-sm text-gray-600">
                              Inscrito: {new Date(enrollment.enrolledAt).toLocaleDateString('es-ES')}
                            </p>

                            {attendanceRecords.length > 0 && (
                              <div className="mt-3 pt-3 border-t">
                                <p className="text-sm font-medium text-gray-700">
                                  Asistencia: {presentCount}/{attendanceRecords.length} presentes
                                </p>
                                <div className="mt-2 flex flex-wrap gap-2">
                                  {attendanceRecords.map(record => (
                                    <div
                                      key={record.id}
                                      className={`text-xs px-2 py-1 rounded ${
                                        record.present
                                          ? 'bg-green-100 text-green-800'
                                          : 'bg-gray-100 text-gray-800'
                                      }`}
                                    >
                                      {new Date(record.date).toLocaleDateString('es-ES')}:{' '}
                                      {record.present ? '✓' : '✗'}
                                    </div>
                                  ))}
                                </div>
                              </div>
                            )}
                          </div>
                        )
                      })}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  )
}
