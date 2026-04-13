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

  // Handle redirect after login succeeds and user state is updated
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
      // Set flag to redirect after state updates
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
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: 'var(--bg)' }}>
        <form onSubmit={handleMfaSubmit} className="w-full max-w-md">
          <div className="card p-8 space-y-6">
            <div>
              <h1 className="text-3xl font-bold" style={{ color: 'var(--text)' }}>Verificación de dos factores</h1>
              <p className="text-sm" style={{ color: 'var(--muted)' }} >Ingresa el código de tu app autenticadora</p>
            </div>

            {mfaError && (
              <div className="p-4 rounded" style={{ backgroundColor: '#FEE2E2', color: '#B91C1C', borderLeft: '4px solid #DC2626' }}>
                {mfaError}
              </div>
            )}

            <div>
              <label htmlFor="mfaCode" className="block text-sm font-semibold" style={{ color: 'var(--text)', marginBottom: '8px' }}>
                Código TOTP
              </label>
              <input
                id="mfaCode"
                type="text"
                value={mfaCode}
                onChange={(e) => setMfaCode(e.target.value)}
                placeholder="000000"
                maxLength={6}
                className="input-field text-center text-2xl tracking-widest"
              />
            </div>

            <button
              type="submit"
              disabled={mfaLoading || mfaCode.length !== 6}
              className="btn-primary w-full"
            >
              {mfaLoading ? 'Verificando...' : 'Verificar'}
            </button>

            <button
              type="button"
              onClick={() => {
                setMfaSessionToken(null)
                setMfaCode('')
                setMfaError(null)
              }}
              className="btn-secondary w-full"
            >
              Volver al login
            </button>
          </div>
        </form>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: 'var(--bg)' }}>
      <div className="w-full max-w-md">
        <LoginForm onMfaRequired={handleMfaRequired} onSuccess={handleLoginSuccess} />
      </div>
    </div>
  )
}
