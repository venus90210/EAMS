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
  enrollmentId: string
  date: string
  present: boolean
  observations?: string
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

  useEffect(() => {
    if (!user?.id) {
      setLoading(false)
      return
    }

    const fetchTracking = async () => {
      try {
        setError(null)
        const [enrollmentsRes, attendanceRes] = await Promise.all([
          apiClient.get(`/enrollments/guardian/${user.id}`),
          apiClient.get(`/attendance/guardians/${user.id}`),
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
          const enrollment = enrollments.find((e: any) => e.id === record.enrollmentId)
          if (enrollment && grouped[enrollment.studentId]) {
            grouped[enrollment.studentId].attendance.push(record)
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

    fetchTracking()
  }, [user?.id])

  return { data, loading, error }
}
