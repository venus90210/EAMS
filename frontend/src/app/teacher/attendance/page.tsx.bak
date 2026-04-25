'use client'

import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'

export const dynamic = 'force-dynamic'

export default function AttendancePage() {
  const router = useRouter()
  const { user, isAuthenticated, logout } = useAuth()

  if (!isAuthenticated || user?.role !== 'TEACHER') {
    router.push('/login')
    return null
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">Registro de asistencia</h1>
          <button
            onClick={() => logout()}
            className="text-gray-600 hover:text-gray-900 font-medium"
          >
            Cerrar sesión
          </button>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow p-6">
          <p className="text-gray-600">
            Aquí podrás registrar la asistencia de los estudiantes en tus actividades.
          </p>
        </div>
      </main>
    </div>
  )
}
