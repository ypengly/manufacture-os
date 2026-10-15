import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { dashboardApi, type DashboardSummary } from '../api/dashboard'
import { useAuth } from '../auth/AuthContext'
import { ErrorBanner } from '../components/ErrorBanner'
import { PageHeader } from '../components/PageHeader'
import { StatCard } from '../components/StatCard'
import { useAsync } from '../hooks/useAsync'

const WARN_KEYS = ['ordersPendingApproval', 'lowStockMaterials', 'pendingInspections', 'openDefects', 'ordersOnHold', 'ordersPaused']
const BAD_KEYS = ['machinesBroken']

function humanize(key: string) {
  return key.replace(/([A-Z])/g, ' $1').replace(/^./, (c) => c.toUpperCase())
}

export function DashboardPage() {
  const { hasRole, user } = useAuth()

  const loader = (): Promise<DashboardSummary> => {
    if (hasRole('ADMIN')) return dashboardApi.admin()
    if (hasRole('PRODUCTION_MANAGER')) return dashboardApi.productionManager()
    if (hasRole('SUPERVISOR')) return dashboardApi.supervisor()
    if (hasRole('QUALITY_INSPECTOR')) return dashboardApi.qualityInspector()
    if (hasRole('INVENTORY_MANAGER')) return dashboardApi.inventoryManager()
    return dashboardApi.worker()
  }

  const { data, loading, error } = useAsync(loader, [user?.id])

  const numeric = Object.entries(data ?? {}).filter(([, v]) => typeof v === 'number') as [string, number][]
  const note = data?.note as string | undefined

  return (
    <>
      <PageHeader title="Dashboard" subtitle={`Live figures for your role, ${user?.fullName ?? ''}`} />
      <ErrorBanner error={error} />
      {loading && <p className="text-ink-300">Loading…</p>}
      {note && <p className="text-sm text-ink-300 mb-4">{note}</p>}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        {numeric.map(([key, value]) => (
          <StatCard
            key={key}
            label={humanize(key)}
            value={value}
            tone={BAD_KEYS.includes(key) && value > 0 ? 'bad' : WARN_KEYS.includes(key) && value > 0 ? 'warn' : 'default'}
          />
        ))}
      </div>
      {numeric.length > 2 && (
        <div className="panel p-4 mt-5">
          <h2 className="text-sm font-medium mb-3">Overview</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={numeric.map(([k, v]) => ({ name: humanize(k), value: v }))}>
                <CartesianGrid stroke="#2a3645" vertical={false} />
                <XAxis dataKey="name" tick={{ fill: '#8595a8', fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={70} />
                <YAxis tick={{ fill: '#8595a8', fontSize: 11 }} allowDecimals={false} />
                <Tooltip contentStyle={{ background: '#161e27', border: '1px solid #2a3645', borderRadius: 6 }} />
                <Bar dataKey="value" fill="#13977a" radius={[3, 3, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}
    </>
  )
}
