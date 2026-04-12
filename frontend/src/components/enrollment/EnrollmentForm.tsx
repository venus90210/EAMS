'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useEnrollment } from '@/hooks/useEnrollment'
import { Student, Activity } from '@/types'

const enrollmentSchema = z.object({
  studentId: z.string().min(1, 'Selecciona un hijo'),
  activityId: z.string().min(1, 'Selecciona una actividad'),
})

type EnrollmentFormData = z.infer<typeof enrollmentSchema>

interface EnrollmentFormProps {
  students: Student[]
  activity: Activity
  onSuccess?: () => void
  onCancel?: () => void
}

export function EnrollmentForm({
  students,
  activity,
  onSuccess,
  onCancel,
}: EnrollmentFormProps) {
  const { enroll, enrolling, error } = useEnrollment()
  const [showConfirmation, setShowConfirmation] = useState(false)
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<EnrollmentFormData>({
    resolver: zodResolver(enrollmentSchema),
    defaultValues: { activityId: activity.id },
  })

  const onSubmit = async (data: EnrollmentFormData) => {
    try {
      const student = students.find((s) => s.id === data.studentId)
      setSelectedStudent(student || null)
      setShowConfirmation(true)
    } catch (err) {
      console.error(err)
    }
  }

  const handleConfirm = async () => {
    if (!selectedStudent) return

    try {
      await enroll(selectedStudent.id, activity.id)
      setShowConfirmation(false)
      onSuccess?.()
    } catch (err) {
      // Error is handled by the hook
      console.error(err)
    }
  }

  if (showConfirmation && selectedStudent) {
    return (
      <div className="bg-white rounded-lg shadow p-6 max-w-md mx-auto">
        <h2 className="text-xl font-bold mb-4">Confirmar inscripción</h2>

        <div className="space-y-4 mb-6 p-4 bg-gray-50 rounded">
          <div>
            <p className="text-sm text-gray-600">Estudiante</p>
            <p className="font-medium text-gray-900">{selectedStudent.name}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Actividad</p>
            <p className="font-medium text-gray-900">{activity.name}</p>
          </div>
          <div>
            <p className="text-sm text-gray-600">Horario</p>
            <p className="font-medium text-gray-900">
              {activity.schedule.dayOfWeek} {activity.schedule.startTime} - {activity.schedule.endTime}
            </p>
          </div>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded text-sm">
            <p className="font-medium">{error.code}</p>
            <p>{error.message}</p>
          </div>
        )}

        <div className="flex gap-3">
          <button
            onClick={handleConfirm}
            disabled={enrolling}
            className="flex-1 bg-green-600 text-white py-2 px-4 rounded-md font-medium hover:bg-green-700 disabled:bg-gray-400"
          >
            {enrolling ? 'Inscribiendo...' : 'Confirmar'}
          </button>
          <button
            onClick={() => {
              setShowConfirmation(false)
              setSelectedStudent(null)
            }}
            disabled={enrolling}
            className="flex-1 bg-gray-300 text-gray-700 py-2 px-4 rounded-md font-medium hover:bg-gray-400 disabled:bg-gray-200"
          >
            Cancelar
          </button>
        </div>
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="bg-white rounded-lg shadow p-6 max-w-md mx-auto">
      <h2 className="text-xl font-bold mb-6">Inscribir a {activity.name}</h2>

      <div className="mb-4">
        <label htmlFor="studentId" className="block text-sm font-medium text-gray-700 mb-2">
          Selecciona un hijo
        </label>
        <select
          id="studentId"
          {...register('studentId')}
          className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
        >
          <option value="">-- Selecciona un hijo --</option>
          {students.map((student) => (
            <option key={student.id} value={student.id}>
              {student.name}
            </option>
          ))}
        </select>
        {errors.studentId && <span className="text-red-500 text-sm">{errors.studentId.message}</span>}
      </div>

      <div className="mb-6 p-4 bg-blue-50 rounded">
        <p className="text-sm text-blue-900">
          <strong>Actividad:</strong> {activity.name}
        </p>
        <p className="text-sm text-blue-900 mt-2">
          <strong>Cupos disponibles:</strong> {activity.availableSpots}/{activity.totalSpots}
        </p>
      </div>

      <div className="flex gap-3">
        <button
          type="submit"
          disabled={students.length === 0}
          className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md font-medium hover:bg-blue-700 disabled:bg-gray-400"
        >
          Siguiente
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="flex-1 bg-gray-300 text-gray-700 py-2 px-4 rounded-md font-medium hover:bg-gray-400"
        >
          Cancelar
        </button>
      </div>
    </form>
  )
}
