import { useState, useCallback } from 'react'
import apiClient from '@/services/apiClient'
import { Activity } from '@/types'

export interface CreateActivityInput {
  name: string
  description: string
  totalSpots: number
  status?: 'DRAFT' | 'PUBLISHED'
}

export interface UpdateActivityInput {
  name?: string
  description?: string
  totalSpots?: number
  availableSpots?: number
  status?: 'DRAFT' | 'PUBLISHED' | 'DISABLED'
}

export function useAdminActivities() {
  const [activities, setActivities] = useState<Activity[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchActivities = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const response = await apiClient.get('/api/activities')
      setActivities(response.data)
      return response.data
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al cargar actividades'
      setError(msg)
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  const createActivity = useCallback(async (input: CreateActivityInput) => {
    try {
      setError(null)
      const response = await apiClient.post('/api/activities', {
        name: input.name,
        description: input.description,
        totalSpots: input.totalSpots,
        status: input.status || 'DRAFT',
        availableSpots: input.totalSpots,
      })

      const newActivity = response.data
      setActivities(prev => [...prev, newActivity])
      return newActivity
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al crear actividad'
      setError(msg)
      throw err
    }
  }, [])

  const updateActivity = useCallback(async (activityId: string, input: UpdateActivityInput) => {
    try {
      setError(null)
      const response = await apiClient.put(`/api/activities/${activityId}`, input)

      const updatedActivity = response.data
      setActivities(prev =>
        prev.map(a => (a.id === activityId ? updatedActivity : a))
      )
      return updatedActivity
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al actualizar actividad'
      setError(msg)
      throw err
    }
  }, [])

  const publishActivity = useCallback(async (activityId: string) => {
    return updateActivity(activityId, { status: 'PUBLISHED' })
  }, [updateActivity])

  const disableActivity = useCallback(async (activityId: string) => {
    return updateActivity(activityId, { status: 'DISABLED' })
  }, [updateActivity])

  const deleteActivity = useCallback(async (activityId: string) => {
    try {
      setError(null)
      await apiClient.delete(`/api/activities/${activityId}`)
      setActivities(prev => prev.filter(a => a.id !== activityId))
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al eliminar actividad'
      setError(msg)
      throw err
    }
  }, [])

  return {
    activities,
    loading,
    error,
    fetchActivities,
    createActivity,
    updateActivity,
    publishActivity,
    disableActivity,
    deleteActivity,
  }
}
