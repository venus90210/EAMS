'use client'

export const dynamic = 'force-dynamic'

import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useTracking } from '@/hooks/useTracking'
import { authService } from '@/services/authService'

export default function TrackingPage() {
  const router = useRouter()
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const { data, loading } = useTracking()

  if (authLoading) {
    return <div className="flex items-center justify-center min-h-screen"><p>Cargando...</p></div>
  }

  // If not authenticated in context, but we have a token stored, wait for context to update
  if (!isAuthenticated && authService.getAccessToken()) {
    return <div className="flex items-center justify-center min-h-screen"><p>Cargando sesión...</p></div>
  }

  if (!isAuthenticated || user?.role !== 'GUARDIAN') {
    router.push('/login')
    return null
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
                SEGUIMIENTO DE INSCRIPCIONES
              </h1>
              <p style={{
                fontSize: '16px',
                lineHeight: '1.5',
                opacity: '0.95',
                marginBottom: '24px'
              }}>
                Monitorea el progreso y asistencia de tus estudiantes en las actividades extracurriculares
              </p>
              <button
                onClick={() => router.push('/guardian/activities')}
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
                ← Volver a actividades
              </button>
            </div>
            <div style={{
              fontSize: '80px',
              textAlign: 'center',
              opacity: '0.8',
              flexShrink: 0
            }}>
              📊
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
          {loading ? (
            <div style={{ textAlign: 'center', padding: '60px 20px', color: 'var(--muted)' }}>
              ⏳ Cargando seguimiento...
            </div>
          ) : data.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '60px 20px' }}>
              <p style={{ color: 'var(--muted)', fontSize: '18px', fontWeight: '600' }}>
                No tienes inscripciones aún
              </p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
              {data.map(tracking => (
                <div
                  key={tracking.studentId}
                  style={{
                    backgroundColor: 'var(--surface)',
                    borderRadius: 'var(--card-radius)',
                    border: '1px solid var(--border)',
                    overflow: 'hidden',
                    transition: 'all 0.3s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.boxShadow = 'var(--card-shadow-hover)'
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.boxShadow = 'var(--card-shadow)'
                  }}
                >
                  {/* Student Header */}
                  <div
                    style={{
                      backgroundColor: 'var(--primary)',
                      backgroundImage: 'linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%)',
                      padding: '32px',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '24px'
                    }}
                  >
                    <div
                      style={{
                        backgroundColor: 'rgba(255, 255, 255, 0.2)',
                        width: '64px',
                        height: '64px',
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '32px'
                      }}
                    >
                      👤
                    </div>
                    <div>
                      <p style={{ color: 'rgba(255, 255, 255, 0.8)', fontSize: '12px', fontWeight: '700', textTransform: 'uppercase' }}>
                        Estudiante
                      </p>
                      <h2 style={{ color: 'white', fontSize: '28px', fontWeight: '900', margin: '4px 0 0 0' }}>
                        {tracking.studentName}
                      </h2>
                    </div>
                  </div>

                  {/* Enrollments */}
                  <div style={{ padding: '32px' }}>
                    {tracking.enrollments.length === 0 ? (
                      <p style={{ color: 'var(--muted)', fontSize: '16px' }}>
                        Sin inscripciones registradas
                      </p>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                        {tracking.enrollments.map(enrollment => {
                          // Los attendance records vienen agrupados por estudiante
                          // Mostramos todos los registros ya que no tenemos enrollmentId en ellos
                          const attendanceRecords = tracking.attendance
                          const presentCount = attendanceRecords.filter(a => a.present).length
                          const attendanceRate = attendanceRecords.length > 0
                            ? Math.round((presentCount / attendanceRecords.length) * 100)
                            : 0

                          return (
                            <div
                              key={enrollment.id}
                              style={{
                                backgroundColor: 'var(--bg)',
                                borderRadius: 'var(--card-radius)',
                                border: '1px solid var(--border)',
                                padding: '24px',
                                transition: 'all 0.2s ease'
                              }}
                              onMouseEnter={(e) => {
                                e.currentTarget.style.boxShadow = '0 4px 16px rgba(0, 0, 0, 0.08)'
                              }}
                              onMouseLeave={(e) => {
                                e.currentTarget.style.boxShadow = 'none'
                              }}
                            >
                              {/* Activity Header */}
                              <div style={{
                              display: 'flex',
                              justifyContent: 'space-between',
                              alignItems: 'center',
                              marginBottom: '20px',
                              paddingBottom: '20px',
                              borderBottom: '2px solid var(--border)'
                            }}>
                              <div style={{ flex: 1 }}>
                                <h3 style={{ fontSize: '18px', fontWeight: '800', color: 'var(--text)', margin: 0, marginBottom: '4px' }}>
                                  {enrollment.activityName}
                                </h3>
                                <div style={{
                                  display: 'inline-block',
                                  paddingLeft: '12px',
                                  paddingRight: '12px',
                                  paddingTop: '6px',
                                  paddingBottom: '6px',
                                  borderRadius: '20px',
                                  fontSize: '12px',
                                  fontWeight: '700',
                                  backgroundColor: enrollment.status === 'ACTIVE'
                                    ? 'rgba(76, 175, 80, 0.15)'
                                    : enrollment.status === 'COMPLETED'
                                    ? 'rgba(33, 150, 243, 0.15)'
                                    : 'rgba(244, 67, 54, 0.15)',
                                  color: enrollment.status === 'ACTIVE'
                                    ? 'var(--success)'
                                    : enrollment.status === 'COMPLETED'
                                    ? '#1976d2'
                                    : 'var(--danger)'
                                }}>
                                  {enrollment.status === 'ACTIVE' && '🟢 Activo'}
                                  {enrollment.status === 'COMPLETED' && '✓ Completado'}
                                  {enrollment.status === 'CANCELLED' && '✕ Cancelado'}
                                </div>
                              </div>
                            </div>

                              {/* Stats Grid */}
                              <div style={{
                                display: 'grid',
                                gridTemplateColumns: '1fr 1fr',
                                gap: '20px',
                                marginBottom: '20px',
                                paddingBottom: '20px',
                                borderBottom: '1px solid var(--border)'
                              }}>
                                <div>
                                  <p style={{ color: 'var(--muted)', fontSize: '12px', fontWeight: '700', textTransform: 'uppercase', margin: 0, marginBottom: '8px' }}>
                                    Fecha de inscripción
                                  </p>
                                  <p style={{ color: 'var(--text)', fontWeight: '700', margin: 0 }}>
                                    {new Date(enrollment.enrolledAt).toLocaleDateString('es-ES', {
                                      year: 'numeric',
                                      month: 'long',
                                      day: 'numeric'
                                    })}
                                  </p>
                                </div>
                                <div>
                                  <p style={{ color: 'var(--muted)', fontSize: '12px', fontWeight: '700', textTransform: 'uppercase', margin: 0, marginBottom: '8px' }}>
                                    Asistencia
                                  </p>
                                  <p style={{ color: 'var(--text)', fontWeight: '700', margin: 0 }}>
                                    {attendanceRecords.length === 0
                                      ? 'Sin registros'
                                      : `${presentCount}/${attendanceRecords.length} (${attendanceRate}%)`
                                    }
                                  </p>
                                </div>
                              </div>

                              {/* Attendance Records */}
                              {attendanceRecords.length > 0 && (
                                <div>
                                  <p style={{ color: 'var(--text)', fontWeight: '700', margin: 0, marginBottom: '16px' }}>
                                    Registro de asistencias
                                  </p>
                                  <div style={{
                                    display: 'grid',
                                    gridTemplateColumns: 'repeat(auto-fill, minmax(100px, 1fr))',
                                    gap: '8px'
                                  }}>
                                    {attendanceRecords.map(record => (
                                      <div
                                        key={record.id}
                                        style={{
                                          fontSize: '12px',
                                          padding: '12px',
                                          borderRadius: 'var(--card-radius)',
                                          display: 'flex',
                                          alignItems: 'center',
                                          justifyContent: 'space-between',
                                          backgroundColor: record.present
                                            ? 'rgba(76, 175, 80, 0.1)'
                                            : 'rgba(244, 67, 54, 0.1)',
                                          color: record.present
                                            ? 'var(--success)'
                                            : 'var(--danger)',
                                          border: `1px solid ${record.present ? 'rgba(76, 175, 80, 0.3)' : 'rgba(244, 67, 54, 0.3)'}`,
                                          fontWeight: '600'
                                        }}
                                      >
                                        <span>{new Date(record.recordedAt).toLocaleDateString('es-ES', { month: 'short', day: 'numeric' })}</span>
                                        <span style={{ fontWeight: '800', marginLeft: '8px' }}>
                                          {record.present ? '✓' : '✗'}
                                        </span>
                                      </div>
                                    ))}
                                  </div>
                                </div>
                              )}
                            </div>
                          )
                        })}
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
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
