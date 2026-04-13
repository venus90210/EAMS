'use client'

export const dynamic = 'force-dynamic'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useAttendanceSessions } from '@/hooks/useAttendanceSessions'
import { useActivities } from '@/hooks/useActivities'
import { AttendanceList } from '@/components/attendance/AttendanceList'
import { authService } from '@/services/authService'

export default function AttendancePage() {
  const router = useRouter()
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const { activities } = useActivities()
  const { session, loading, error, openSession, recordAttendance } = useAttendanceSessions()

  const [selectedActivityId, setSelectedActivityId] = useState('')
  const [message, setMessage] = useState('')

  if (authLoading) {
    return <div className="flex items-center justify-center min-h-screen"><p>Cargando...</p></div>
  }

  // If not authenticated in context, but we have a token stored, wait for context to update
  if (!isAuthenticated && authService.getAccessToken()) {
    return <div className="flex items-center justify-center min-h-screen"><p>Cargando sesión...</p></div>
  }

  if (!isAuthenticated || user?.role !== 'TEACHER') {
    router.push('/login')
    return null
  }

  const handleOpenSession = async () => {
    if (!selectedActivityId) {
      setMessage('Selecciona una actividad')
      return
    }

    try {
      await openSession(selectedActivityId)
      setMessage('')
    } catch (err) {
      setMessage(error || 'Error al abrir sesión')
    }
  }

  const publishedActivities = activities.filter(a => a.status === 'PUBLISHED')

  if (!session) {
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
                  REGISTRO DE ASISTENCIA
                </h1>
                <p style={{
                  fontSize: '16px',
                  lineHeight: '1.5',
                  opacity: '0.95',
                  marginBottom: '24px'
                }}>
                  Abre una sesión y controla la asistencia de tus estudiantes en tiempo real
                </p>
              </div>
              <div style={{
                fontSize: '80px',
                textAlign: 'center',
                opacity: '0.8',
                flexShrink: 0
              }}>
                📋
              </div>
            </div>
          </div>
        </section>

        {/* Content Section */}
        <section style={{
          padding: '80px 40px',
          background: 'white'
        }}>
          <div style={{ maxWidth: '600px', margin: '0 auto' }}>
            <div style={{
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
                🎯 Abrir sesión
              </h2>

              <div style={{ marginBottom: '28px' }}>
                <label style={{
                  display: 'block',
                  fontSize: '14px',
                  fontWeight: '700',
                  color: 'var(--text)',
                  marginBottom: '12px',
                  textTransform: 'uppercase',
                  letterSpacing: '0.5px'
                }}>
                  Selecciona la actividad
                </label>
                <select
                  value={selectedActivityId}
                  onChange={e => setSelectedActivityId(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px 16px',
                    fontSize: '15px',
                    border: '2px solid var(--border)',
                    borderRadius: 'var(--input-radius)',
                    backgroundColor: 'var(--bg)',
                    color: 'var(--text)',
                    transition: 'all 0.2s',
                    cursor: 'pointer'
                  }}
                >
                  <option value="">-- Selecciona una actividad --</option>
                  {publishedActivities.map(activity => (
                    <option key={activity.id} value={activity.id}>
                      {activity.name}
                    </option>
                  ))}
                </select>
              </div>

              {message && (
                <div
                  style={{
                    padding: '16px',
                    borderRadius: 'var(--card-radius)',
                    backgroundColor: message.includes('Error') ? '#ffebee' : '#fff3cd',
                    color: message.includes('Error') ? '#c62828' : '#856404',
                    border: `1px solid ${message.includes('Error') ? '#fca5a5' : '#ffeaa7'}`,
                    borderLeft: `4px solid ${message.includes('Error') ? 'var(--danger)' : 'var(--warning)'}`,
                    fontWeight: '600',
                    marginBottom: '28px'
                  }}
                >
                  {message.includes('Error') ? '⚠️ ' : '📌 '}
                  {message}
                </div>
              )}

              <button
                onClick={handleOpenSession}
                disabled={loading || !selectedActivityId}
                style={{
                  width: '100%',
                  padding: '14px 24px',
                  fontSize: '16px',
                  fontWeight: '700',
                  backgroundColor: loading || !selectedActivityId ? 'var(--muted)' : 'var(--primary)',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: loading || !selectedActivityId ? 'not-allowed' : 'pointer',
                  transition: 'all 0.2s',
                  boxShadow: '0 4px 12px rgba(0, 166, 81, 0.2)'
                }}
                onMouseEnter={(e) => {
                  if (!loading && selectedActivityId) {
                    e.currentTarget.style.backgroundColor = 'var(--primary-dark)'
                    e.currentTarget.style.transform = 'translateY(-2px)'
                    e.currentTarget.style.boxShadow = '0 6px 16px rgba(0, 166, 81, 0.3)'
                  }
                }}
                onMouseLeave={(e) => {
                  if (!loading && selectedActivityId) {
                    e.currentTarget.style.backgroundColor = 'var(--primary)'
                    e.currentTarget.style.transform = 'translateY(0)'
                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 166, 81, 0.2)'
                  }
                }}
              >
                {loading ? '⏳ Abriendo sesión...' : '✓ Abrir sesión de hoy'}
              </button>
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

  return (
    <div style={{ backgroundColor: '#f5f5f5', minHeight: '100vh' }}>
      {/* Hero Section */}
      <section style={{
        background: 'linear-gradient(135deg, var(--accent) 0%, var(--primary) 100%)',
        color: 'white',
        padding: '60px 40px',
        position: 'relative',
        overflow: 'hidden'
      }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          <h1 style={{
            fontSize: '40px',
            fontWeight: '900',
            lineHeight: '1.2',
            marginBottom: '16px'
          }}>
            SESIÓN ACTIVA
          </h1>
          <p style={{
            fontSize: '16px',
            lineHeight: '1.5',
            opacity: '0.95'
          }}>
            Registra la asistencia de los estudiantes de {activities.find(a => a.id === session.activityId)?.name}
          </p>
        </div>
      </section>

      {/* Content Section */}
      <section style={{
        padding: '80px 40px',
        background: 'white'
      }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          {/* Session Info */}
          <div
            style={{
              padding: '24px',
              borderRadius: 'var(--card-radius)',
              backgroundColor: '#dcfce7',
              border: '1px solid #86efac',
              borderLeft: '4px solid var(--accent)',
              marginBottom: '48px'
            }}
          >
            <h2 style={{ fontSize: '18px', fontWeight: '800', color: '#166534', margin: '0 0 8px 0' }}>
              ✓ Sesión abierta para {activities.find(a => a.id === session.activityId)?.name}
            </h2>
            <p style={{ color: '#15803d', fontSize: '15px', margin: '0' }}>
              📅 {new Date(session.date).toLocaleDateString('es-ES', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric'
              })}
            </p>
          </div>

          {/* Students Section */}
          <div style={{ marginBottom: '48px' }}>
            <h3 style={{
              fontSize: '28px',
              fontWeight: '900',
              color: 'var(--text)',
              marginBottom: '24px',
              margin: '0 0 24px 0'
            }}>
              👥 Estudiantes inscritos ({session.students.length})
            </h3>
            <AttendanceList
              students={session.students}
              sessionId={session.id}
              onRecordAttendance={recordAttendance}
              isLoading={loading}
            />
          </div>

          {/* Close Button */}
          <button
            onClick={() => {
              window.location.href = '/teacher/attendance'
            }}
            style={{
              width: '100%',
              padding: '14px 24px',
              fontSize: '16px',
              fontWeight: '700',
              backgroundColor: 'var(--muted)',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              transition: 'all 0.2s'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-2px)'
              e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.1)'
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)'
              e.currentTarget.style.boxShadow = 'none'
            }}
          >
            ✕ Cerrar sesión
          </button>
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
