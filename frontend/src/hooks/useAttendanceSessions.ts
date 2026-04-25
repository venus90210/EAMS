import { useState, useCallback } from 'react'
import apiClient from '@/services/apiClient'

export interface AttendanceStudent {
  enrollmentId: string
  studentId: string
  studentName: string
  present: boolean
  observations: string
}

export interface AttendanceSession {
  id: string
  activityId: string
  date: string
  students: AttendanceStudent[]
}

export function useAttendanceSessions() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [session, setSession] = useState<AttendanceSession | null>(null)

  const openSession = useCallback(async (activityId: string) => {
    try {
      setLoading(true)
      setError(null)

      const today = new Date().toISOString().split('T')[0]
      const response = await apiClient.post('/api/attendance/sessions', {
        activityId,
        date: today,
      })
      setSession(response.data)
      return response.data
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al abrir sesión de asistencia'
      setError(msg)
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  const recordAttendance = useCallback(
    async (sessionId: string, enrollmentId: string, present: boolean, observations?: string) => {
      try {
        setError(null)

        // Obtener el studentId del estado actual
        if (!session) return

        const enrollment = session.students.find(s => s.enrollmentId === enrollmentId)
        if (!enrollment) throw new Error('Estudiante no encontrado')

        await apiClient.post(`/api/attendance/records`, {
          sessionId,
          studentId: enrollment.studentId,
          present,
          observation: observations,
        })

        // Actualizar estado local
        if (session) {
          const updated = {
            ...session,
            students: session.students.map(s =>
              s.enrollmentId === enrollmentId ? { ...s, present, observations: observations || '' } : s
            ),
          }
          setSession(updated)
        }
      } catch (err: any) {
        const msg = err.response?.data?.message || 'Error al guardar asistencia'
        setError(msg)
        throw err
      }
    },
    [session]
  )

  return { session, loading, error, openSession, recordAttendance }
}
