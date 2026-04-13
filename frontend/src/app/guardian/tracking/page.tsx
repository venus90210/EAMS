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
    <div className="min-h-screen" style={{ backgroundColor: 'var(--background)' }}>
      <header style={{ backgroundColor: 'var(--surface)', borderBottom: `1px solid var(--border)` }}>
        <div className="max-w-6xl mx-auto px-4 py-6 flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold" style={{ color: 'var(--text)' }}>
              Seguimiento de inscripciones
            </h1>
            <p style={{ color: 'var(--muted)' }} className="mt-1">
              Monitorea el progreso de tus estudiantes
            </p>
          </div>
          <button
            onClick={() => router.push('/guardian/activities')}
            className="btn-secondary"
          >
            ← Volver
          </button>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-8">
        {loading ? (
          <div className="text-center py-12">
            <p style={{ color: 'var(--muted)' }}>Cargando seguimiento...</p>
          </div>
        ) : data.length === 0 ? (
          <div className="text-center py-12">
            <p style={{ color: 'var(--muted)' }}>No tienes inscripciones aún</p>
          </div>
        ) : (
          <div className="space-y-6">
            {data.map(tracking => (
              <div
                key={tracking.studentId}
                style={{ backgroundColor: 'var(--surface)', borderRadius: 'var(--radius-lg)', border: `1px solid var(--border)` }}
                className="overflow-hidden hover:shadow-md transition-shadow"
              >
                <div
                  style={{ backgroundColor: 'var(--primary)', backgroundImage: 'linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%)' }}
                  className="px-6 py-8"
                >
                  <div className="flex items-center gap-4">
                    <div
                      style={{ backgroundColor: 'rgba(255, 255, 255, 0.2)' }}
                      className="w-16 h-16 rounded-full flex items-center justify-center"
                    >
                      <span className="text-2xl">👤</span>
                    </div>
                    <div>
                      <p style={{ color: 'rgba(255, 255, 255, 0.8)' }} className="text-sm uppercase font-medium">
                        Estudiante
                      </p>
                      <h2 className="text-3xl font-bold text-white">{tracking.studentName}</h2>
                    </div>
                  </div>
                </div>

                <div className="p-6">
                  {tracking.enrollments.length === 0 ? (
                    <p style={{ color: 'var(--muted)' }}>Sin inscripciones registradas</p>
                  ) : (
                    <div className="space-y-4">
                      {tracking.enrollments.map(enrollment => {
                        const attendanceRecords = tracking.attendance.filter(
                          a => a.enrollmentId === enrollment.id
                        )
                        const presentCount = attendanceRecords.filter(a => a.present).length
                        const attendanceRate = attendanceRecords.length > 0
                          ? Math.round((presentCount / attendanceRecords.length) * 100)
                          : 0

                        return (
                          <div
                            key={enrollment.id}
                            style={{
                              backgroundColor: 'var(--background)',
                              borderRadius: 'var(--radius-md)',
                              border: `1px solid var(--border)`
                            }}
                            className="p-5 hover:shadow-sm transition-shadow"
                          >
                            <div className="flex justify-between items-start mb-4">
                              <div className="flex-1">
                                <h3 className="text-lg font-bold" style={{ color: 'var(--text)' }}>
                                  {enrollment.activityName}
                                </h3>
                              </div>
                              <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                                enrollment.status === 'ACTIVE'
                                  ? 'bg-green-100 text-green-700'
                                  : enrollment.status === 'COMPLETED'
                                  ? 'bg-blue-100 text-blue-700'
                                  : 'bg-red-100 text-red-700'
                              }`}>
                                {enrollment.status === 'ACTIVE' && '🟢 Activo'}
                                {enrollment.status === 'COMPLETED' && '✓ Completado'}
                                {enrollment.status === 'CANCELLED' && '✕ Cancelado'}
                              </span>
                            </div>

                            <div className="grid grid-cols-2 gap-4 mb-4">
                              <div>
                                <p style={{ color: 'var(--muted)' }} className="text-xs font-medium uppercase">
                                  Fecha de inscripción
                                </p>
                                <p style={{ color: 'var(--text)' }} className="font-semibold mt-1">
                                  {new Date(enrollment.enrolledAt).toLocaleDateString('es-ES', {
                                    year: 'numeric',
                                    month: 'long',
                                    day: 'numeric'
                                  })}
                                </p>
                              </div>
                              <div>
                                <p style={{ color: 'var(--muted)' }} className="text-xs font-medium uppercase">
                                  Estado de asistencia
                                </p>
                                <p style={{ color: 'var(--text)' }} className="font-semibold mt-1">
                                  {attendanceRecords.length === 0
                                    ? 'Sin registros'
                                    : `${presentCount}/${attendanceRecords.length} (${attendanceRate}%)`
                                  }
                                </p>
                              </div>
                            </div>

                            {attendanceRecords.length > 0 && (
                              <div className="pt-4" style={{ borderTop: `1px solid var(--border)` }}>
                                <p style={{ color: 'var(--text)' }} className="font-semibold mb-3">
                                  Registro de asistencias
                                </p>
                                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                                  {attendanceRecords.map(record => (
                                    <div
                                      key={record.id}
                                      className={`text-xs p-2 rounded flex items-center justify-between ${
                                        record.present
                                          ? 'bg-green-50 text-green-700 border border-green-200'
                                          : 'bg-red-50 text-red-700 border border-red-200'
                                      }`}
                                    >
                                      <span>{new Date(record.date).toLocaleDateString('es-ES', { month: 'short', day: 'numeric' })}</span>
                                      <span className="font-bold">
                                        {record.present ? '✓' : '✗'}
                                      </span>
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
