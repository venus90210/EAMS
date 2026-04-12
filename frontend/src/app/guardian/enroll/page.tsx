'use client'

import { useEffect, useState, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import apiClient from '@/services/apiClient'
import { Activity, Student } from '@/types'
import { EnrollmentForm } from '@/components/enrollment/EnrollmentForm'

// Prevent static generation for this page
export const dynamic = 'force-dynamic'

function EnrollPageContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { user, isAuthenticated } = useAuth()

  const activityId = searchParams.get('activityId')

  const [activity, setActivity] = useState<Activity | null>(null)
  const [students, setStudents] = useState<Student[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  if (!isAuthenticated || user?.role !== 'GUARDIAN' || !activityId) {
    router.push('/login')
    return null
  }

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true)
        setError(null)

        // Fetch activity
        const activityResponse = await apiClient.get<Activity>(`/api/activities/${activityId}`)
        setActivity(activityResponse.data)

        // Fetch students
        const studentsResponse = await apiClient.get<Student[]>(`/api/users/guardians/${user?.id}/students`)
        setStudents(studentsResponse.data)
      } catch (err: any) {
        setError(err.response?.data?.message || 'Error al cargar los datos')
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [activityId, user?.id])

  const handleSuccess = () => {
    setSuccessMessage('¡Inscripción exitosa!')
    setTimeout(() => {
      router.push('/guardian/tracking')
    }, 2000)
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-gray-600">Cargando...</p>
      </div>
    )
  }

  if (error || !activity) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-lg shadow p-6 max-w-md w-full">
          <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded mb-4">
            {error || 'Actividad no encontrada'}
          </div>
          <button
            onClick={() => router.push('/guardian/activities')}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-md font-medium hover:bg-blue-700"
          >
            Volver a actividades
          </button>
        </div>
      </div>
    )
  }

  if (successMessage) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-lg shadow p-6 max-w-md w-full text-center">
          <div className="mb-4 text-4xl">✅</div>
          <h2 className="text-xl font-bold text-green-600 mb-2">{successMessage}</h2>
          <p className="text-gray-600 mb-4">Redirigiendo a tus inscripciones...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto px-4">
        <h1 className="text-2xl font-bold text-gray-900 mb-8">Inscripción a actividad</h1>

        {students.length === 0 && (
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-gray-600 mb-4">No tienes hijos registrados en el sistema.</p>
            <button
              onClick={() => router.push('/guardian/activities')}
              className="bg-blue-600 text-white py-2 px-4 rounded-md font-medium hover:bg-blue-700"
            >
              Volver a actividades
            </button>
          </div>
        )}

        {students.length > 0 && (
          <EnrollmentForm
            students={students}
            activity={activity}
            onSuccess={handleSuccess}
            onCancel={() => router.push('/guardian/activities')}
          />
        )}
      </div>
    </div>
  )
}

export default function EnrollPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-gray-50 flex items-center justify-center"><p>Cargando...</p></div>}>
      <EnrollPageContent />
    </Suspense>
  )
}
