'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { useActivities } from '@/hooks/useActivities'
import { useEnrollment } from '@/hooks/useEnrollment'
import { useStudents } from '@/hooks/useStudents'
import { authService } from '@/services/authService'

export function EnrollClient() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const { students, loading: studentsLoading, error: studentsError } = useStudents()
  const { enroll, loading: enrollLoading, error: enrollError } = useEnrollment()
  const { activities } = useActivities()

  const [selectedStudentId, setSelectedStudentId] = useState('')
  const [message, setMessage] = useState('')

  const activityId = searchParams.get('activityId')
  const selectedActivity = activities.find(a => a.id === activityId)

  console.log('[EnrollClient] State:', { authLoading, isAuthenticated, userId: user?.id, students: students.length, studentsLoading, studentsError })

  if (authLoading) {
    return <div className="flex items-center justify-center min-h-screen"><p style={{ color: 'var(--muted)' }}>Cargando...</p></div>
  }

  // If not authenticated in context, but we have a token stored, wait for context to update
  if (!isAuthenticated && authService.getAccessToken()) {
    return <div className="flex items-center justify-center min-h-screen"><p style={{ color: 'var(--muted)' }}>Cargando sesión...</p></div>
  }

  if (!isAuthenticated || user?.role !== 'GUARDIAN') {
    router.push('/login')
    return null
  }

  if (!activityId || !selectedActivity) {
    return (
      <div style={{ backgroundColor: '#f5f5f5', minHeight: '100vh' }}>
        <section style={{
          background: 'linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%)',
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
              ACTIVIDAD NO ENCONTRADA
            </h1>
            <p style={{
              fontSize: '16px',
              lineHeight: '1.5',
              opacity: '0.95',
              marginBottom: '24px'
            }}>
              No pudimos encontrar la actividad que buscas. Por favor, intenta nuevamente.
            </p>
          </div>
        </section>

        <section style={{
          padding: '80px 40px',
          background: 'white',
          textAlign: 'center'
        }}>
          <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
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
        </section>

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

  const handleEnroll = async () => {
    if (!selectedStudentId) {
      setMessage('Por favor selecciona un estudiante')
      return
    }

    try {
      await enroll(selectedStudentId, activityId)
      setMessage('¡Inscripción exitosa!')
      setTimeout(() => router.push('/guardian/tracking'), 2000)
    } catch (err) {
      setMessage(enrollError?.message || 'Error al inscribirse')
    }
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
                INSCRIBIR ESTUDIANTE
              </h1>
              <p style={{
                fontSize: '16px',
                lineHeight: '1.5',
                opacity: '0.95'
              }}>
                Completa los pasos para registrar a tu estudiante en una actividad extracurricular
              </p>
            </div>
            <div style={{
              fontSize: '80px',
              textAlign: 'center',
              opacity: '0.8',
              flexShrink: 0
            }}>
              📝
            </div>
          </div>
        </div>
      </section>

      {/* Process Steps Indicator */}
      <section style={{
        padding: '40px',
        background: 'var(--bg)',
        borderBottom: '1px solid var(--border)'
      }}>
        <div style={{ maxWidth: '800px', margin: '0 auto' }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '16px'
          }}>
            {/* Step 1 */}
            <div style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              flex: 1
            }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                backgroundColor: 'var(--primary)',
                color: 'white',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: '900',
                fontSize: '20px',
                marginBottom: '8px'
              }}>
                1
              </div>
              <p style={{
                fontSize: '12px',
                fontWeight: '700',
                color: 'var(--text)',
                margin: 0,
                textAlign: 'center'
              }}>
                Seleccionar
              </p>
            </div>

            {/* Connector */}
            <div style={{
              flex: 0.5,
              height: '2px',
              backgroundColor: 'var(--border)',
              marginBottom: '32px'
            }} />

            {/* Step 2 */}
            <div style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              flex: 1
            }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                backgroundColor: 'var(--primary)',
                color: 'white',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: '900',
                fontSize: '20px',
                marginBottom: '8px'
              }}>
                2
              </div>
              <p style={{
                fontSize: '12px',
                fontWeight: '700',
                color: 'var(--text)',
                margin: 0,
                textAlign: 'center'
              }}>
                Confirmar
              </p>
            </div>

            {/* Connector */}
            <div style={{
              flex: 0.5,
              height: '2px',
              backgroundColor: 'var(--border)',
              marginBottom: '32px'
            }} />

            {/* Step 3 */}
            <div style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              flex: 1
            }}>
              <div style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                backgroundColor: 'var(--muted)',
                color: 'white',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: '900',
                fontSize: '20px',
                marginBottom: '8px'
              }}>
                ✓
              </div>
              <p style={{
                fontSize: '12px',
                fontWeight: '700',
                color: 'var(--muted)',
                margin: 0,
                textAlign: 'center'
              }}>
                Completado
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Content Section */}
      <section style={{
        padding: '80px 40px',
        background: 'white'
      }}>
        <div style={{ maxWidth: '800px', margin: '0 auto' }}>
          {/* Paso 1: Selecciona el estudiante */}
          <div style={{
            backgroundColor: 'var(--surface)',
            borderRadius: 'var(--card-radius)',
            border: '1px solid var(--border)',
            padding: '32px',
            marginBottom: '32px'
          }}>
            <div style={{ marginBottom: '24px' }}>
              <h2 style={{
                fontSize: '20px',
                fontWeight: '800',
                color: 'var(--primary)',
                margin: '0 0 8px 0',
                textTransform: 'uppercase',
                letterSpacing: '0.5px'
              }}>
                Paso 1
              </h2>
              <h3 style={{
                fontSize: '24px',
                fontWeight: '900',
                color: 'var(--text)',
                margin: '0'
              }}>
                Selecciona el estudiante
              </h3>
            </div>

            {studentsLoading ? (
              <p style={{ color: 'var(--muted)', fontSize: '16px' }}>⏳ Cargando estudiantes...</p>
            ) : studentsError ? (
              <div style={{
                padding: '16px',
                borderRadius: 'var(--card-radius)',
                backgroundColor: '#ffebee',
                borderLeft: '4px solid var(--danger)',
                color: '#c62828',
                fontWeight: '600'
              }}>
                ⚠️ Error al cargar estudiantes: {studentsError}
              </div>
            ) : students.length === 0 ? (
              <div style={{
                padding: '16px',
                borderRadius: 'var(--card-radius)',
                backgroundColor: '#fff3cd',
                borderLeft: '4px solid var(--warning)',
                color: '#856404',
                fontWeight: '600'
              }}>
                ⚠️ No tienes estudiantes asociados
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {students.map(student => (
                  <label
                    key={student.id}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      padding: '16px',
                      borderRadius: 'var(--card-radius)',
                      cursor: 'pointer',
                      transition: 'all 0.2s ease',
                      backgroundColor: selectedStudentId === student.id ? 'var(--primary)' : 'var(--bg)',
                      color: selectedStudentId === student.id ? 'white' : 'var(--text)',
                      border: `2px solid ${selectedStudentId === student.id ? 'var(--primary)' : 'var(--border)'}`,
                      gap: '12px'
                    }}
                  >
                    <input
                      type="radio"
                      name="student"
                      value={student.id}
                      checked={selectedStudentId === student.id}
                      onChange={e => setSelectedStudentId(e.target.value)}
                      style={{
                        cursor: 'pointer',
                        width: '18px',
                        height: '18px'
                      }}
                    />
                    <div>
                      <p style={{
                        fontSize: '16px',
                        fontWeight: '700',
                        margin: '0 0 4px 0'
                      }}>
                        {student.firstName} {student.lastName}
                      </p>
                      {student.grade && (
                        <p style={{
                          fontSize: '14px',
                          opacity: selectedStudentId === student.id ? 0.9 : 0.7,
                          margin: '0'
                        }}>
                          Grado {student.grade}
                        </p>
                      )}
                    </div>
                  </label>
                ))}
              </div>
            )}
          </div>

          {/* Paso 2: Confirma la actividad */}
          <div style={{
            backgroundColor: 'var(--surface)',
            borderRadius: 'var(--card-radius)',
            border: '1px solid var(--border)',
            padding: '32px',
            marginBottom: '32px'
          }}>
            <div style={{ marginBottom: '24px' }}>
              <h2 style={{
                fontSize: '20px',
                fontWeight: '800',
                color: 'var(--primary)',
                margin: '0 0 8px 0',
                textTransform: 'uppercase',
                letterSpacing: '0.5px'
              }}>
                Paso 2
              </h2>
              <h3 style={{
                fontSize: '24px',
                fontWeight: '900',
                color: 'var(--text)',
                margin: '0'
              }}>
                Confirma la actividad
              </h3>
            </div>

            <div style={{
              backgroundColor: 'var(--bg)',
              borderRadius: 'var(--card-radius)',
              border: '1px solid var(--border)',
              padding: '24px',
              marginBottom: '24px'
            }}>
              <p style={{
                color: 'var(--muted)',
                fontSize: '12px',
                fontWeight: '700',
                textTransform: 'uppercase',
                margin: '0 0 12px 0',
                letterSpacing: '0.5px'
              }}>
                Actividad seleccionada
              </p>
              <h4 style={{
                fontSize: '28px',
                fontWeight: '900',
                color: 'var(--primary)',
                margin: '0 0 16px 0'
              }}>
                {selectedActivity.name}
              </h4>
              {selectedActivity.description && (
                <p style={{
                  color: 'var(--text)',
                  fontSize: '15px',
                  lineHeight: '1.6',
                  margin: 0
                }}>
                  {selectedActivity.description}
                </p>
              )}
            </div>

            <div style={{
              display: 'grid',
              gridTemplateColumns: '1fr 1fr',
              gap: '16px'
            }}>
              <div
                style={{
                  backgroundColor: 'var(--primary)',
                  color: 'white',
                  padding: '20px',
                  borderRadius: 'var(--card-radius)'
                }}
              >
                <p style={{
                  fontSize: '12px',
                  opacity: 0.9,
                  fontWeight: '700',
                  textTransform: 'uppercase',
                  margin: '0 0 8px 0',
                  letterSpacing: '0.5px'
                }}>
                  Cupos disponibles
                </p>
                <p style={{
                  fontSize: '32px',
                  fontWeight: '900',
                  margin: '0'
                }}>
                  {selectedActivity.availableSpots}/{selectedActivity.totalSpots}
                </p>
              </div>
              <div
                style={{
                  backgroundColor: selectedActivity.availableSpots > 0 ? 'var(--success)' : 'var(--danger)',
                  color: 'white',
                  padding: '20px',
                  borderRadius: 'var(--card-radius)'
                }}
              >
                <p style={{
                  fontSize: '12px',
                  opacity: 0.9,
                  fontWeight: '700',
                  textTransform: 'uppercase',
                  margin: '0 0 8px 0',
                  letterSpacing: '0.5px'
                }}>
                  Estado
                </p>
                <p style={{
                  fontSize: '20px',
                  fontWeight: '900',
                  margin: '0'
                }}>
                  {selectedActivity.availableSpots > 0 ? '✓ Disponible' : '✕ Lleno'}
                </p>
              </div>
            </div>
          </div>

          {/* Mensaje */}
          {message && (
            <div
              style={{
                padding: '16px',
                borderRadius: 'var(--card-radius)',
                backgroundColor: message.includes('exitosa') ? '#dcfce7' : '#ffebee',
                color: message.includes('exitosa') ? '#166534' : '#c62828',
                border: `1px solid ${message.includes('exitosa') ? '#86efac' : '#fca5a5'}`,
                borderLeft: `4px solid ${message.includes('exitosa') ? 'var(--success)' : 'var(--danger)'}`,
                fontWeight: '600',
                marginBottom: '32px'
              }}
            >
              {message.includes('exitosa') ? '✓ ' : '⚠️ '}
              {message}
            </div>
          )}

          {/* Botones */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '16px'
          }}>
            <button
              onClick={() => router.back()}
              style={{
                padding: '14px 24px',
                fontSize: '14px',
                fontWeight: '800',
                backgroundColor: 'var(--muted)',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                transition: 'all 0.2s',
                textTransform: 'uppercase'
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
              Cancelar
            </button>
            <button
              onClick={handleEnroll}
              disabled={!selectedStudentId || enrollLoading || selectedActivity.availableSpots === 0}
              style={{
                padding: '14px 24px',
                fontSize: '14px',
                fontWeight: '800',
                backgroundColor: !selectedStudentId || enrollLoading || selectedActivity.availableSpots === 0
                  ? 'var(--muted)'
                  : 'var(--secondary)',
                color: !selectedStudentId || enrollLoading || selectedActivity.availableSpots === 0 ? 'white' : '#000',
                border: 'none',
                borderRadius: '4px',
                cursor: !selectedStudentId || enrollLoading || selectedActivity.availableSpots === 0 ? 'not-allowed' : 'pointer',
                transition: 'all 0.2s',
                textTransform: 'uppercase'
              }}
              onMouseEnter={(e) => {
                if (!(!selectedStudentId || enrollLoading || selectedActivity.availableSpots === 0)) {
                  e.currentTarget.style.transform = 'translateY(-2px)'
                  e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.1)'
                }
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)'
                e.currentTarget.style.boxShadow = 'none'
              }}
            >
              {enrollLoading ? '⏳ Inscribiendo...' : '✓ Confirmar inscripción'}
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
