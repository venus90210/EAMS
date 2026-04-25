'use client'

import { useState } from 'react'
import { AttendanceStudent } from '@/hooks/useAttendanceSessions'

interface AttendanceListProps {
  students: AttendanceStudent[]
  sessionId: string
  onRecordAttendance: (sessionId: string, enrollmentId: string, present: boolean, observations?: string) => Promise<void>
  isLoading: boolean
}

export function AttendanceList({
  students,
  sessionId,
  onRecordAttendance,
  isLoading,
}: AttendanceListProps) {
  const [touchCounts, setTouchCounts] = useState<Record<string, number>>({})
  const [expandedStudent, setExpandedStudent] = useState<string | null>(null)
  const [observations, setObservations] = useState<Record<string, string>>({})
  const [recordingIds, setRecordingIds] = useState<Set<string>>(new Set())

  const getTouchCount = (enrollmentId: string) => touchCounts[enrollmentId] || 0
  const canToggle = (enrollmentId: string) => getTouchCount(enrollmentId) < 3

  const handleToggleAttendance = async (student: AttendanceStudent) => {
    if (!canToggle(student.enrollmentId) || isLoading || recordingIds.has(student.enrollmentId)) {
      return
    }

    try {
      const newTouchCount = getTouchCount(student.enrollmentId) + 1
      setTouchCounts(prev => ({ ...prev, [student.enrollmentId]: newTouchCount }))

      setRecordingIds(prev => new Set(prev).add(student.enrollmentId))

      const obs = observations[student.enrollmentId] || ''
      await onRecordAttendance(sessionId, student.enrollmentId, !student.present, obs)
    } catch (err) {
      console.error(err)
    } finally {
      setRecordingIds(prev => {
        const newSet = new Set(prev)
        newSet.delete(student.enrollmentId)
        return newSet
      })
    }
  }

  const handleSaveObservations = async (student: AttendanceStudent) => {
    try {
      setRecordingIds(prev => new Set(prev).add(student.enrollmentId))
      const obs = observations[student.enrollmentId] || ''
      await onRecordAttendance(sessionId, student.enrollmentId, student.present, obs)
      setExpandedStudent(null)
    } catch (err) {
      console.error(err)
    } finally {
      setRecordingIds(prev => {
        const newSet = new Set(prev)
        newSet.delete(student.enrollmentId)
        return newSet
      })
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {students.map(student => {
        const touchCount = getTouchCount(student.enrollmentId)
        const isExpanded = expandedStudent === student.enrollmentId
        const canRecord = canToggle(student.enrollmentId)
        const isRecording = recordingIds.has(student.enrollmentId)

        return (
          <div
            key={student.enrollmentId}
            style={{
              backgroundColor: 'var(--surface)',
              borderRadius: 'var(--card-radius)',
              border: '1px solid var(--border)',
              overflow: 'hidden',
              transition: 'all 0.3s ease',
              boxShadow: isExpanded ? 'var(--card-shadow-hover)' : 'var(--card-shadow)'
            }}
          >
            {/* Student Header */}
            <div
              style={{
                padding: '20px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                cursor: 'pointer',
                backgroundColor: 'var(--bg)',
                borderBottom: isExpanded ? '1px solid var(--border)' : 'none',
                transition: 'all 0.2s ease'
              }}
              onClick={() => {
                setExpandedStudent(isExpanded ? null : student.enrollmentId)
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = 'var(--surface)'
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = 'var(--bg)'
              }}
            >
              <div style={{ flex: 1 }}>
                <p style={{
                  fontSize: '16px',
                  fontWeight: '700',
                  color: 'var(--text)',
                  margin: '0 0 6px 0'
                }}>
                  👤 {student.studentName}
                </p>
                <p style={{
                  fontSize: '13px',
                  color: 'var(--muted)',
                  margin: 0
                }}>
                  ID: {student.studentId}
                </p>
              </div>

              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '20px'
              }}>
                {/* Attendance Button */}
                <button
                  onClick={(e) => {
                    e.stopPropagation()
                    handleToggleAttendance(student)
                  }}
                  disabled={!canRecord || isLoading || isRecording}
                  style={{
                    padding: '10px 20px',
                    borderRadius: '6px',
                    fontWeight: '700',
                    fontSize: '13px',
                    border: 'none',
                    cursor: !canRecord || isLoading || isRecording ? 'not-allowed' : 'pointer',
                    transition: 'all 0.2s',
                    backgroundColor: student.present
                      ? 'var(--success)'
                      : 'var(--danger)',
                    color: 'white',
                    opacity: !canRecord || isLoading || isRecording ? 0.6 : 1,
                    textTransform: 'uppercase',
                    letterSpacing: '0.5px'
                  }}
                  onMouseEnter={(e) => {
                    if (!(!canRecord || isLoading || isRecording)) {
                      e.currentTarget.style.transform = 'translateY(-2px)'
                      e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)'
                    }
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)'
                    e.currentTarget.style.boxShadow = 'none'
                  }}
                >
                  {isRecording ? '⏳' : student.present ? '✓ Presente' : '✕ Ausente'}
                </button>

                {/* Touch Count Badge */}
                <div style={{
                  textAlign: 'center',
                  padding: '8px 16px',
                  backgroundColor: canRecord ? 'var(--bg)' : 'rgba(244, 67, 54, 0.1)',
                  borderRadius: '8px',
                  minWidth: '60px'
                }}>
                  <p style={{
                    fontSize: '11px',
                    color: 'var(--muted)',
                    fontWeight: '700',
                    margin: '0 0 4px 0',
                    textTransform: 'uppercase',
                    letterSpacing: '0.5px'
                  }}>
                    Toques
                  </p>
                  <p style={{
                    fontSize: '18px',
                    fontWeight: '900',
                    color: canRecord ? 'var(--text)' : 'var(--danger)',
                    margin: 0
                  }}>
                    {touchCount}/3
                  </p>
                </div>

                {/* Expand Icon */}
                <svg
                  style={{
                    width: '20px',
                    height: '20px',
                    color: 'var(--muted)',
                    transition: 'transform 0.3s ease',
                    transform: isExpanded ? 'rotate(180deg)' : 'rotate(0deg)'
                  }}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                </svg>
              </div>
            </div>

            {/* Expanded Section - Observations */}
            {isExpanded && (
              <div style={{
                padding: '24px',
                backgroundColor: 'var(--bg)',
                borderTop: '1px solid var(--border)'
              }}>
                <div style={{ marginBottom: '24px' }}>
                  <label style={{
                    display: 'block',
                    fontSize: '14px',
                    fontWeight: '700',
                    color: 'var(--text)',
                    marginBottom: '12px',
                    textTransform: 'uppercase',
                    letterSpacing: '0.5px'
                  }}>
                    📝 Observaciones
                  </label>
                  <textarea
                    value={observations[student.enrollmentId] || ''}
                    onChange={(e) =>
                      setObservations(prev => ({ ...prev, [student.enrollmentId]: e.target.value }))
                    }
                    placeholder="Ej: Excelente desempeño, participación activa, llegó tarde..."
                    style={{
                      width: '100%',
                      padding: '12px 16px',
                      fontSize: '14px',
                      border: '2px solid var(--border)',
                      borderRadius: 'var(--input-radius)',
                      backgroundColor: 'var(--surface)',
                      color: 'var(--text)',
                      fontFamily: 'inherit',
                      transition: 'all 0.2s',
                      resize: 'vertical',
                      minHeight: '100px'
                    }}
                    rows={4}
                    autoFocus
                    onFocus={(e) => {
                      e.currentTarget.style.borderColor = 'var(--primary)'
                      e.currentTarget.style.boxShadow = '0 0 0 3px rgba(0, 166, 81, 0.1)'
                    }}
                    onBlur={(e) => {
                      e.currentTarget.style.borderColor = 'var(--border)'
                      e.currentTarget.style.boxShadow = 'none'
                    }}
                  />
                  <p style={{
                    fontSize: '12px',
                    color: 'var(--muted)',
                    marginTop: '8px',
                    margin: '8px 0 0 0'
                  }}>
                    💡 Sé específico: comportamiento, desempeño, ausencia justificada, etc.
                  </p>
                </div>

                {/* Action Buttons */}
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '1fr 1fr',
                  gap: '12px'
                }}>
                  <button
                    onClick={() => handleSaveObservations(student)}
                    disabled={isLoading || isRecording}
                    style={{
                      padding: '14px 20px',
                      borderRadius: '4px',
                      fontWeight: '800',
                      fontSize: '14px',
                      border: 'none',
                      cursor: isLoading || isRecording ? 'not-allowed' : 'pointer',
                      backgroundColor: isLoading || isRecording ? 'var(--muted)' : 'var(--primary)',
                      color: 'white',
                      transition: 'all 0.2s',
                      textTransform: 'uppercase',
                      letterSpacing: '0.5px'
                    }}
                    onMouseEnter={(e) => {
                      if (!isLoading && !isRecording) {
                        e.currentTarget.style.transform = 'translateY(-2px)'
                        e.currentTarget.style.boxShadow = '0 6px 16px rgba(0, 166, 81, 0.3)'
                      }
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.transform = 'translateY(0)'
                      e.currentTarget.style.boxShadow = 'none'
                    }}
                  >
                    {isRecording ? '⏳ Guardando' : '💾 Guardar'}
                  </button>
                  <button
                    onClick={() => setExpandedStudent(null)}
                    style={{
                      padding: '14px 20px',
                      borderRadius: '4px',
                      fontWeight: '800',
                      fontSize: '14px',
                      border: '2px solid var(--border)',
                      cursor: 'pointer',
                      backgroundColor: 'transparent',
                      color: 'var(--text)',
                      transition: 'all 0.2s',
                      textTransform: 'uppercase',
                      letterSpacing: '0.5px'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor = 'var(--bg)'
                      e.currentTarget.style.transform = 'translateY(-2px)'
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor = 'transparent'
                      e.currentTarget.style.transform = 'translateY(0)'
                    }}
                  >
                    Cerrar
                  </button>
                </div>
              </div>
            )}
          </div>
        )
      })}
    </div>
  )
}
