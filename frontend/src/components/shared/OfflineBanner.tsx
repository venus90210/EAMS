'use client'

import { useOfflineStatus } from '@/hooks/useOfflineStatus'

export function OfflineBanner() {
  const { isOnline, cacheAge, cacheExpired } = useOfflineStatus()

  if (isOnline && !cacheExpired) return null

  const formatAge = (ms: number | null) => {
    if (!ms) return 'desconocida'
    const hours = Math.floor(ms / (1000 * 60 * 60))
    const minutes = Math.floor((ms % (1000 * 60 * 60)) / (1000 * 60))
    if (hours > 0) return `${hours}h ${minutes}m`
    return `${minutes}m`
  }

  return (
    <div
      className={`fixed bottom-0 left-0 right-0 p-4 text-white ${
        cacheExpired ? 'bg-red-600' : 'bg-yellow-600'
      }`}
    >
      <div className="max-w-4xl mx-auto">
        {!isOnline && (
          <p className="mb-2">
            📴 Modo offline — datos de hace {formatAge(cacheAge)}. Algunas acciones no están disponibles.
          </p>
        )}
        {cacheExpired && (
          <p>
            ⚠️ Los datos en caché tienen más de 48 horas. Por favor, conéctese a internet para actualizar.
          </p>
        )}
      </div>
    </div>
  )
}
