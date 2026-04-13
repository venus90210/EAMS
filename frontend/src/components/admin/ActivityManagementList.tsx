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

const statusColors = {
  DRAFT: 'bg-gray-100 text-gray-800',
  PUBLISHED: 'bg-green-100 text-green-800',
  DISABLED: 'bg-red-100 text-red-800',
}

const statusLabels = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Publicado',
  DISABLED: 'Deshabilitado',
}

export function ActivityManagementList({
  activities,
  loading = false,
  onEdit,
  onPublish,
  onDisable,
  onDelete,
}: ActivityManagementListProps) {
  return (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Nombre
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Descripción
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Cupos
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Estado
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              Acciones
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {activities.length === 0 ? (
            <tr>
              <td colSpan={5} className="px-6 py-8 text-center text-gray-500">
                No hay actividades
              </td>
            </tr>
          ) : (
            activities.map(activity => (
              <tr key={activity.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {activity.name}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600 truncate max-w-xs">
                  {activity.description}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                  {activity.availableSpots}/{activity.totalSpots}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ${statusColors[activity.status] || statusColors.DRAFT}`}>
                    {statusLabels[activity.status] || activity.status}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                  <button
                    onClick={() => onEdit(activity)}
                    disabled={loading}
                    className="text-blue-600 hover:text-blue-800 disabled:text-gray-400"
                  >
                    Editar
                  </button>

                  {activity.status === 'DRAFT' && (
                    <button
                      onClick={() => onPublish(activity.id)}
                      disabled={loading}
                      className="text-green-600 hover:text-green-800 disabled:text-gray-400"
                    >
                      Publicar
                    </button>
                  )}

                  {activity.status === 'PUBLISHED' && (
                    <button
                      onClick={() => onDisable(activity.id)}
                      disabled={loading}
                      className="text-red-600 hover:text-red-800 disabled:text-gray-400"
                    >
                      Deshabilitar
                    </button>
                  )}

                  <button
                    onClick={() => {
                      if (confirm('¿Estás seguro de que deseas eliminar esta actividad?')) {
                        onDelete(activity.id)
                      }
                    }}
                    disabled={loading}
                    className="text-red-600 hover:text-red-800 disabled:text-gray-400"
                  >
                    Eliminar
                  </button>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  )
}
