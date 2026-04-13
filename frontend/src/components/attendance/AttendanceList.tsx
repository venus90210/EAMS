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
      // Error handled by hook
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
    <div className="space-y-3">
      {students.map(student => {
        const touchCount = getTouchCount(student.enrollmentId)
        const isExpanded = expandedStudent === student.enrollmentId
        const canRecord = canToggle(student.enrollmentId)
        const isRecording = recordingIds.has(student.enrollmentId)

        return (
          <div key={student.enrollmentId} className="bg-white rounded-lg border border-gray-200 overflow-hidden">
            <div
              className="p-4 flex items-center justify-between hover:bg-gray-50 cursor-pointer transition"
              onClick={() => {
                if (isExpanded) {
                  setExpandedStudent(null)
                } else {
                  setExpandedStudent(student.enrollmentId)
                }
              }}
            >
              <div className="flex-1">
                <p className="font-medium text-gray-900">{student.studentName}</p>
                <p className="text-sm text-gray-500">ID: {student.studentId}</p>
              </div>

              <div className="flex items-center gap-4">
                <button
                  onClick={e => {
                    e.stopPropagation()
                    handleToggleAttendance(student)
                  }}
                  disabled={!canRecord || isLoading || isRecording}
                  className={`px-4 py-2 rounded-md font-medium transition ${
                    student.present
                      ? 'bg-green-600 text-white hover:bg-green-700 disabled:bg-gray-400'
                      : 'bg-red-600 text-white hover:bg-red-700 disabled:bg-gray-400'
                  }`}
                >
                  {isRecording ? 'Guardando...' : student.present ? 'Presente' : 'Ausente'}
                </button>

                <div className="text-right">
                  <p className="text-xs text-gray-500">Toques</p>
                  <p className={`text-lg font-bold ${canRecord ? 'text-gray-900' : 'text-red-600'}`}>
                    {touchCount}/3
                  </p>
                </div>

                <svg
                  className={`w-5 h-5 text-gray-400 transition-transform ${isExpanded ? 'rotate-180' : ''}`}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                </svg>
              </div>
            </div>

            {isExpanded && (
              <div className="border-t border-gray-200 p-4 bg-gray-50">
                <label className="block text-sm font-medium text-gray-700 mb-2">Observaciones</label>
                <textarea
                  value={observations[student.enrollmentId] || ''}
                  onChange={e =>
                    setObservations(prev => ({ ...prev, [student.enrollmentId]: e.target.value }))
                  }
                  placeholder="Añade notas sobre la asistencia de este estudiante"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-blue-500 focus:border-blue-500"
                  rows={3}
                />
                <div className="mt-4 flex gap-2">
                  <button
                    onClick={() => handleSaveObservations(student)}
                    disabled={isLoading || isRecording}
                    className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md font-medium hover:bg-blue-700 disabled:bg-gray-400"
                  >
                    {isRecording ? 'Guardando...' : 'Guardar observaciones'}
                  </button>
                  <button
                    onClick={() => setExpandedStudent(null)}
                    className="flex-1 bg-gray-300 text-gray-700 py-2 px-4 rounded-md font-medium hover:bg-gray-400"
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
