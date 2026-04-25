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
    <form onSubmit={handleSubmit(handleFormSubmit)}>


      {error && (
        <div style={{
          marginBottom: '24px',
          padding: '16px',
          borderRadius: 'var(--card-radius)',
          backgroundColor: '#ffebee',
          borderLeft: '4px solid var(--danger)',
          color: '#c62828',
          fontWeight: '600',
          fontSize: '14px'
        }}>
          ⚠️ {error}
        </div>
      )}

      <div style={{ marginBottom: '24px' }}>
        <label htmlFor="name" style={{
          display: 'block',
          fontSize: '14px',
          fontWeight: '700',
          color: 'var(--text)',
          marginBottom: '12px',
          textTransform: 'uppercase',
          letterSpacing: '0.5px'
        }}>
          Nombre
        </label>
        <input
          id="name"
          type="text"
          {...register('name')}
          style={{
            width: '100%',
            padding: '12px 16px',
            fontSize: '15px',
            border: '2px solid var(--border)',
            borderRadius: 'var(--input-radius)',
            backgroundColor: 'var(--bg)',
            color: 'var(--text)',
            transition: 'all 0.2s',
            fontFamily: 'inherit'
          }}
          disabled={isSubmitting || loading}
          onFocus={(e) => {
            e.currentTarget.style.borderColor = 'var(--primary)'
            e.currentTarget.style.boxShadow = '0 0 0 3px rgba(0, 166, 81, 0.1)'
          }}
          onBlur={(e) => {
            e.currentTarget.style.borderColor = 'var(--border)'
            e.currentTarget.style.boxShadow = 'none'
          }}
        />
        {errors.name && <span style={{ color: 'var(--danger)', fontSize: '13px', marginTop: '6px', display: 'block' }}>{errors.name.message}</span>}
      </div>

      <div style={{ marginBottom: '24px' }}>
        <label htmlFor="description" style={{
          display: 'block',
          fontSize: '14px',
          fontWeight: '700',
          color: 'var(--text)',
          marginBottom: '12px',
          textTransform: 'uppercase',
          letterSpacing: '0.5px'
        }}>
          Descripción
        </label>
        <textarea
          id="description"
          {...register('description')}
          rows={4}
          style={{
            width: '100%',
            padding: '12px 16px',
            fontSize: '15px',
            border: '2px solid var(--border)',
            borderRadius: 'var(--input-radius)',
            backgroundColor: 'var(--bg)',
            color: 'var(--text)',
            transition: 'all 0.2s',
            fontFamily: 'inherit',
            resize: 'vertical',
            minHeight: '120px'
          }}
          disabled={isSubmitting || loading}
          onFocus={(e) => {
            e.currentTarget.style.borderColor = 'var(--primary)'
            e.currentTarget.style.boxShadow = '0 0 0 3px rgba(0, 166, 81, 0.1)'
          }}
          onBlur={(e) => {
            e.currentTarget.style.borderColor = 'var(--border)'
            e.currentTarget.style.boxShadow = 'none'
          }}
        />
        {errors.description && (
          <span style={{ color: 'var(--danger)', fontSize: '13px', marginTop: '6px', display: 'block' }}>{errors.description.message}</span>
        )}
      </div>

      <div style={{ marginBottom: '32px' }}>
        <label htmlFor="totalSpots" style={{
          display: 'block',
          fontSize: '14px',
          fontWeight: '700',
          color: 'var(--text)',
          marginBottom: '12px',
          textTransform: 'uppercase',
          letterSpacing: '0.5px'
        }}>
          Cupos totales
        </label>
        <input
          id="totalSpots"
          type="number"
          {...register('totalSpots', { valueAsNumber: true })}
          min={1}
          style={{
            width: '100%',
            padding: '12px 16px',
            fontSize: '15px',
            border: '2px solid var(--border)',
            borderRadius: 'var(--input-radius)',
            backgroundColor: 'var(--bg)',
            color: 'var(--text)',
            transition: 'all 0.2s',
            fontFamily: 'inherit'
          }}
          disabled={isSubmitting || loading}
          onFocus={(e) => {
            e.currentTarget.style.borderColor = 'var(--primary)'
            e.currentTarget.style.boxShadow = '0 0 0 3px rgba(0, 166, 81, 0.1)'
          }}
          onBlur={(e) => {
            e.currentTarget.style.borderColor = 'var(--border)'
            e.currentTarget.style.boxShadow = 'none'
          }}
        />
        {errors.totalSpots && (
          <span style={{ color: 'var(--danger)', fontSize: '13px', marginTop: '6px', display: 'block' }}>{errors.totalSpots.message}</span>
        )}
      </div>

      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: '12px'
      }}>
        <button
          type="submit"
          disabled={isSubmitting || loading}
          style={{
            padding: '14px 24px',
            fontSize: '14px',
            fontWeight: '800',
            backgroundColor: isSubmitting || loading ? 'var(--muted)' : 'var(--primary)',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: isSubmitting || loading ? 'not-allowed' : 'pointer',
            transition: 'all 0.2s',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            boxShadow: '0 4px 12px rgba(0, 166, 81, 0.2)'
          }}
          onMouseEnter={(e) => {
            if (!isSubmitting && !loading) {
              e.currentTarget.style.transform = 'translateY(-2px)'
              e.currentTarget.style.boxShadow = '0 6px 16px rgba(0, 166, 81, 0.3)'
            }
          }}
          onMouseLeave={(e) => {
            if (!isSubmitting && !loading) {
              e.currentTarget.style.transform = 'translateY(0)'
              e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 166, 81, 0.2)'
            }
          }}
        >
          {isSubmitting ? '⏳ Guardando...' : activity ? '💾 Guardar cambios' : '✓ Crear actividad'}
        </button>
        <button
          type="button"
          onClick={onCancel}
          disabled={isSubmitting || loading}
          style={{
            padding: '14px 24px',
            fontSize: '14px',
            fontWeight: '800',
            backgroundColor: 'transparent',
            color: 'var(--text)',
            border: '2px solid var(--border)',
            borderRadius: '4px',
            cursor: isSubmitting || loading ? 'not-allowed' : 'pointer',
            transition: 'all 0.2s',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            opacity: isSubmitting || loading ? 0.6 : 1
          }}
          onMouseEnter={(e) => {
            if (!isSubmitting && !loading) {
              e.currentTarget.style.backgroundColor = 'var(--bg)'
              e.currentTarget.style.transform = 'translateY(-2px)'
            }
          }}
          onMouseLeave={(e) => {
            if (!isSubmitting && !loading) {
              e.currentTarget.style.backgroundColor = 'transparent'
              e.currentTarget.style.transform = 'translateY(0)'
            }
          }}
        >
          ✕ Cancelar
        </button>
      </div>
    </form>
  )
}
