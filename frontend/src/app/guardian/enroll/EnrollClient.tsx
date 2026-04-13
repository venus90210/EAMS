'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useActivities } from '@/hooks/useActivities'
import { useEnrollment } from '@/hooks/useEnrollment'
import { useStudents } from '@/hooks/useStudents'
import { authService } from '@/services/authService'

export function EnrollClient() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const { students, loading: studentsLoading, error: studentsError } = useStudents()
  const { enroll, loading: enrollLoading, error: enrollError } = useEnrollment()
  const { activities } = useActivities()

  const [selectedStudentId, setSelectedStudentId] = useState('')
  const [message, setMessage] = useState('')

  const activityId = searchParams.get('activityId')
  const selectedActivity = activities.find(a => a.id === activityId)

  console.log('[EnrollClient] State:', { authLoading, isAuthenticated, userId: user?.id, students: students.length, studentsLoading, studentsError })

  if (authLoading) {
    return <div className="flex items-center justify-center min-h-screen"><p style={{ color: 'var(--muted)' }}>Cargando...</p></div>
  }

  // If not authenticated in context, but we have a token stored, wait for context to update
  if (!isAuthenticated && authService.getAccessToken()) {
    return <div className="flex items-center justify-center min-h-screen"><p style={{ color: 'var(--muted)' }}>Cargando sesión...</p></div>
  }

  if (!isAuthenticated || user?.role !== 'GUARDIAN') {
    router.push('/login')
    return null
  }

  if (!activityId || !selectedActivity) {
    return (
      <div className="min-h-screen" style={{ backgroundColor: 'var(--background)' }}>
        <div className="max-w-md mx-auto p-8">
          <div className="p-4 rounded-lg" style={{ backgroundColor: '#fee', borderLeft: `4px solid #dc2626` }}>
            <p style={{ color: '#991b1b' }} className="font-medium">Actividad no encontrada</p>
          </div>
          <button
            onClick={() => router.push('/guardian/activities')}
            className="mt-4 w-full btn-secondary"
          >
            Volver a actividades
          </button>
        </div>
      </div>
    )
  }

  const handleEnroll = async () => {
    if (!selectedStudentId) {
      setMessage('Por favor selecciona un estudiante')
      return
    }

    try {
      await enroll(selectedStudentId, activityId)
      setMessage('¡Inscripción exitosa!')
      setTimeout(() => router.push('/guardian/tracking'), 2000)
    } catch (err) {
      setMessage(enrollError?.message || 'Error al inscribirse')
    }
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--background)' }}>
      <div className="max-w-2xl mx-auto px-4 py-12">
        <div
          style={{
            backgroundColor: 'var(--surface)',
            borderRadius: 'var(--radius-lg)',
            border: `1px solid var(--border)`,
          }}
          className="p-8 shadow-sm"
        >
          <h1 className="text-3xl font-bold mb-8" style={{ color: 'var(--text)' }}>
            📝 Inscribir estudiante
          </h1>

          {/* Paso 1 */}
          <div
            className="mb-8 p-6 rounded-lg"
            style={{
              backgroundColor: 'var(--background)',
              border: `1px solid var(--border)`,
            }}
          >
            <h2 className="text-lg font-bold mb-4" style={{ color: 'var(--text)' }}>
              Paso 1: Selecciona el estudiante
            </h2>
            {studentsLoading ? (
              <p style={{ color: 'var(--muted)' }}>Cargando estudiantes...</p>
            ) : studentsError ? (
              <p style={{ color: '#991b1b' }}>Error al cargar estudiantes: {studentsError}</p>
            ) : students.length === 0 ? (
              <p style={{ color: '#991b1b' }}>No tienes estudiantes asociados</p>
            ) : (
              <div className="space-y-3">
                {students.map(student => (
                  <label
                    key={student.id}
                    className="flex items-center p-3 rounded cursor-pointer transition"
                    style={{
                      backgroundColor: selectedStudentId === student.id ? 'var(--primary)' : 'var(--surface)',
                      color: selectedStudentId === student.id ? 'white' : 'var(--text)',
                      border: `1px solid ${selectedStudentId === student.id ? 'var(--primary)' : 'var(--border)'}`,
                    }}
                  >
                    <input
                      type="radio"
                      name="student"
                      value={student.id}
                      checked={selectedStudentId === student.id}
                      onChange={e => setSelectedStudentId(e.target.value)}
                      className="mr-3"
                    />
                    <div>
                      <p className="font-semibold">
                        {student.firstName} {student.lastName}
                      </p>
                      {student.grade && (
                        <p className="text-sm opacity-75">
                          Grado {student.grade}
                        </p>
                      )}
                    </div>
                  </label>
                ))}
              </div>
            )}
          </div>

          {/* Paso 2 */}
          <div
            className="mb-8 p-6 rounded-lg"
            style={{
              backgroundColor: 'var(--background)',
              border: `1px solid var(--border)`,
            }}
          >
            <h2 className="text-lg font-bold mb-4" style={{ color: 'var(--text)' }}>
              Paso 2: Confirma la actividad
            </h2>
            <div>
              <p style={{ color: 'var(--muted)' }} className="text-sm font-medium uppercase mb-2">
                Actividad seleccionada
              </p>
              <h3 className="text-2xl font-bold mb-3" style={{ color: 'var(--primary)' }}>
                {selectedActivity.name}
              </h3>
              <p style={{ color: 'var(--text)' }} className="mb-4">
                {selectedActivity.description}
              </p>
              <div className="grid grid-cols-2 gap-4">
                <div
                  className="p-3 rounded"
                  style={{
                    backgroundColor: 'var(--primary)',
                    color: 'white',
                  }}
                >
                  <p className="text-sm opacity-90">Cupos disponibles</p>
                  <p className="text-2xl font-bold">
                    {selectedActivity.availableSpots}/{selectedActivity.totalSpots}
                  </p>
                </div>
                <div
                  className="p-3 rounded"
                  style={{
                    backgroundColor: selectedActivity.availableSpots > 0 ? 'var(--accent)' : '#dc2626',
                    color: 'white',
                  }}
                >
                  <p className="text-sm opacity-90">Estado</p>
                  <p className="text-lg font-bold">
                    {selectedActivity.availableSpots > 0 ? '✓ Disponible' : '✕ Lleno'}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Mensaje */}
          {message && (
            <div
              className="mb-6 p-4 rounded-lg font-medium"
              style={{
                backgroundColor: message.includes('exitosa') ? '#dcfce7' : '#fee',
                color: message.includes('exitosa') ? '#166534' : '#991b1b',
                border: `1px solid ${message.includes('exitosa') ? '#86efac' : '#fca5a5'}`,
              }}
            >
              {message.includes('exitosa') ? '✓ ' : '⚠️ '}
              {message}
            </div>
          )}

          {/* Botones */}
          <div className="flex gap-3">
            <button
              onClick={() => router.back()}
              className="flex-1 btn-secondary"
            >
              Cancelar
            </button>
            <button
              onClick={handleEnroll}
              disabled={!selectedStudentId || enrollLoading || selectedActivity.availableSpots === 0}
              className={`flex-1 btn-primary ${(!selectedStudentId || enrollLoading || selectedActivity.availableSpots === 0) ? 'opacity-50 cursor-not-allowed' : ''}`}
            >
              {enrollLoading ? '⏳ Inscribiendo...' : '✓ Confirmar inscripción'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
