'use client'

export const dynamic = 'force-dynamic'

import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useActivities } from '@/hooks/useActivities'
import { useOfflineStatus } from '@/hooks/useOfflineStatus'
import { ActivityCard } from '@/components/activities/ActivityCard'
import { authService } from '@/services/authService'

export default function ActivitiesPage() {
  const router = useRouter()
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const { activities, loading, error, fromCache } = useActivities()
  const { isOnline } = useOfflineStatus()

  if (authLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p style={{ color: 'var(--muted)' }}>Cargando...</p>
      </div>
    )
  }

  if (!isAuthenticated && authService.getAccessToken()) {
    console.log('[ActivitiesPage] Token exists but context not updated yet, waiting...')
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p style={{ color: 'var(--muted)' }}>Cargando sesión...</p>
      </div>
    )
  }

  if (!isAuthenticated || user?.role !== 'GUARDIAN') {
    console.log('[ActivitiesPage] Not authenticated or wrong role, redirecting to login')
    router.push('/login')
    return null
  }

  const handleEnroll = (activityId: string) => {
    router.push(`/guardian/enroll?activityId=${activityId}`)
  }

  return (
    <div style={{ backgroundColor: '#f5f5f5' }}>
      {/* Hero Section - Green */}
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
                ACTIVIDADES EXTRACURRICULARES
              </h1>
              <p style={{
                fontSize: '16px',
                lineHeight: '1.5',
                opacity: '0.95',
                marginBottom: '24px'
              }}>
                Desarrolla tus talentos y habilidades participando en actividades diseñadas para ti
              </p>
              {activities.length > 0 && (
                <button
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
                  EXPLORAR ACTIVIDADES
                </button>
              )}
            </div>
            <div style={{
              fontSize: '80px',
              textAlign: 'center',
              opacity: '0.8',
              flexShrink: 0
            }}>
              🎯
            </div>
          </div>
        </div>
      </section>

      {/* Status Section */}
      {activities.length > 0 && (
        <section style={{
          background: 'white',
          padding: '50px 40px',
          borderBottom: '1px solid var(--border)'
        }}>
          <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
            <div style={{
              textAlign: 'center',
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: '30px'
            }}>
              <div>
                <div style={{
                  fontSize: '48px',
                  fontWeight: '900',
                  color: 'var(--primary)',
                  marginBottom: '8px'
                }}>
                  {activities.length}
                </div>
                <p style={{
                  color: 'var(--text-light)',
                  fontSize: '14px',
                  fontWeight: '600',
                  textTransform: 'uppercase',
                  letterSpacing: '1px'
                }}>
                  Actividades Disponibles
                </p>
              </div>
              <div>
                <div style={{
                  fontSize: '48px',
                  fontWeight: '900',
                  color: 'var(--primary)',
                  marginBottom: '8px'
                }}>
                  {activities.reduce((sum, a) => sum + a.availableSpots, 0)}
                </div>
                <p style={{
                  color: 'var(--text-light)',
                  fontSize: '14px',
                  fontWeight: '600',
                  textTransform: 'uppercase',
                  letterSpacing: '1px'
                }}>
                  Cupos Disponibles
                </p>
              </div>
              <div>
                <div style={{
                  fontSize: '48px',
                  fontWeight: '900',
                  color: isOnline ? 'var(--primary)' : 'var(--warning)',
                  marginBottom: '8px'
                }}>
                  {isOnline ? '✓' : '📴'}
                </div>
                <p style={{
                  color: 'var(--text-light)',
                  fontSize: '14px',
                  fontWeight: '600',
                  textTransform: 'uppercase',
                  letterSpacing: '1px'
                }}>
                  {isOnline ? 'Conexión Activa' : 'Modo Offline'}
                </p>
              </div>
            </div>
          </div>
        </section>
      )}

      {/* Activities Section */}
      {activities.length > 0 && (
        <section style={{
          padding: '80px 40px',
          background: 'white'
        }}>
          <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
            <div style={{
              textAlign: 'center',
              marginBottom: '60px'
            }}>
              <h2 style={{
                fontSize: '40px',
                fontWeight: '900',
                color: 'var(--primary)',
                marginBottom: '12px',
                letterSpacing: '-0.5px'
              }}>
                Nuestras Actividades
              </h2>
              <p style={{
                color: 'var(--primary)',
                fontSize: '18px',
                fontWeight: '700'
              }}>
                EXPLORA Y PARTICIPA EN LAS QUE TE INTERESEN
              </p>
            </div>

            {error && (
              <div style={{
                marginBottom: '40px',
                padding: '24px 32px',
                borderRadius: '4px',
                backgroundColor: '#ffebee',
                borderLeft: '6px solid var(--danger)',
                color: '#c62828',
                fontWeight: '600'
              }}>
                ⚠️ {error}
              </div>
            )}

            {loading ? (
              <div style={{ textAlign: 'center', padding: '60px 20px', color: 'var(--muted)' }}>
                ⏳ Cargando actividades...
              </div>
            ) : (
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
                gap: '32px'
              }}>
                {activities.map((activity) => (
                  <ActivityCard
                    key={activity.id}
                    activity={activity}
                    onEnroll={handleEnroll}
                    offlineMode={!isOnline}
                  />
                ))}
              </div>
            )}
          </div>
        </section>
      )}

      {activities.length === 0 && !loading && (
        <section style={{
          padding: '100px 40px',
          textAlign: 'center',
          background: 'white'
        }}>
          <p style={{
            color: 'var(--muted)',
            fontSize: '18px',
            fontWeight: '600'
          }}>
            No hay actividades disponibles en este momento
          </p>
        </section>
      )}

      {/* CTA Section */}
      {activities.length > 0 && (
        <section style={{
          background: 'linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%)',
          color: 'white',
          padding: '80px 40px',
          textAlign: 'center'
        }}>
          <div style={{ maxWidth: '800px', margin: '0 auto' }}>
            <h2 style={{
              fontSize: '40px',
              fontWeight: '900',
              marginBottom: '16px'
            }}>
              COMIENZA TU VIAJE
            </h2>
            <p style={{
              fontSize: '18px',
              opacity: '0.95',
              marginBottom: '40px',
              lineHeight: '1.6'
            }}>
              Consulta el estado de tus inscripciones y participa en las actividades que te interesan
            </p>
            <button
              onClick={() => router.push('/guardian/tracking')}
              style={{
                backgroundColor: 'var(--secondary)',
                color: '#000',
                padding: '16px 48px',
                fontSize: '16px',
                fontWeight: '800',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                transition: 'all 0.2s',
                textTransform: 'uppercase',
                letterSpacing: '1px'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateY(-2px)'
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)'
              }}
            >
              Ver mis inscripciones
            </button>
          </div>
        </section>
      )}

      {/* Footer */}
      <footer style={{
        background: '#1a1a1a',
        color: 'white',
        padding: '40px',
        textAlign: 'center',
        borderTop: `4px solid var(--primary)`
      }}>
        <p style={{ fontSize: '14px', margin: 0 }}>
          © 2026 EAMS - Sistema de Gestión de Actividades Extracurriculares
        </p>
      </footer>
    </div>
  )
}
