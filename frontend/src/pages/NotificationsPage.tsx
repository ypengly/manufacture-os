import { notificationsApi } from '../api/notifications'
import { ErrorBanner } from '../components/ErrorBanner'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import { useAsync } from '../hooks/useAsync'

export function NotificationsPage() {
  const { data, loading, error, reload } = useAsync(() => notificationsApi.mine(0, 50))

  return (
    <>
      <PageHeader title="Notifications" subtitle="Alerts addressed to you" />
      <ErrorBanner error={error} />
      {loading && <p className="text-ink-300">Loading…</p>}
      <ul className="space-y-2">
        {data?.content.length === 0 && <li className="text-ink-400">No notifications.</li>}
        {data?.content.map((n) => (
          <li key={n.id} className={`panel p-3 flex items-start justify-between gap-3 ${n.readStatus ? 'opacity-60' : ''}`}>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-medium text-sm">{n.title}</span>
                <StatusBadge value={n.type} />
              </div>
              <p className="text-sm text-ink-300 mt-1">{n.message}</p>
              <p className="text-[11px] text-ink-400 mt-1">{new Date(n.createdAt).toLocaleString()}</p>
            </div>
            {!n.readStatus && (
              <button className="btn-secondary shrink-0" onClick={() => notificationsApi.markRead(n.id).then(reload)}>
                Mark read
              </button>
            )}
          </li>
        ))}
      </ul>
    </>
  )
}
