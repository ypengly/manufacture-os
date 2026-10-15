import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'
import type { Role } from '../types/domain'

/** Gate: must be signed in, and (optionally) hold one of the given roles. Server still enforces everything. */
export function RequireAuth({ roles }: { roles?: Role[] }) {
  const { user, loading, hasRole } = useAuth()
  if (loading) return <div className="p-8 text-ink-300">Loading…</div>
  if (!user) return <Navigate to="/login" replace />
  if (roles && !hasRole(...roles)) {
    return (
      <div className="panel p-8 text-center">
        <h1 className="text-lg font-semibold">Not authorised</h1>
        <p className="text-sm text-ink-300 mt-1">Your role does not have access to this area.</p>
      </div>
    )
  }
  return <Outlet />
}
