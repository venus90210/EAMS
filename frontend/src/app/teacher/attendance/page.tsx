'use client'

export const dynamic = 'force-dynamic'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useAttendanceSessions } from '@/hooks/useAttendanceSessions'
import { useActivities } from '@/hooks/useActivities'
import { AttendanceList } from '@/components/attendance/AttendanceList'
import { authService } from '@/services/authService'

export default function AttendancePage() {
  const router = useRouter()
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const { activities } = useActivities()
  const { session, loading, error, openSession, recordAttendance } = useAttendanceSessions()

  const [selectedActivityId, setSelectedActivityId] = useState('')
  const [message, setMessage] = useState('')

  if (authLoading) {
    return <div className="flex items-center justify-center min-h-screen"><p>Cargando...</p></div>
  }

  // If not authenticated in context, but we have a token stored, wait for context to update
  if (!isAuthenticated && authService.getAccessToken()) {
    return <div className="flex items-center justify-center min-h-screen"><p>Cargando sesión...</p></div>
  }

  if (!isAuthenticated || user?.role !== 'TEACHER') {
    router.push('/login')
    return null
  }

  const handleOpenSession = async () => {
    if (!selectedActivityId) {
      setMessage('Selecciona una actividad')
      return
    }

    try {
      await openSession(selectedActivityId)
      setMessage('')
    } catch (err) {
      setMessage(error || 'Error al abrir sesión')
    }
  }

  const publishedActivities = activities.filter(a => a.status === 'PUBLISHED')

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--background)' }}>
      <header style={{ backgroundColor: 'var(--surface)', borderBottom: `1px solid var(--border)` }}>
        <div className="max-w-6xl mx-auto px-4 py-6 flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold" style={{ color: 'var(--text)' }}>
              📋 Registro de asistencia
            </h1>
            <p style={{ color: 'var(--muted)' }} className="mt-1">
              Controla la asistencia de tus estudiantes
            </p>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-8">
        {!session ? (
          <div
            style={{
              backgroundColor: 'var(--surface)',
              borderRadius: 'var(--radius-lg)',
              border: `1px solid var(--border)`,
            }}
            className="p-8 shadow-sm"
          >
            <h2 className="text-2xl font-bold mb-6" style={{ color: 'var(--text)' }}>
              🎯 Abrir sesión de asistencia
            </h2>

            <div className="mb-6">
              <label style={{ color: 'var(--text)' }} className="block text-sm font-semibold mb-3">
                Selecciona la actividad
              </label>
              <select
                value={selectedActivityId}
                onChange={e => setSelectedActivityId(e.target.value)}
                className="input-field w-full"
              >
                <option value="">-- Selecciona una actividad --</option>
                {publishedActivities.map(activity => (
                  <option key={activity.id} value={activity.id}>
                    {activity.name}
                  </option>
                ))}
              </select>
            </div>

            {message && (
              <div
                className="mb-6 p-4 rounded-lg font-medium"
                style={{
                  backgroundColor: message.includes('Error') ? '#fee' : '#fef3c7',
                  color: message.includes('Error') ? '#991b1b' : '#92400e',
                  border: `1px solid ${message.includes('Error') ? '#fca5a5' : '#fde68a'}`,
                }}
              >
                {message.includes('Error') ? '⚠️ ' : '📌 '}
                {message}
              </div>
            )}

            <button
              onClick={handleOpenSession}
              disabled={loading || !selectedActivityId}
              className={`w-full btn-primary py-3 ${loading || !selectedActivityId ? 'opacity-50 cursor-not-allowed' : ''}`}
            >
              {loading ? '⏳ Abriendo sesión...' : '✓ Abrir sesión de hoy'}
            </button>
          </div>
        ) : (
          <div className="space-y-8">
            <div
              className="rounded-lg p-6"
              style={{
                backgroundColor: '#dcfce7',
                borderLeft: `4px solid var(--accent)`,
              }}
            >
              <h2 className="text-lg font-bold" style={{ color: '#166534' }}>
                ✓ Sesión abierta para {activities.find(a => a.id === session.activityId)?.name}
              </h2>
              <p style={{ color: '#15803d' }} className="text-sm mt-2">
                📅 {new Date(session.date).toLocaleDateString('es-ES', {
                  weekday: 'long',
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric'
                })}
              </p>
            </div>

            <div>
              <h3 className="text-xl font-bold mb-4" style={{ color: 'var(--text)' }}>
                👥 Estudiantes inscritos ({session.students.length})
              </h3>
              <AttendanceList
                students={session.students}
                sessionId={session.id}
                onRecordAttendance={recordAttendance}
                isLoading={loading}
              />
            </div>

            <button
              onClick={() => router.push('/teacher/attendance')}
              className="w-full btn-secondary py-3"
            >
              ✕ Cerrar sesión
            </button>
          </div>
        )}
      </main>
    </div>
  )
}
