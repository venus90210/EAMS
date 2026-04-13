'use client'

import React, { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useAuth } from '@/hooks/useAuth'

const loginSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(1, 'La contraseña es requerida'),
})

type LoginFormData = z.infer<typeof loginSchema>

interface LoginFormProps {
  onMfaRequired?: (sessionToken: string) => void
  onSuccess?: () => void
}

export function LoginForm({ onMfaRequired, onSuccess }: LoginFormProps) {
  const { login } = useAuth()
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  })

  const onSubmit = async (data: LoginFormData) => {
    try {
      setIsLoading(true)
      setError(null)

      console.log('[LoginForm] Submitting login for:', data.email)
      const sessionToken = await login(data.email, data.password)
      console.log('[LoginForm] Login returned sessionToken:', sessionToken)

      if (sessionToken) {
        console.log('[LoginForm] MFA required')
        onMfaRequired?.(sessionToken)
      } else {
        console.log('[LoginForm] Calling onSuccess')
        onSuccess?.()
      }
    } catch (err: any) {
      console.log('[LoginForm] Login error:', err)
      setError(err.response?.data?.message || err.message || 'Login failed')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="card p-8 space-y-6">
      <div>
        <h1 className="text-4xl font-bold mb-2" style={{ color: 'var(--text)' }}>
          Iniciar sesión
        </h1>
        <p style={{ color: 'var(--muted)' }}>Accede a EAMS para gestionar tus actividades</p>
      </div>

      {error && (
        <div className="p-4 rounded" style={{ backgroundColor: '#FEE2E2', color: '#991B1B', borderLeft: '4px solid #DC2626' }}>
          <p className="text-sm font-medium">{error}</p>
        </div>
      )}

      <div className="space-y-2">
        <label htmlFor="email" className="block text-sm font-semibold" style={{ color: 'var(--text)' }}>
          Email o teléfono
        </label>
        <input
          id="email"
          type="email"
          {...register('email')}
          className="input-field"
          placeholder="correo@ejemplo.com"
        />
        {errors.email && <p className="text-red-600 text-sm mt-1">{errors.email.message}</p>}
      </div>

      <div className="space-y-2">
        <div className="flex justify-between items-center">
          <label htmlFor="password" className="block text-sm font-semibold" style={{ color: 'var(--text)' }}>
            Contraseña
          </label>
          <a href="#" className="text-sm" style={{ color: 'var(--primary)' }}>
            ¿Olvidaste la contraseña?
          </a>
        </div>
        <input
          id="password"
          type="password"
          {...register('password')}
          className="input-field"
          placeholder="••••••••"
        />
        {errors.password && <p className="text-red-600 text-sm mt-1">{errors.password.message}</p>}
      </div>

      <button
        type="submit"
        disabled={isLoading}
        className="btn-primary w-full"
      >
        {isLoading ? 'Iniciando sesión...' : 'Iniciar sesión'}
      </button>

      <div className="relative py-4">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t" style={{ borderColor: 'var(--border)' }}></div>
        </div>
        <div className="relative flex justify-center text-sm">
          <span style={{ color: 'var(--muted)', backgroundColor: 'var(--surface)' }} className="px-2">
            o
          </span>
        </div>
      </div>

      <button
        type="button"
        className="btn-secondary w-full"
      >
        🍎 Iniciar sesión con Apple
      </button>

      <div className="pt-2 text-center">
        <p style={{ color: 'var(--muted)' }} className="text-sm">
          ¿Nuevo en EAMS?{' '}
          <a href="#" style={{ color: 'var(--primary)' }} className="font-semibold">
            Registrarse
          </a>
        </p>
      </div>
    </form>
  )
}
