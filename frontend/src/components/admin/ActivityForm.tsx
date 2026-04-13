'use client'

import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { Activity } from '@/types'

const activitySchema = z.object({
  name: z.string().min(1, 'El nombre es requerido'),
  description: z.string().min(1, 'La descripción es requerida'),
  totalSpots: z.number().min(1, 'Debe haber al menos 1 cupo disponible'),
})

type ActivityFormData = z.infer<typeof activitySchema>

interface ActivityFormProps {
  activity?: Activity
  loading?: boolean
  error?: string | null
  onSubmit: (data: ActivityFormData) => Promise<void>
  onCancel: () => void
}

export function ActivityForm({
  activity,
  loading = false,
  error,
  onSubmit,
  onCancel,
}: ActivityFormProps) {
  const [isSubmitting, setIsSubmitting] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<ActivityFormData>({
    resolver: zodResolver(activitySchema),
    defaultValues: {
      name: activity?.name || '',
      description: activity?.description || '',
      totalSpots: activity?.totalSpots || 20,
    },
  })

  useEffect(() => {
    if (activity) {
      reset({
        name: activity.name,
        description: activity.description,
        totalSpots: activity.totalSpots,
      })
    }
  }, [activity, reset])

  const handleFormSubmit = async (data: ActivityFormData) => {
    try {
      setIsSubmitting(true)
      await onSubmit(data)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="bg-white rounded-lg shadow p-6 max-w-2xl">
      <h2 className="text-xl font-bold mb-6">
        {activity ? 'Editar actividad' : 'Nueva actividad'}
      </h2>

      {error && (
        <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded text-sm">
          {error}
        </div>
      )}

      <div className="mb-4">
        <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
          Nombre
        </label>
        <input
          id="name"
          type="text"
          {...register('name')}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          disabled={isSubmitting || loading}
        />
        {errors.name && <span className="text-red-500 text-sm">{errors.name.message}</span>}
      </div>

      <div className="mb-4">
        <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
          Descripción
        </label>
        <textarea
          id="description"
          {...register('description')}
          rows={4}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          disabled={isSubmitting || loading}
        />
        {errors.description && (
          <span className="text-red-500 text-sm">{errors.description.message}</span>
        )}
      </div>

      <div className="mb-6">
        <label htmlFor="totalSpots" className="block text-sm font-medium text-gray-700 mb-2">
          Cupos totales
        </label>
        <input
          id="totalSpots"
          type="number"
          {...register('totalSpots', { valueAsNumber: true })}
          min={1}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          disabled={isSubmitting || loading}
        />
        {errors.totalSpots && (
          <span className="text-red-500 text-sm">{errors.totalSpots.message}</span>
        )}
      </div>

      <div className="flex gap-3">
        <button
          type="submit"
          disabled={isSubmitting || loading}
          className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md font-medium hover:bg-blue-700 disabled:bg-gray-400"
        >
          {isSubmitting ? 'Guardando...' : activity ? 'Guardar cambios' : 'Crear actividad'}
        </button>
        <button
          type="button"
          onClick={onCancel}
          disabled={isSubmitting || loading}
          className="flex-1 bg-gray-300 text-gray-700 py-2 px-4 rounded-md font-medium hover:bg-gray-400 disabled:bg-gray-200"
        >
          Cancelar
        </button>
      </div>
    </form>
  )
}
