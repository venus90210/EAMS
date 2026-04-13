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
    <form onSubmit={handleSubmit(onSubmit)} style={{
      backgroundColor: 'var(--surface)',
      borderRadius: 'var(--card-radius)',
      border: '1px solid var(--border)',
      padding: '48px 40px',
      boxShadow: 'var(--elevation-3)'
    }}>
      <div style={{ marginBottom: '32px' }}>
        <h2 style={{
          fontSize: '32px',
          fontWeight: '800',
          color: 'var(--text)',
          marginBottom: '8px'
        }}>
          Iniciar sesión
        </h2>
        <p style={{
          color: 'var(--text-light)',
          fontSize: '16px'
        }}>
          Accede a EAMS para gestionar tus actividades
        </p>
      </div>

      {error && (
        <div style={{
          padding: '16px',
          borderRadius: 'var(--button-radius)',
          backgroundColor: '#ffebee',
          borderLeft: '4px solid var(--danger)',
          color: '#c62828',
          fontWeight: '600',
          fontSize: '14px',
          marginBottom: '24px'
        }}>
          ⚠️ {error}
        </div>
      )}

      <div style={{ marginBottom: '24px' }}>
        <label htmlFor="email" style={{
          display: 'block',
          fontSize: '14px',
          fontWeight: '700',
          color: 'var(--text)',
          marginBottom: '8px'
        }}>
          Email
        </label>
        <input
          id="email"
          type="email"
          {...register('email')}
          className="input-field"
          placeholder="tu.email@ejemplo.com"
          style={{
            width: '100%',
            padding: '12px 16px',
            fontSize: '15px',
            border: '2px solid var(--border)',
            borderRadius: 'var(--input-radius)',
            backgroundColor: 'var(--bg)',
            transition: 'all 0.2s'
          }}
        />
        {errors.email && (
          <p style={{
            color: 'var(--danger)',
            fontSize: '13px',
            marginTop: '6px',
            fontWeight: '500'
          }}>
            {errors.email.message}
          </p>
        )}
      </div>

      <div style={{ marginBottom: '32px' }}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '8px'
        }}>
          <label htmlFor="password" style={{
            fontSize: '14px',
            fontWeight: '700',
            color: 'var(--text)'
          }}>
            Contraseña
          </label>
          <a href="#" style={{
            fontSize: '13px',
            color: 'var(--primary)',
            fontWeight: '600',
            textDecoration: 'none'
          }}>
            ¿Olvidaste?
          </a>
        </div>
        <input
          id="password"
          type="password"
          {...register('password')}
          className="input-field"
          placeholder="••••••••"
          style={{
            width: '100%',
            padding: '12px 16px',
            fontSize: '15px',
            border: '2px solid var(--border)',
            borderRadius: 'var(--input-radius)',
            backgroundColor: 'var(--bg)',
            transition: 'all 0.2s'
          }}
        />
        {errors.password && (
          <p style={{
            color: 'var(--danger)',
            fontSize: '13px',
            marginTop: '6px',
            fontWeight: '500'
          }}>
            {errors.password.message}
          </p>
        )}
      </div>

      <button
        type="submit"
        disabled={isLoading}
        style={{
          width: '100%',
          padding: '14px 24px',
          fontSize: '16px',
          fontWeight: '700',
          backgroundColor: isLoading ? 'var(--muted)' : 'var(--primary)',
          color: 'white',
          border: 'none',
          borderRadius: 'var(--button-radius)',
          cursor: isLoading ? 'not-allowed' : 'pointer',
          transition: 'all 0.2s',
          boxShadow: '0 4px 12px rgba(0, 166, 81, 0.2)'
        }}
        onMouseEnter={(e) => {
          if (!isLoading) {
            e.currentTarget.style.backgroundColor = 'var(--primary-dark)'
            e.currentTarget.style.transform = 'translateY(-2px)'
          }
        }}
        onMouseLeave={(e) => {
          if (!isLoading) {
            e.currentTarget.style.backgroundColor = 'var(--primary)'
            e.currentTarget.style.transform = 'translateY(0)'
          }
        }}
      >
        {isLoading ? '⏳ Iniciando sesión...' : '→ Iniciar sesión'}
      </button>

      <div style={{
        marginTop: '24px',
        paddingTop: '24px',
        borderTop: '1px solid var(--border)',
        textAlign: 'center'
      }}>
        <p style={{
          color: 'var(--text-light)',
          fontSize: '14px'
        }}>
          ¿Nuevo en EAMS?{' '}
          <a href="#" style={{
            color: 'var(--primary)',
            fontWeight: '700',
            textDecoration: 'none'
          }}>
            Registrarse
          </a>
        </p>
      </div>
    </form>
  )
}
