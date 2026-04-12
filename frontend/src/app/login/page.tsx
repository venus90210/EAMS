'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { LoginForm } from '@/components/auth/LoginForm'

export default function LoginPage() {
  const router = useRouter()
  const [mfaSessionToken, setMfaSessionToken] = useState<string | null>(null)
  const [mfaCode, setMfaCode] = useState('')
  const [mfaLoading, setMfaLoading] = useState(false)
  const [mfaError, setMfaError] = useState<string | null>(null)

  const handleMfaRequired = (sessionToken: string) => {
    setMfaSessionToken(sessionToken)
  }

  const handleLoginSuccess = () => {
    router.push('/')
  }

  const handleMfaSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!mfaSessionToken) return

    try {
      setMfaLoading(true)
      setMfaError(null)

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/mfa/verify`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sessionToken: mfaSessionToken,
          code: mfaCode,
        }),
      })

      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'MFA verification failed')
      }

      const { accessToken, refreshToken } = await response.json()

      // Store tokens (normally done by authService)
      localStorage.setItem('eams_refresh_token', refreshToken)

      // Redirect to home
      router.push('/')
    } catch (err: any) {
      setMfaError(err.message || 'MFA verification failed')
    } finally {
      setMfaLoading(false)
    }
  }

  if (mfaSessionToken) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-100">
        <form onSubmit={handleMfaSubmit} className="space-y-4 max-w-md w-full bg-white p-8 rounded shadow">
          <h1 className="text-2xl font-bold">Verificación de dos factores</h1>

          {mfaError && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
              {mfaError}
            </div>
          )}

          <div>
            <label htmlFor="mfaCode" className="block text-sm font-medium text-gray-700">
              Código TOTP
            </label>
            <input
              id="mfaCode"
              type="text"
              value={mfaCode}
              onChange={(e) => setMfaCode(e.target.value)}
              placeholder="000000"
              maxLength={6}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <button
            type="submit"
            disabled={mfaLoading || mfaCode.length !== 6}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-md font-medium hover:bg-blue-700 disabled:bg-gray-400"
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
            className="w-full text-gray-600 py-2 px-4 rounded-md font-medium hover:bg-gray-100"
          >
            Volver al login
          </button>
        </form>
      </div>
    )
  }

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100">
      <div className="bg-white p-8 rounded shadow">
        <LoginForm onMfaRequired={handleMfaRequired} onSuccess={handleLoginSuccess} />
      </div>
    </div>
  )
}
