import Link from 'next/link'
import React from 'react'

export default function Navbar() {
  return (
    <nav className="w-full bg-transparent py-4">
      <div className="container mx-auto px-6 lg:px-12 flex items-center justify-between">
        <Link href="/" className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-accent flex items-center justify-center text-white font-bold">E</div>
          <span className="font-semibold text-lg" style={{ color: 'var(--text)' }}>EAMS</span>
        </Link>

        <div className="hidden md:flex items-center gap-4">
          <Link href="/admin/activities" className="text-sm text-muted hover:text-black">Actividades</Link>
          <Link href="/enrollments" className="text-sm text-muted hover:text-black">Inscripciones</Link>
          <Link href="/attendance" className="text-sm text-muted hover:text-black">Asistencia</Link>
          <button className="btn-primary">Iniciar</button>
        </div>
      </div>
    </nav>
  )
}
