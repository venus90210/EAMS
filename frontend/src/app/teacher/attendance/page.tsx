'use client'

export const dynamic = 'force-dynamic'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useAttendanceSessions } from '@/hooks/useAttendanceSessions'
import { useActivities } from '@/hooks/useActivities'
import { AttendanceList } from '@/components/attendance/AttendanceList'

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
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold">Registro de asistencia</h1>
          <button
            onClick={() => router.push('/')}
            className="text-gray-600 hover:text-gray-900 font-medium"
          >
            Inicio
          </button>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-8">
        {!session ? (
          <div className="bg-white rounded-lg shadow p-8">
            <h2 className="text-xl font-bold mb-6">Abrir sesión de asistencia</h2>

            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Selecciona actividad
              </label>
              <select
                value={selectedActivityId}
                onChange={e => setSelectedActivityId(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
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
              <div className={`mb-4 p-4 rounded ${message.includes('Error') ? 'bg-red-100 text-red-700' : 'bg-yellow-100 text-yellow-700'}`}>
                {message}
              </div>
            )}

            <button
              onClick={handleOpenSession}
              disabled={loading || !selectedActivityId}
              className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 disabled:bg-gray-400 font-medium"
            >
              {loading ? 'Abriendo sesión...' : 'Abrir sesión de hoy'}
            </button>
          </div>
        ) : (
          <div className="space-y-8">
            <div className="bg-green-50 border border-green-200 rounded-lg p-4">
              <h2 className="text-lg font-bold text-green-900">
                Sesión abierta para {activities.find(a => a.id === session.activityId)?.name}
              </h2>
              <p className="text-sm text-green-700">
                Fecha: {new Date(session.date).toLocaleDateString('es-ES')}
              </p>
            </div>

            <div>
              <h3 className="text-lg font-bold mb-4">Estudiantes inscritos ({session.students.length})</h3>
              <AttendanceList
                students={session.students}
                sessionId={session.id}
                onRecordAttendance={recordAttendance}
                isLoading={loading}
              />
            </div>

            <button
              onClick={() => router.push('/teacher/attendance')}
              className="w-full bg-gray-600 text-white py-2 px-4 rounded-lg hover:bg-gray-700 font-medium"
            >
              Cerrar sesión
            </button>
          </div>
        )}
      </main>
    </div>
  )
}
