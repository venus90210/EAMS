'use client'

import Link from 'next/link'
import { useRouter, usePathname } from 'next/navigation'
import React from 'react'
import { useAuth } from '@/hooks/useAuth'
import { authService } from '@/services/authService'

export default function Navbar() {
  const router = useRouter()
  const pathname = usePathname()
  const { user, isAuthenticated } = useAuth()

  const handleLogout = () => {
    authService.clearTokens()
    router.push('/login')
  }

  // No mostrar navbar en rutas públicas
  const isPublicRoute = pathname === '/login' || pathname === '/'

  // Si no está autenticado o está en ruta pública, no mostrar navbar
  if (!isAuthenticated || !user || isPublicRoute) {
    return null
  }

  // Rutas según el rol
  const getNavLinks = () => {
    switch (user.role) {
      case 'GUARDIAN':
        return [
          { label: 'Actividades', href: '/guardian/activities' },
          { label: 'Seguimiento', href: '/guardian/tracking' },
        ]
      case 'TEACHER':
        return [
          { label: 'Asistencia', href: '/teacher/attendance' },
        ]
      case 'ADMIN':
        return [
          { label: 'Actividades', href: '/admin/activities' },
        ]
      default:
        return []
    }
  }

  const navLinks = getNavLinks()

  return (
    <nav className="w-full" style={{ backgroundColor: 'var(--surface)', borderBottom: `1px solid var(--border)` }}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2 flex-shrink-0">
            <div
              className="w-8 h-8 rounded-full flex items-center justify-center text-white font-bold text-sm"
              style={{ backgroundColor: 'var(--primary)' }}
            >
              E
            </div>
            <span className="font-semibold text-lg" style={{ color: 'var(--text)' }}>
              EAMS
            </span>
          </Link>

          {/* Navigation Links */}
          <div className="flex items-center gap-8">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="text-sm font-medium transition-colors"
                style={{ color: 'var(--text)' }}
                onMouseEnter={(e) => (e.currentTarget.style.color = 'var(--primary)')}
                onMouseLeave={(e) => (e.currentTarget.style.color = 'var(--text)')}
              >
                {link.label}
              </Link>
            ))}

            {/* User Info and Logout */}
            <div className="flex items-center gap-4 border-l" style={{ borderColor: 'var(--border)', paddingLeft: '32px' }}>
              <div className="text-right">
                <p className="text-sm font-medium" style={{ color: 'var(--text)' }}>
                  {user.name || user.email.split('@')[0]}
                </p>
                <p className="text-xs" style={{ color: 'var(--muted)' }}>
                  {user.role === 'GUARDIAN' && 'Acudiente'}
                  {user.role === 'TEACHER' && 'Docente'}
                  {user.role === 'ADMIN' && 'Administrador'}
                </p>
              </div>

              <button
                onClick={handleLogout}
                className="btn-secondary px-4 py-2 text-sm"
              >
                Cerrar sesión
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>
  )
}
