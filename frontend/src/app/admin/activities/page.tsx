'use client'

export const dynamic = 'force-dynamic'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useAdminActivities, CreateActivityInput } from '@/hooks/useAdminActivities'
import { ActivityForm } from '@/components/admin/ActivityForm'
import { ActivityManagementList } from '@/components/admin/ActivityManagementList'
import { Activity } from '@/types'

export default function AdminActivitiesPage() {
  const router = useRouter()
  const { user, isAuthenticated, logout, loading: authLoading } = useAuth()
  const {
    activities,
    loading,
    error,
    fetchActivities,
    createActivity,
    updateActivity,
    publishActivity,
    disableActivity,
    deleteActivity,
  } = useAdminActivities()

  const [showForm, setShowForm] = useState(false)
  const [editingActivity, setEditingActivity] = useState<Activity | null>(null)
  const [successMessage, setSuccessMessage] = useState('')

  useEffect(() => {
    if (isAuthenticated && !authLoading) {
      fetchActivities()
    }
  }, [isAuthenticated, authLoading, fetchActivities])

  if (authLoading) {
    return <div className="flex items-center justify-center min-h-screen"><p>Cargando...</p></div>
  }

  if (!isAuthenticated || (user?.role !== 'ADMIN' && user?.role !== 'SUPERADMIN')) {
    router.push('/login')
    return null
  }

  const handleCreateActivity = async (data: CreateActivityInput) => {
    try {
      await createActivity(data)
      setShowForm(false)
      setSuccessMessage('Actividad creada exitosamente')
      setTimeout(() => setSuccessMessage(''), 3000)
    } catch (err) {
      console.error(err)
    }
  }

  const handleEditActivity = (activity: Activity) => {
    setEditingActivity(activity)
    setShowForm(true)
  }

  const handleUpdateActivity = async (data: CreateActivityInput) => {
    if (!editingActivity) return

    try {
      await updateActivity(editingActivity.id, {
        name: data.name,
        description: data.description,
        totalSpots: data.totalSpots,
      })
      setShowForm(false)
      setEditingActivity(null)
      setSuccessMessage('Actividad actualizada exitosamente')
      setTimeout(() => setSuccessMessage(''), 3000)
    } catch (err) {
      console.error(err)
    }
  }

  const handlePublishActivity = async (activityId: string) => {
    try {
      await publishActivity(activityId)
      setSuccessMessage('Actividad publicada exitosamente')
      setTimeout(() => setSuccessMessage(''), 3000)
    } catch (err) {
      console.error(err)
    }
  }

  const handleDisableActivity = async (activityId: string) => {
    try {
      await disableActivity(activityId)
      setSuccessMessage('Actividad deshabilitada')
      setTimeout(() => setSuccessMessage(''), 3000)
    } catch (err) {
      console.error(err)
    }
  }

  const handleDeleteActivity = async (activityId: string) => {
    try {
      await deleteActivity(activityId)
      setSuccessMessage('Actividad eliminada')
      setTimeout(() => setSuccessMessage(''), 3000)
    } catch (err) {
      console.error(err)
    }
  }

  const handleCancel = () => {
    setShowForm(false)
    setEditingActivity(null)
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">Administración de actividades</h1>
          <button
            onClick={() => router.push('/')}
            className="text-gray-600 hover:text-gray-900 font-medium"
          >
            Inicio
          </button>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-8">
        {successMessage && (
          <div className="mb-4 p-4 bg-green-100 border border-green-400 text-green-700 rounded">
            {successMessage}
          </div>
        )}

        {showForm ? (
          <div className="mb-8">
            <ActivityForm
              activity={editingActivity || undefined}
              loading={loading}
              error={error}
              onSubmit={editingActivity ? handleUpdateActivity : handleCreateActivity}
              onCancel={handleCancel}
            />
          </div>
        ) : (
          <div className="mb-8">
            <button
              onClick={() => setShowForm(true)}
              className="bg-blue-600 text-white py-2 px-6 rounded-md font-medium hover:bg-blue-700"
            >
              + Nueva actividad
            </button>
          </div>
        )}

        <div>
          <h2 className="text-lg font-bold mb-4">Actividades existentes</h2>
          <ActivityManagementList
            activities={activities}
            loading={loading}
            onEdit={handleEditActivity}
            onPublish={handlePublishActivity}
            onDisable={handleDisableActivity}
            onDelete={handleDeleteActivity}
          />
        </div>
      </main>
    </div>
  )
}
