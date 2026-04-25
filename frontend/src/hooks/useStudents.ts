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
      console.log('[useStudents] User not loaded yet, user:', user)
      setLoading(false)
      return
    }

    const fetchStudents = async () => {
      try {
        setError(null)
        console.log('[useStudents] Fetching students for guardian:', user.id)
        const response = await apiClient.get(`/api/users/guardians/${user.id}/students`)
        console.log('[useStudents] Response:', response.data)
        setStudents(response.data || [])
      } catch (err: any) {
        console.error('[useStudents] Error:', err.response?.data || err.message)
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
