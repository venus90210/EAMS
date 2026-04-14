'use client'

import { Activity } from '@/types'

interface ActivityManagementListProps {
  activities: Activity[]
  loading?: boolean
  onEdit: (activity: Activity) => void
  onPublish: (activityId: string) => Promise<void>
  onDisable: (activityId: string) => Promise<void>
  onDelete: (activityId: string) => Promise<void>
}

const statusConfig = {
  DRAFT: { icon: '📝', label: 'Borrador', bgColor: 'rgba(255, 193, 7, 0.1)', textColor: '#f57f17' },
  PUBLISHED: { icon: '✓', label: 'Publicado', bgColor: 'rgba(76, 175, 80, 0.15)', textColor: 'var(--success)' },
  DISABLED: { icon: '✕', label: 'Deshabilitado', bgColor: 'rgba(244, 67, 54, 0.15)', textColor: 'var(--danger)' },
}

export function ActivityManagementList({
  activities,
  loading = false,
  onEdit,
  onPublish,
  onDisable,
  onDelete,
}: ActivityManagementListProps) {
  if (activities.length === 0) {
    return (
      <div style={{
        backgroundColor: 'var(--surface)',
        borderRadius: 'var(--card-radius)',
        border: '1px solid var(--border)',
        padding: '60px 40px',
        textAlign: 'center'
      }}>
        <p style={{ color: 'var(--muted)', fontSize: '16px', fontWeight: '600', margin: 0 }}>
          No hay actividades aún
        </p>
      </div>
    )
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {activities.map(activity => {
        const status = statusConfig[activity.status] || statusConfig.DRAFT

        return (
          <div
            key={activity.id}
            style={{
              backgroundColor: 'var(--surface)',
              borderRadius: 'var(--card-radius)',
              border: '1px solid var(--border)',
              overflow: 'hidden',
              transition: 'all 0.3s ease',
              boxShadow: 'var(--card-shadow)'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.boxShadow = 'var(--card-shadow-hover)'
              e.currentTarget.style.transform = 'translateY(-2px)'
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.boxShadow = 'var(--card-shadow)'
              e.currentTarget.style.transform = 'translateY(0)'
            }}
          >
            {/* Header */}
            <div style={{
              padding: '24px',
              backgroundColor: 'var(--bg)',
              borderBottom: '1px solid var(--border)',
              display: 'grid',
              gridTemplateColumns: '2fr 3fr 1fr auto',
              gap: '24px',
              alignItems: 'center'
            }}>
              {/* Activity Name */}
              <div>
                <p style={{ fontSize: '12px', color: 'var(--muted)', fontWeight: '700', margin: '0 0 6px 0', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  Actividad
                </p>
                <h3 style={{ fontSize: '18px', fontWeight: '800', color: 'var(--text)', margin: 0 }}>
                  {activity.name}
                </h3>
              </div>

              {/* Description */}
              <div>
                <p style={{ fontSize: '12px', color: 'var(--muted)', fontWeight: '700', margin: '0 0 6px 0', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  Descripción
                </p>
                <p style={{ fontSize: '14px', color: 'var(--text)', margin: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {activity.description}
                </p>
              </div>

              {/* Spots */}
              <div style={{ textAlign: 'center' }}>
                <p style={{ fontSize: '12px', color: 'var(--muted)', fontWeight: '700', margin: '0 0 6px 0', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  Cupos
                </p>
                <p style={{ fontSize: '18px', fontWeight: '900', color: 'var(--primary)', margin: 0 }}>
                  {activity.availableSpots}/{activity.totalSpots}
                </p>
              </div>

              {/* Status Badge */}
              <div style={{
                padding: '8px 16px',
                borderRadius: '20px',
                backgroundColor: status.bgColor,
                color: status.textColor,
                fontSize: '12px',
                fontWeight: '700',
                whiteSpace: 'nowrap',
                textAlign: 'center'
              }}>
                {status.icon} {status.label}
              </div>
            </div>

            {/* Actions */}
            <div style={{
              padding: '16px 24px',
              backgroundColor: 'var(--surface)',
              display: 'flex',
              gap: '8px',
              flexWrap: 'wrap'
            }}>
              <button
                onClick={() => onEdit(activity)}
                disabled={loading}
                style={{
                  padding: '10px 16px',
                  fontSize: '12px',
                  fontWeight: '700',
                  backgroundColor: 'var(--primary)',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  transition: 'all 0.2s',
                  opacity: loading ? 0.6 : 1,
                  textTransform: 'uppercase',
                  letterSpacing: '0.5px'
                }}
                onMouseEnter={(e) => {
                  if (!loading) {
                    e.currentTarget.style.transform = 'translateY(-2px)'
                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 166, 81, 0.3)'
                  }
                }}
                onMouseLeave={(e) => {
                  if (!loading) {
                    e.currentTarget.style.transform = 'translateY(0)'
                    e.currentTarget.style.boxShadow = 'none'
                  }
                }}
              >
                ✏️ Editar
              </button>

              {activity.status === 'DRAFT' && (
                <button
                  onClick={() => onPublish(activity.id)}
                  disabled={loading}
                  style={{
                    padding: '10px 16px',
                    fontSize: '12px',
                    fontWeight: '700',
                    backgroundColor: 'var(--success)',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: loading ? 'not-allowed' : 'pointer',
                    transition: 'all 0.2s',
                    opacity: loading ? 0.6 : 1,
                    textTransform: 'uppercase',
                    letterSpacing: '0.5px'
                  }}
                  onMouseEnter={(e) => {
                    if (!loading) {
                      e.currentTarget.style.transform = 'translateY(-2px)'
                      e.currentTarget.style.boxShadow = '0 4px 12px rgba(76, 175, 80, 0.3)'
                    }
                  }}
                  onMouseLeave={(e) => {
                    if (!loading) {
                      e.currentTarget.style.transform = 'translateY(0)'
                      e.currentTarget.style.boxShadow = 'none'
                    }
                  }}
                >
                  📤 Publicar
                </button>
              )}

              {activity.status === 'PUBLISHED' && (
                <button
                  onClick={() => onDisable(activity.id)}
                  disabled={loading}
                  style={{
                    padding: '10px 16px',
                    fontSize: '12px',
                    fontWeight: '700',
                    backgroundColor: 'var(--warning)',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: loading ? 'not-allowed' : 'pointer',
                    transition: 'all 0.2s',
                    opacity: loading ? 0.6 : 1,
                    textTransform: 'uppercase',
                    letterSpacing: '0.5px'
                  }}
                  onMouseEnter={(e) => {
                    if (!loading) {
                      e.currentTarget.style.transform = 'translateY(-2px)'
                      e.currentTarget.style.boxShadow = '0 4px 12px rgba(255, 193, 7, 0.3)'
                    }
                  }}
                  onMouseLeave={(e) => {
                    if (!loading) {
                      e.currentTarget.style.transform = 'translateY(0)'
                      e.currentTarget.style.boxShadow = 'none'
                    }
                  }}
                >
                  🔒 Deshabilitar
                </button>
              )}

              <button
                onClick={() => {
                  if (confirm('¿Estás seguro de que deseas eliminar esta actividad?')) {
                    onDelete(activity.id)
                  }
                }}
                disabled={loading}
                style={{
                  padding: '10px 16px',
                  fontSize: '12px',
                  fontWeight: '700',
                  backgroundColor: 'var(--danger)',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  transition: 'all 0.2s',
                  opacity: loading ? 0.6 : 1,
                  textTransform: 'uppercase',
                  letterSpacing: '0.5px'
                }}
                onMouseEnter={(e) => {
                  if (!loading) {
                    e.currentTarget.style.transform = 'translateY(-2px)'
                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(244, 67, 54, 0.3)'
                  }
                }}
                onMouseLeave={(e) => {
                  if (!loading) {
                    e.currentTarget.style.transform = 'translateY(0)'
                    e.currentTarget.style.boxShadow = 'none'
                  }
                }}
              >
                🗑️ Eliminar
              </button>
            </div>
          </div>
        )
      })}
    </div>
  )
}
