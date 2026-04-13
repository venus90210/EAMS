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
    <div style={{ backgroundColor: '#f5f5f5', minHeight: '100vh' }}>
      {/* Hero Section */}
      <section style={{
        background: 'linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%)',
        color: 'white',
        padding: '60px 40px',
        position: 'relative',
        overflow: 'hidden'
      }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          <div style={{
            display: 'grid',
            gridTemplateColumns: '1fr auto',
            gap: '32px',
            alignItems: 'center'
          }}>
            <div>
              <h1 style={{
                fontSize: '40px',
                fontWeight: '900',
                lineHeight: '1.2',
                marginBottom: '16px'
              }}>
                ADMINISTRACIÓN DE ACTIVIDADES
              </h1>
              <p style={{
                fontSize: '16px',
                lineHeight: '1.5',
                opacity: '0.95',
                marginBottom: '24px'
              }}>
                Crea, edita, publica y gestiona todas las actividades extracurriculares de tu institución
              </p>
              {!showForm && (
                <button
                  onClick={() => setShowForm(true)}
                  style={{
                    backgroundColor: 'var(--secondary)',
                    color: '#000',
                    padding: '14px 32px',
                    fontSize: '14px',
                    fontWeight: '800',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                    textTransform: 'uppercase',
                    letterSpacing: '0.5px'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-2px)'
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)'
                  }}
                >
                  + Nueva actividad
                </button>
              )}
            </div>
            <div style={{
              fontSize: '80px',
              textAlign: 'center',
              opacity: '0.8',
              flexShrink: 0
            }}>
              ⚙️
            </div>
          </div>
        </div>
      </section>

      {/* Content Section */}
      <section style={{
        padding: '80px 40px',
        background: 'white'
      }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          {/* Success Message */}
          {successMessage && (
            <div style={{
              padding: '16px',
              borderRadius: 'var(--card-radius)',
              backgroundColor: '#dcfce7',
              borderLeft: '4px solid var(--accent)',
              color: '#166534',
              fontWeight: '600',
              marginBottom: '48px'
            }}>
              ✓ {successMessage}
            </div>
          )}

          {/* Form Section */}
          {showForm && (
            <div style={{
              marginBottom: '48px',
              backgroundColor: 'var(--surface)',
              borderRadius: 'var(--card-radius)',
              border: '1px solid var(--border)',
              padding: '40px',
              boxShadow: 'var(--card-shadow)'
            }}>
              <h2 style={{
                fontSize: '28px',
                fontWeight: '900',
                color: 'var(--text)',
                marginBottom: '32px',
                margin: '0 0 32px 0'
              }}>
                {editingActivity ? '✏️ Editar actividad' : '+ Nueva actividad'}
              </h2>
              <ActivityForm
                activity={editingActivity || undefined}
                loading={loading}
                error={error}
                onSubmit={editingActivity ? handleUpdateActivity : handleCreateActivity}
                onCancel={handleCancel}
              />
            </div>
          )}

          {/* Activities List Section */}
          <div>
            <h2 style={{
              fontSize: '28px',
              fontWeight: '900',
              color: 'var(--text)',
              marginBottom: '32px',
              margin: '0 0 32px 0'
            }}>
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
        </div>
      </section>

      {/* Footer */}
      <footer style={{
        background: '#1a1a1a',
        color: 'white',
        padding: '40px',
        textAlign: 'center',
        borderTop: '4px solid var(--primary)'
      }}>
        <p style={{ fontSize: '14px', margin: 0 }}>
          © 2026 EAMS - Sistema de Gestión de Actividades Extracurriculares
        </p>
      </footer>
    </div>
  )
}
