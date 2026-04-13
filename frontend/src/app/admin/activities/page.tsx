'use client'

export const dynamic = 'force-dynamic'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useAdminActivities, CreateActivityInput } from '@/hooks/useAdminActivities'
import { ActivityForm } from '@/components/admin/ActivityForm'
import { ActivityManagementList } from '@/components/admin/ActivityManagementList'
import { Activity } from '@/types'
import { authService } from '@/services/authService'

export default function AdminActivitiesPage() {
  const router = useRouter()
  const { user, isAuthenticated, loading: authLoading } = useAuth()
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

  // If not authenticated in context, but we have a token stored, wait for context to update
  if (!isAuthenticated && authService.getAccessToken()) {
    return <div className="flex items-center justify-center min-h-screen"><p>Cargando sesión...</p></div>
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
    <div className="min-h-screen" style={{ backgroundColor: 'var(--background)' }}>
      <header style={{ backgroundColor: 'var(--surface)', borderBottom: `1px solid var(--border)` }}>
        <div className="max-w-6xl mx-auto px-4 py-6 flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold" style={{ color: 'var(--text)' }}>
              ⚙️ Administración de actividades
            </h1>
            <p style={{ color: 'var(--muted)' }} className="mt-1">
              Crea, edita y publica tus actividades
            </p>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-8">
        {successMessage && (
          <div className="mb-6 p-4 rounded-lg" style={{ backgroundColor: '#dcfce7', borderLeft: `4px solid var(--accent)` }}>
            <p style={{ color: '#166534' }} className="font-medium">
              ✓ {successMessage}
            </p>
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
              className="btn-primary py-3 px-6 text-lg"
            >
              + Nueva actividad
            </button>
          </div>
        )}

        <div>
          <h2 className="text-2xl font-bold mb-6" style={{ color: 'var(--text)' }}>
            📚 Actividades existentes
          </h2>
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
