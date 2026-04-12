/**
 * Role enum - defines user roles in the system
 */
export enum Role {
  GUARDIAN = 'GUARDIAN',
  TEACHER = 'TEACHER',
  ADMIN = 'ADMIN',
  SUPERADMIN = 'SUPERADMIN',
}

/**
 * Token pair returned after successful authentication
 */
export interface TokenPair {
  accessToken: string
  refreshToken: string
}

/**
 * Auth response when MFA is required
 */
export interface LoginResponse {
  mfaRequired?: boolean
  sessionToken?: string
  tokens?: TokenPair
}

/**
 * User model
 */
export interface User {
  id: string
  email: string
  role: Role
  institutionId?: string
  name?: string
  createdAt: Date
  updatedAt: Date
}

/**
 * Guardian user (extends User)
 */
export interface Guardian extends User {
  students: Student[]
}

/**
 * Student model
 */
export interface Student {
  id: string
  name: string
  email: string
  institutionId: string
  createdAt: Date
  updatedAt: Date
}

/**
 * Schedule information for an activity
 */
export interface Schedule {
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'
  startTime: string // HH:mm:ss
  endTime: string // HH:mm:ss
}

/**
 * Activity status
 */
export enum ActivityStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  DISABLED = 'DISABLED',
}

/**
 * Activity model
 */
export interface Activity {
  id: string
  name: string
  description?: string
  totalSpots: number
  availableSpots: number
  status: ActivityStatus
  schedule: Schedule
  institutionId: string
  createdAt: Date
  updatedAt: Date
}

/**
 * Enrollment status
 */
export enum EnrollmentStatus {
  ACTIVE = 'ACTIVE',
  CANCELLED = 'CANCELLED',
}

/**
 * Enrollment model
 */
export interface Enrollment {
  id: string
  studentId: string
  student?: Student
  activityId: string
  activity?: Activity
  status: EnrollmentStatus
  enrolledAt: Date
  cancelledAt?: Date
}

/**
 * Attendance session
 */
export interface AttendanceSession {
  id: string
  activityId: string
  date: string // YYYY-MM-DD
  topicsCovered?: string
  recordedAt: Date
}

/**
 * Attendance record for a student in a session
 */
export interface AttendanceRecord {
  id: string
  sessionId: string
  studentId: string
  student?: Student
  present: boolean
  observation?: string
  recordedAt: Date
}

/**
 * API error response
 */
export interface ApiError {
  statusCode: number
  code: string
  message: string
  details?: Record<string, any>
}

/**
 * Pagination info
 */
export interface PaginatedResponse<T> {
  data: T[]
  total: number
  page: number
  limit: number
  totalPages: number
}

/**
 * JWT payload structure (decoded from token)
 */
export interface JwtPayload {
  sub: string // user ID
  role: Role
  institutionId?: string
  mfaPending?: boolean
  iat: number
  exp: number
}
