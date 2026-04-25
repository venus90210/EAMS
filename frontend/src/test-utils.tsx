import React, { ReactNode } from 'react'
import { AuthProvider } from '@/contexts/AuthContext'
import { renderHook, RenderHookOptions } from '@testing-library/react'

/**
 * Custom render hook that wraps component in AuthProvider for tests
 */
export function renderHookWithAuth<TProps, TResult>(
  hook: (props: TProps) => TResult,
  options?: Omit<RenderHookOptions<TProps>, 'wrapper'>
) {
  const wrapper = ({ children }: { children: ReactNode }) => (
    <AuthProvider>{children}</AuthProvider>
  )

  return renderHook(hook, { ...options, wrapper })
}
