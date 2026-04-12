'use client'

import { useState } from 'react'
import { Enrollment } from '@/types'
import apiClient from '@/services/apiClient'

export interface EnrollmentError {
  code: string
  message: string
}

export interface UseEnrollmentResult {
  enrolling: boolean
  error: EnrollmentError | null
  enroll: (studentId: string, activityId: string) => Promise<Enrollment>
  cancel: (enrollmentId: string) => Promise<void>
  getByStudent: (studentId: string) => Promise<Enrollment[]>
}

/**
 * Hook to manage enrollments
 */
export function useEnrollment(): UseEnrollmentResult {
  const [enrolling, setEnrolling] = useState(false)
  const [error, setError] = useState<EnrollmentError | null>(null)

  const enroll = async (studentId: string, activityId: string): Promise<Enrollment> => {
    try {
      setEnrolling(true)
      setError(null)

      const response = await apiClient.post<Enrollment>('/api/enrollments', {
        studentId,
        activityId,
      })

      return response.data
    } catch (err: any) {
      const errorData: EnrollmentError = {
        code: err.response?.data?.code || 'ENROLLMENT_ERROR',
        message: err.response?.data?.message || 'Failed to enroll',
      }
      setError(errorData)
      throw errorData
    } finally {
      setEnrolling(false)
    }
  }

  const cancel = async (enrollmentId: string): Promise<void> => {
    try {
      setEnrolling(true)
      setError(null)

      await apiClient.delete(`/api/enrollments/${enrollmentId}`)
    } catch (err: any) {
      const errorData: EnrollmentError = {
        code: err.response?.data?.code || 'CANCEL_ERROR',
        message: err.response?.data?.message || 'Failed to cancel enrollment',
      }
      setError(errorData)
      throw errorData
    } finally {
      setEnrolling(false)
    }
  }

  const getByStudent = async (studentId: string): Promise<Enrollment[]> => {
    try {
      setError(null)

      const response = await apiClient.get<Enrollment[]>(
        `/api/enrollments/student/${studentId}`,
      )

      return response.data
    } catch (err: any) {
      const errorData: EnrollmentError = {
        code: err.response?.data?.code || 'FETCH_ERROR',
        message: err.response?.data?.message || 'Failed to fetch enrollments',
      }
      setError(errorData)
      throw errorData
    }
  }

  return { enrolling, error, enroll, cancel, getByStudent }
}
