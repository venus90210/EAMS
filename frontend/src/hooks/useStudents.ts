import { useState, useEffect } from 'react'
import apiClient from '@/services/apiClient'
import { useAuth } from './useAuth'

export interface Student {
  id: string
  firstName: string
  lastName: string
  grade?: string
}

export function useStudents() {
  const { user } = useAuth()
  const [students, setStudents] = useState<Student[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!user?.id) {
      setLoading(false)
      return
    }

    const fetchStudents = async () => {
      try {
        setError(null)
        const response = await apiClient.get(`/users/guardians/${user.id}/students`)
        setStudents(response.data || [])
      } catch (err: any) {
        setError(err.response?.data?.message || 'Error al cargar estudiantes')
        setStudents([])
      } finally {
        setLoading(false)
      }
    }

    fetchStudents()
  }, [user?.id])

  return { students, loading, error }
}
