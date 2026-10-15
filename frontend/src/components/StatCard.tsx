export function StatCard({ label, value, tone = 'default' }: { label: string; value: number | string; tone?: 'default' | 'warn' | 'bad' }) {
  const color = tone === 'bad' ? 'text-rust-500' : tone === 'warn' ? 'text-amber-500' : 'text-ink-50'
  return (
    <div className="panel p-4">
      <div className="text-xs uppercase tracking-wide text-ink-300">{label}</div>
      <div className={`mt-1 text-3xl font-semibold tabular-nums ${color}`}>{value}</div>
    </div>
  )
}
