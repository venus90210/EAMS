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

  const getActivityEmoji = (name: string) => {
    const nameLower = name.toLowerCase()
    if (nameLower.includes('fútbol') || nameLower.includes('futbol')) return '⚽'
    if (nameLower.includes('matemática')) return '🧮'
    if (nameLower.includes('arte')) return '🎨'
    if (nameLower.includes('cocina')) return '🍳'
    if (nameLower.includes('música') || nameLower.includes('musica')) return '🎵'
    if (nameLower.includes('danza')) return '💃'
    if (nameLower.includes('inglés') || nameLower.includes('idioma')) return '🌍'
    if (nameLower.includes('tecnología') || nameLower.includes('codigo')) return '💻'
    if (nameLower.includes('deportes')) return '🏃'
    return '✨'
  }

  return (
    <div
      style={{
        backgroundColor: 'var(--surface)',
        borderRadius: 'var(--card-radius)',
        border: '1px solid var(--border)',
        boxShadow: 'var(--card-shadow)',
        transition: 'all 0.3s ease',
        padding: '24px',
        margin: '8px'
      }}
      className="hover:shadow-2xl hover:scale-105"
      onMouseEnter={(e) => {
        e.currentTarget.style.borderColor = 'var(--primary)'
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.borderColor = 'var(--border)'
      }}
    >
      {/* Header with Icon and Title */}
      <div className="flex items-start gap-4 mb-4">
        <div style={{ fontSize: '40px' }}>
          {getActivityEmoji(activity.name)}
        </div>
        <div className="flex-1">
          <h3 style={{ fontSize: '20px', fontWeight: '700', marginBottom: '4px', color: 'var(--text)' }}>
            {activity.name}
          </h3>
          <span
            style={{
              display: 'inline-block',
              fontSize: '12px',
              fontWeight: '700',
              padding: '6px 12px',
              borderRadius: '20px',
              backgroundColor: hasAvailableSpots ? 'rgba(76, 175, 80, 0.1)' : 'rgba(244, 67, 54, 0.1)',
              color: hasAvailableSpots ? 'var(--success)' : 'var(--danger)'
            }}
          >
            {hasAvailableSpots ? `✓ ${activity.availableSpots} cupos` : '✕ Sin cupos'}
          </span>
        </div>
      </div>

      {activity.description && (
        <p style={{ color: 'var(--text-light)', fontSize: '14px', marginBottom: '12px', lineHeight: '1.5' }}>
          {activity.description}
        </p>
      )}

      {activity.schedule && (
        <div
          className="flex items-center gap-2 text-sm font-semibold mb-6 p-3 rounded"
          style={{ backgroundColor: 'var(--bg)', color: 'var(--text)' }}
        >
          <span>📅</span>
          <span>
            {activity.schedule.dayOfWeek} • {activity.schedule.startTime} - {activity.schedule.endTime}
          </span>
        </div>
      )}

      {offlineMode && (
        <div className="mb-4 p-3 rounded text-sm font-medium" style={{ backgroundColor: '#fff3cd', color: '#856404', border: '1px solid #ffeaa7' }}>
          📴 No disponible sin conexión
        </div>
      )}

      <button
        onClick={() => onEnroll?.(activity.id)}
        disabled={!canEnroll || loading}
        style={{
          width: '100%',
          padding: '14px 16px',
          borderRadius: 'var(--button-radius)',
          fontWeight: '700',
          fontSize: '16px',
          border: 'none',
          cursor: canEnroll ? 'pointer' : 'not-allowed',
          backgroundColor: canEnroll ? 'var(--primary)' : 'var(--muted)',
          color: canEnroll ? 'white' : 'var(--surface)',
          transition: 'all 0.2s ease',
          boxShadow: canEnroll ? '0 4px 12px rgba(0, 166, 81, 0.2)' : 'none',
        }}
        onMouseEnter={(e) => {
          if (canEnroll) {
            e.currentTarget.style.backgroundColor = 'var(--primary-dark)'
            e.currentTarget.style.boxShadow = '0 6px 16px rgba(0, 166, 81, 0.3)'
            e.currentTarget.style.transform = 'translateY(-2px)'
          }
        }}
        onMouseLeave={(e) => {
          if (canEnroll) {
            e.currentTarget.style.backgroundColor = 'var(--primary)'
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 166, 81, 0.2)'
            e.currentTarget.style.transform = 'translateY(0)'
          }
        }}
      >
        {loading ? '⏳ Inscribiendo...' : canEnroll ? '→ Inscribirse' : '✕ Sin cupos'}
      </button>
    </div>
  )
}
