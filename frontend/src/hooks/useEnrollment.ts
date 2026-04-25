import { useState, useCallback } from 'react'
import apiClient from '@/services/apiClient'

export interface EnrollmentError {
  code: string
  message: string
}

export function useEnrollment() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<EnrollmentError | null>(null)

  const enroll = useCallback(
    async (studentId: string, activityId: string) => {
      try {
        setLoading(true)
        setError(null)

        console.log('[useEnrollment] Enrolling student:', { studentId, activityId })
        const response = await apiClient.post('/api/enrollments', {
          studentId,
          activityId,
        })

        console.log('[useEnrollment] Success:', response.data)
        return response.data
      } catch (err: any) {
        console.error('[useEnrollment] Error:', err.response?.data || err.message)
        const errorCode = err.response?.data?.code || 'ENROLLMENT_ERROR'
        const errorMessage =
          err.response?.data?.message || 'Error al inscribirse en la actividad'

        let userMessage = errorMessage
        if (errorCode === 'SPOT_EXHAUSTED') {
          userMessage = 'Cupos agotados en esta actividad'
        } else if (errorCode === 'ALREADY_ENROLLED') {
          userMessage = 'Ya estás inscrito en esta actividad'
        } else if (errorCode === 'ACTIVE_ENROLLMENT_EXISTS') {
          userMessage = 'Ya tienes una inscripción activa en esta actividad'
        }

        const enrollmentError: EnrollmentError = {
          code: errorCode,
          message: userMessage,
        }

        setError(enrollmentError)
        throw enrollmentError
      } finally {
        setLoading(false)
      }
    },
    []
  )

  return { enroll, loading, error }
}
