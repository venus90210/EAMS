'use client'

import { Activity } from '@/types'

interface ActivityCardProps {
  activity: Activity
  onEnroll?: (activityId: string) => void
  offlineMode?: boolean
  loading?: boolean
}

export function ActivityCard({
  activity,
  onEnroll,
  offlineMode = false,
  loading = false,
}: ActivityCardProps) {
  const hasAvailableSpots = activity.availableSpots > 0
  const canEnroll = hasAvailableSpots && !offlineMode

  return (
    <div
      style={{
        backgroundColor: 'var(--surface)',
        borderRadius: 'var(--radius-lg)',
        border: `1px solid var(--border)`,
      }}
      className="p-6 hover:shadow-lg transition-shadow"
    >
      <div className="flex justify-between items-start mb-4">
        <h3 className="text-lg font-bold flex-1" style={{ color: 'var(--text)' }}>
          {activity.name}
        </h3>
        <span
          className={`text-xs font-bold px-3 py-1 rounded-full whitespace-nowrap ml-2 ${
            hasAvailableSpots
              ? 'bg-green-100 text-green-700'
              : 'bg-red-100 text-red-700'
          }`}
        >
          {hasAvailableSpots ? `✓ ${activity.availableSpots}` : '✕'} cupos
        </span>
      </div>

      {activity.description && (
        <p style={{ color: 'var(--muted)' }} className="text-sm mb-4 line-clamp-2">
          {activity.description}
        </p>
      )}

      {activity.schedule && (
        <div
          className="flex items-center gap-2 text-sm font-medium mb-4 p-3 rounded"
          style={{ backgroundColor: 'var(--background)', color: 'var(--text)' }}
        >
          <span>📅</span>
          <span>
            {activity.schedule.dayOfWeek} • {activity.schedule.startTime} - {activity.schedule.endTime}
          </span>
        </div>
      )}

      {offlineMode && (
        <div className="mb-4 p-3 rounded text-sm font-medium" style={{ backgroundColor: '#fef3c7', color: '#92400e' }}>
          📴 No disponible sin conexión
        </div>
      )}

      <button
        onClick={() => onEnroll?.(activity.id)}
        disabled={!canEnroll || loading}
        className={`w-full py-2 px-4 rounded-md font-semibold transition ${
          canEnroll
            ? 'btn-primary'
            : 'opacity-60 cursor-not-allowed'
        }`}
        style={!canEnroll ? { backgroundColor: 'var(--muted)', color: 'var(--surface)' } : {}}
      >
        {loading ? '⏳ Inscribiendo...' : canEnroll ? 'Inscribirse' : 'Sin cupos'}
      </button>
    </div>
  )
}
