import { useState, useEffect } from 'react'
import apiClient from '@/services/apiClient'
import { useAuth } from './useAuth'

export interface Enrollment {
  id: string
  studentId: string
  activityId: string
  activityName: string
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED'
  enrolledAt: string
}

export interface AttendanceRecord {
  id: string
  sessionId: string
  studentId: string
  present: boolean
  observation?: string
  recordedAt: string
}

export interface TrackingData {
  studentId: string
  studentName: string
  enrollments: Enrollment[]
  attendance: AttendanceRecord[]
}

export function useTracking() {
  const { user } = useAuth()
  const [data, setData] = useState<TrackingData[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [refreshKey, setRefreshKey] = useState(0)

  const fetchTracking = async () => {
    if (!user?.id) {
      setLoading(false)
      return
    }

    try {
      setError(null)
      const [enrollmentsRes, attendanceRes] = await Promise.all([
        apiClient.get(`/api/enrollments/guardian/${user.id}`),
        apiClient.get(`/api/attendance/guardians/${user.id}`),
      ])

      const enrollments = enrollmentsRes.data || []
      const attendance = attendanceRes.data || []

      // Agrupar por estudiante
      const grouped = enrollments.reduce((acc: Record<string, TrackingData>, enrollment: any) => {
        if (!acc[enrollment.studentId]) {
          acc[enrollment.studentId] = {
            studentId: enrollment.studentId,
            studentName: enrollment.studentName,
            enrollments: [],
            attendance: [],
          }
        }
        acc[enrollment.studentId].enrollments.push(enrollment)
        return acc
      }, {})

      // Agregar registros de asistencia
      attendance.forEach((record: any) => {
        if (grouped[record.studentId]) {
          grouped[record.studentId].attendance.push(record)
        }
      })

      setData(Object.values(grouped))
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error al cargar seguimiento')
      setData([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchTracking()
  }, [user?.id, refreshKey])

  const refresh = () => {
    setLoading(true)
    setRefreshKey(prev => prev + 1)
  }

  return { data, loading, error, refresh }
}
