'use client'

import { useState } from 'react'
import { useAuth } from '@/hooks/useAuth'
import { authService } from '@/services/authService'

export default function TestPage() {
  const { user, isAuthenticated, loading, login, logout } = useAuth()
  const [email, setEmail] = useState('guardian@example.com')
  const [password, setPassword] = useState('password123')
  const [message, setMessage] = useState('')
  const [isLogging, setIsLogging] = useState(false)

  const handleLogin = async () => {
    try {
      setIsLogging(true)
      setMessage('[1] Iniciando login...')

      const result = await login(email, password)

      setMessage(`[2] Login completado. Result: ${result === null ? 'sin MFA' : 'con MFA'}`)

      // Check tokens
      const token = authService.getAccessToken()
      setMessage(prev => prev + `\n[3] Token en authService: ${token ? token.substring(0, 30) + '...' : 'NADA'}`)

      // Check context
      setMessage(prev => prev + `\n[4] Context - Auth: ${isAuthenticated}, User: ${user?.id || 'NADA'}, Role: ${user?.role || 'NADA'}`)
    } catch (err: any) {
      setMessage(`[ERROR] ${err.message || JSON.stringify(err)}`)
    } finally {
      setIsLogging(false)
    }
  }

  const handleLogout = async () => {
    await logout()
    setMessage('Logout completado')
  }

  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow p-8">
        <h1 className="text-3xl font-bold mb-8">🧪 Test de Login</h1>

        {/* Status Panel */}
        <div className="bg-blue-50 border border-blue-200 rounded p-4 mb-8">
          <h2 className="font-bold mb-2">Estado del Contexto:</h2>
          <pre className="text-sm bg-white p-2 rounded border overflow-auto">
{`Loading: ${loading}
IsAuthenticated: ${isAuthenticated}
User ID: ${user?.id || 'N/A'}
User Role: ${user?.role || 'N/A'}
Token en localStorage: ${authService.getRefreshToken() ? 'SÍ' : 'NO'}
Token en memoria: ${authService.getAccessToken() ? 'SÍ' : 'NO'}`}
          </pre>
        </div>

        {/* Login Form */}
        <div className="space-y-4 mb-8">
          <div>
            <label className="block text-sm font-medium mb-2">Email:</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full border rounded px-3 py-2"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-2">Password:</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full border rounded px-3 py-2"
            />
          </div>

          <div className="flex gap-4">
            <button
              onClick={handleLogin}
              disabled={isLogging}
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:bg-gray-400"
            >
              {isLogging ? 'Cargando...' : 'Login'}
            </button>

            <button
              onClick={handleLogout}
              className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
            >
              Logout
            </button>
          </div>
        </div>

        {/* Messages */}
        {message && (
          <div className="bg-yellow-50 border border-yellow-200 rounded p-4 whitespace-pre-wrap font-mono text-sm">
            {message}
          </div>
        )}

        {/* Status */}
        {isAuthenticated && (
          <div className="mt-8 bg-green-50 border border-green-200 rounded p-4">
            <p className="text-green-800">✓ Autenticado como: <strong>{user?.role}</strong></p>
            <p className="text-sm text-green-700 mt-2">
              <a href="/guardian/activities" className="text-blue-600 hover:underline">
                → Ir a guardian/activities
              </a>
            </p>
          </div>
        )}
      </div>
    </div>
  )
}
