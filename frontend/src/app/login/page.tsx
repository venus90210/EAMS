'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { LoginForm } from '@/components/auth/LoginForm'

export default function LoginPage() {
  const router = useRouter()
  const { mfaVerify, isAuthenticated, user } = useAuth()
  const [shouldRedirect, setShouldRedirect] = useState(false)
  const [mfaSessionToken, setMfaSessionToken] = useState<string | null>(null)
  const [mfaCode, setMfaCode] = useState('')
  const [mfaLoading, setMfaLoading] = useState(false)
  const [mfaError, setMfaError] = useState<string | null>(null)

  useEffect(() => {
    if (shouldRedirect && isAuthenticated && user) {
      console.log('[LoginPage] User authenticated, redirecting to home')
      setShouldRedirect(false)
      router.push('/')
    }
  }, [shouldRedirect, isAuthenticated, user, router])

  const handleMfaRequired = (sessionToken: string) => {
    setMfaSessionToken(sessionToken)
  }

  const handleLoginSuccess = () => {
    console.log('[LoginPage] Login successful, preparing to redirect')
    setShouldRedirect(true)
  }

  const handleMfaSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!mfaSessionToken) return

    try {
      setMfaLoading(true)
      setMfaError(null)

      console.log('[LoginPage] Verifying MFA code')
      await mfaVerify(mfaSessionToken, mfaCode)

      console.log('[LoginPage] MFA verified, preparing to redirect')
      setShouldRedirect(true)
    } catch (err: any) {
      console.log('[LoginPage] MFA error:', err)
      setMfaError(err.message || 'MFA verification failed')
    } finally {
      setMfaLoading(false)
    }
  }

  if (mfaSessionToken) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center" style={{ backgroundColor: 'var(--bg)' }}>
        <div className="w-full max-w-2xl px-4">
          {/* Logo/Title */}
          <div className="text-center mb-12">
            <h1 style={{ fontSize: '42px', fontWeight: '800', color: 'var(--primary)', marginBottom: '8px' }}>
              🔐 EAMS
            </h1>
            <p style={{ color: 'var(--muted)', fontSize: '16px' }}>Sistema de Gestión de Actividades</p>
          </div>

          <form onSubmit={handleMfaSubmit} className="w-full max-w-md mx-auto">
            <div style={{
              backgroundColor: 'var(--surface)',
              borderRadius: 'var(--card-radius)',
              boxShadow: 'var(--elevation-3)',
              padding: '48px 40px',
              border: '1px solid var(--border)'
            }} className="space-y-8">
              <div>
                <h2 style={{ fontSize: '28px', fontWeight: '700', color: 'var(--text)', marginBottom: '12px' }}>
                  Verificación en dos factores
                </h2>
                <p style={{ color: 'var(--muted)', fontSize: '16px' }}>
                  Ingresa el código de 6 dígitos de tu app autenticadora
                </p>
              </div>

              {mfaError && (
                <div style={{
                  padding: '16px',
                  borderRadius: 'var(--button-radius)',
                  backgroundColor: '#ffebee',
                  borderLeft: '4px solid var(--danger)',
                  color: '#c62828',
                  fontWeight: '600',
                  fontSize: '14px'
                }}>
                  ⚠️ {mfaError}
                </div>
              )}

              <div>
                <label htmlFor="mfaCode" style={{
                  display: 'block',
                  fontSize: '14px',
                  fontWeight: '700',
                  color: 'var(--text)',
                  marginBottom: '12px'
                }}>
                  Código TOTP
                </label>
                <input
                  id="mfaCode"
                  type="text"
                  value={mfaCode}
                  onChange={(e) => setMfaCode(e.target.value)}
                  placeholder="000000"
                  maxLength={6}
                  style={{
                    width: '100%',
                    padding: '16px',
                    fontSize: '28px',
                    textAlign: 'center',
                    letterSpacing: '12px',
                    border: '2px solid var(--border)',
                    borderRadius: 'var(--input-radius)',
                    fontFamily: 'monospace',
                    fontWeight: '700',
                    transition: 'all 0.2s'
                  }}
                  className="input-field"
                />
              </div>

              <button
                type="submit"
                disabled={mfaLoading || mfaCode.length !== 6}
                style={{
                  width: '100%',
                  padding: '16px',
                  fontSize: '16px',
                  fontWeight: '700',
                  backgroundColor: mfaLoading || mfaCode.length !== 6 ? 'var(--muted)' : 'var(--primary)',
                  color: 'white',
                  border: 'none',
                  borderRadius: 'var(--button-radius)',
                  cursor: mfaLoading || mfaCode.length !== 6 ? 'not-allowed' : 'pointer',
                  transition: 'all 0.2s'
                }}
              >
                {mfaLoading ? '⏳ Verificando...' : '→ Verificar'}
              </button>

              <button
                type="button"
                onClick={() => {
                  setMfaSessionToken(null)
                  setMfaCode('')
                  setMfaError(null)
                }}
                style={{
                  width: '100%',
                  padding: '16px',
                  fontSize: '16px',
                  fontWeight: '700',
                  backgroundColor: 'transparent',
                  color: 'var(--primary)',
                  border: '2px solid var(--primary)',
                  borderRadius: 'var(--button-radius)',
                  cursor: 'pointer',
                  transition: 'all 0.2s'
                }}
              >
                ← Volver al login
              </button>
            </div>
          </form>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--bg)' }}>
      {/* Header with Logo */}
      <div style={{
        background: 'linear-gradient(135deg, var(--primary-dark) 0%, var(--primary) 100%)',
        padding: '32px 20px'
      }}>
        <div className="max-w-6xl mx-auto">
          <h1 style={{ fontSize: '32px', fontWeight: '800', color: 'white' }}>
            🎓 EAMS
          </h1>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex items-center justify-center" style={{ minHeight: 'calc(100vh - 100px)', padding: '40px 20px' }}>
        <div className="w-full max-w-lg">
          {/* Info Section */}
          <div className="text-center mb-12">
            <h2 style={{ fontSize: '36px', fontWeight: '800', color: 'var(--text)', marginBottom: '12px' }}>
              Bienvenido
            </h2>
            <p style={{ fontSize: '18px', color: 'var(--text-light)', marginBottom: '24px' }}>
              Sistema de Gestión de Actividades Extracurriculares
            </p>
          </div>

          {/* Login Form */}
          <LoginForm onMfaRequired={handleMfaRequired} onSuccess={handleLoginSuccess} />
        </div>
      </div>
    </div>
  )
}
