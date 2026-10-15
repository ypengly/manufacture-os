import { useEffect, useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { notificationsApi } from '../api/notifications'
import type { Role } from '../types/domain'

interface NavItem {
  to: string
  label: string
  roles?: Role[] // omitted = visible to everyone signed in
}

const NAV: NavItem[] = [
  { to: '/', label: 'Dashboard' },
  { to: '/production-orders', label: 'Production Orders', roles: ['ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR', 'WORKER'] },
  { to: '/boms', label: 'Bills of Material', roles: ['ADMIN', 'PRODUCTION_MANAGER'] },
  { to: '/inventory', label: 'Inventory', roles: ['ADMIN', 'INVENTORY_MANAGER', 'PRODUCTION_MANAGER', 'SUPERVISOR'] },
  { to: '/purchase-orders', label: 'Purchasing', roles: ['ADMIN', 'INVENTORY_MANAGER', 'PRODUCTION_MANAGER'] },
  { to: '/machines', label: 'Machines', roles: ['ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR', 'WORKER'] },
  { to: '/workers', label: 'Workers', roles: ['ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR'] },
  { to: '/quality', label: 'Quality', roles: ['ADMIN', 'QUALITY_INSPECTOR', 'PRODUCTION_MANAGER', 'SUPERVISOR'] },
  { to: '/reports', label: 'Reports', roles: ['ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR', 'QUALITY_INSPECTOR', 'INVENTORY_MANAGER'] },
  { to: '/notifications', label: 'Notifications' },
]

export function AppLayout() {
  const { user, logout, hasRole } = useAuth()
  const navigate = useNavigate()
  const [unread, setUnread] = useState(0)
  const [menuOpen, setMenuOpen] = useState(false)

  useEffect(() => {
    notificationsApi
      .mine(0, 50)
      .then((page) => setUnread(page.content.filter((n) => !n.readStatus).length))
      .catch(() => setUnread(0))
  }, [])

  const items = NAV.filter((i) => !i.roles || hasRole(...i.roles))

  async function onLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex">
      <aside
        className={`${menuOpen ? 'block' : 'hidden'} md:block fixed md:static inset-y-0 left-0 z-40 w-60 shrink-0 bg-ink-900 border-r border-ink-700 flex-col`}
      >
        <div className="px-4 py-4 border-b border-ink-700">
          <div className="text-sm font-semibold tracking-wide">
            Manufacture<span className="text-brand-400">OS</span>
          </div>
          <div className="text-[11px] text-ink-400">Execution &amp; management</div>
        </div>
        <nav className="p-2 space-y-0.5" aria-label="Primary">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              onClick={() => setMenuOpen(false)}
              className={({ isActive }) =>
                `flex items-center justify-between rounded-md px-3 py-2 text-sm transition-colors ${
                  isActive ? 'bg-brand-600/20 text-brand-300' : 'text-ink-200 hover:bg-ink-800'
                }`
              }
            >
              {item.label}
              {item.to === '/notifications' && unread > 0 && (
                <span className="rounded-full bg-amber-500 px-1.5 text-[10px] font-semibold text-ink-950">{unread}</span>
              )}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="flex-1 min-w-0 flex flex-col">
        <header className="flex items-center justify-between border-b border-ink-700 bg-ink-900/70 px-4 py-2.5">
          <button className="btn-ghost md:hidden" onClick={() => setMenuOpen((v) => !v)} aria-label="Toggle navigation">
            ☰
          </button>
          <div className="hidden md:block" />
          <div className="flex items-center gap-3">
            <div className="text-right leading-tight">
              <div className="text-sm">{user?.fullName}</div>
              <div className="text-[11px] text-ink-400">{user?.roles.map((r) => r.replace(/_/g, ' ')).join(', ')}</div>
            </div>
            <button className="btn-secondary" onClick={onLogout}>
              Sign out
            </button>
          </div>
        </header>
        <main className="flex-1 p-4 md:p-6 max-w-[1400px] w-full mx-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
