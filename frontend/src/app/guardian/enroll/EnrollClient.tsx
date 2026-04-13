'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useActivities } from '@/hooks/useActivities'
import { useEnrollment } from '@/hooks/useEnrollment'
import { useStudents } from '@/hooks/useStudents'

export function EnrollClient() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const { students, loading: studentsLoading } = useStudents()
  const { enroll, loading: enrollLoading, error: enrollError } = useEnrollment()
  const { activities } = useActivities()

  const [selectedStudentId, setSelectedStudentId] = useState('')
  const [message, setMessage] = useState('')

  const activityId = searchParams.get('activityId')
  const selectedActivity = activities.find(a => a.id === activityId)

  if (authLoading) {
    return <div className="flex items-center justify-center min-h-screen"><p>Cargando...</p></div>
  }

  if (!isAuthenticated || user?.role !== 'GUARDIAN') {
    router.push('/login')
    return null
  }

  if (!activityId || !selectedActivity) {
    return (
      <div className="max-w-md mx-auto p-8">
        <div className="bg-red-100 border border-red-400 text-red-700 p-4 rounded">
          Actividad no encontrada
        </div>
        <button
          onClick={() => router.push('/guardian/activities')}
          className="mt-4 w-full bg-gray-500 text-white py-2 px-4 rounded hover:bg-gray-600"
        >
          Volver a actividades
        </button>
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
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow p-8">
        <h1 className="text-3xl font-bold mb-6">Inscribir estudiante</h1>

        <div className="mb-8 p-4 border rounded-lg">
          <h2 className="text-lg font-semibold mb-4">Paso 1: Selecciona el estudiante</h2>
          {studentsLoading ? (
            <p className="text-gray-500">Cargando estudiantes...</p>
          ) : students.length === 0 ? (
            <p className="text-red-600">No tienes estudiantes asociados</p>
          ) : (
            <div className="space-y-2">
              {students.map(student => (
                <label key={student.id} className="flex items-center">
                  <input
                    type="radio"
                    name="student"
                    value={student.id}
                    checked={selectedStudentId === student.id}
                    onChange={e => setSelectedStudentId(e.target.value)}
                    className="mr-3"
                  />
                  <span>{student.firstName} {student.lastName} {student.grade ? `(${student.grade})` : ''}</span>
                </label>
              ))}
            </div>
          )}
        </div>

        <div className="mb-8 p-4 border rounded-lg bg-blue-50">
          <h2 className="text-lg font-semibold mb-4">Paso 2: Confirma la actividad</h2>
          <div>
            <p className="text-sm text-gray-600">Actividad seleccionada</p>
            <h3 className="text-xl font-bold text-blue-600">{selectedActivity.name}</h3>
            <p className="text-gray-700 mt-2">{selectedActivity.description}</p>
            <div className="mt-3 text-sm">
              <p><strong>Cupos disponibles:</strong> {selectedActivity.availableSpots}/{selectedActivity.totalSpots}</p>
            </div>
          </div>
        </div>

        {message && (
          <div className={`mb-4 p-4 rounded ${message.includes('exitosa') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
            {message}
          </div>
        )}

        <div className="flex gap-4">
          <button
            onClick={() => router.back()}
            className="flex-1 bg-gray-500 text-white py-2 px-4 rounded hover:bg-gray-600 disabled:bg-gray-300"
          >
            Cancelar
          </button>
          <button
            onClick={handleEnroll}
            disabled={!selectedStudentId || enrollLoading || selectedActivity.availableSpots === 0}
            className="flex-1 bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700 disabled:bg-gray-400"
          >
            {enrollLoading ? 'Inscribiendo...' : 'Confirmar inscripción'}
          </button>
        </div>
      </div>
    </div>
  )
}
