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

      const response = await apiClient.post('/attendance/sessions', { activityId })
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

        await apiClient.post(`/attendance/records`, {
          sessionId,
          enrollmentId,
          present,
          observations,
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
