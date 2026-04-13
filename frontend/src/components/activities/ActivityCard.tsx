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
    <div className="bg-white rounded-lg shadow p-6 border border-gray-200 hover:shadow-lg transition">
      <div className="flex justify-between items-start mb-3">
        <h3 className="text-lg font-semibold text-gray-900">{activity.name}</h3>
        <span
          className={`text-sm font-medium px-3 py-1 rounded-full ${
            hasAvailableSpots
              ? 'bg-green-100 text-green-800'
              : 'bg-red-100 text-red-800'
          }`}
        >
          {activity.availableSpots}/{activity.totalSpots} cupos
        </span>
      </div>

      {activity.description && <p className="text-gray-600 text-sm mb-4">{activity.description}</p>}

      {activity.schedule && (
        <div className="flex items-center gap-2 text-sm text-gray-700 mb-4">
          <span className="font-medium">
            {activity.schedule.dayOfWeek} — {activity.schedule.startTime} a {activity.schedule.endTime}
          </span>
        </div>
      )}

      {offlineMode && (
        <div className="mb-4 p-2 bg-yellow-50 border border-yellow-200 rounded text-yellow-800 text-sm">
          📴 Inscripción no disponible en modo offline
        </div>
      )}

      <button
        onClick={() => onEnroll?.(activity.id)}
        disabled={!canEnroll || loading}
        className={`w-full py-2 px-4 rounded-md font-medium transition ${
          canEnroll
            ? 'bg-blue-600 text-white hover:bg-blue-700'
            : 'bg-gray-300 text-gray-500 cursor-not-allowed'
        } ${loading ? 'opacity-75' : ''}`}
      >
        {loading ? 'Inscribiendo...' : 'Inscribirse'}
      </button>
    </div>
  )
}
