import { useState } from 'react'
import { downloadReport, type ReportKind } from '../../api/reports'
import { ErrorBanner } from '../../components/ErrorBanner'
import { PageHeader } from '../../components/PageHeader'

const REPORTS: { kind: ReportKind; title: string; description: string }[] = [
  { kind: 'production', title: 'Production orders', description: 'Orders with planned vs completed vs rejected quantity.' },
  { kind: 'production/output', title: 'Production output', description: 'Per-report completed and rejected counts with downtime.' },
  { kind: 'machines', title: 'Machine utilisation', description: 'Scheduled vs actual machine task windows.' },
  { kind: 'quality', title: 'Quality inspections', description: 'Inspection results and pass/fail quantities.' },
  { kind: 'defects', title: 'Defects', description: 'Reported defects by severity and status.' },
  { kind: 'workers', title: 'Worker assignments', description: 'Assignment history per worker.' },
]

export function ReportsPage() {
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [factoryId, setFactoryId] = useState('')
  const [lineId, setLineId] = useState('')
  const [busy, setBusy] = useState<string | null>(null)
  const [error, setError] = useState<unknown>(null)

  async function run(kind: ReportKind) {
    setBusy(kind)
    setError(null)
    try {
      await downloadReport(kind, {
        from: from ? new Date(from).toISOString() : undefined,
        to: to ? new Date(to).toISOString() : undefined,
        factoryId: factoryId ? Number(factoryId) : undefined,
        lineId: lineId ? Number(lineId) : undefined,
      })
    } catch (e) {
      setError(e)
    } finally {
      setBusy(null)
    }
  }

  return (
    <>
      <PageHeader title="Reports" subtitle="Export CSV with optional date, factory and line filters" />
      <ErrorBanner error={error} />
      <div className="panel p-4 mb-5 grid grid-cols-2 md:grid-cols-4 gap-3">
        <label>
          <span className="field-label">From</span>
          <input type="datetime-local" className="field-input" value={from} onChange={(e) => setFrom(e.target.value)} />
        </label>
        <label>
          <span className="field-label">To</span>
          <input type="datetime-local" className="field-input" value={to} onChange={(e) => setTo(e.target.value)} />
        </label>
        <label>
          <span className="field-label">Factory ID</span>
          <input className="field-input" inputMode="numeric" value={factoryId} onChange={(e) => setFactoryId(e.target.value)} />
        </label>
        <label>
          <span className="field-label">Line ID</span>
          <input className="field-input" inputMode="numeric" value={lineId} onChange={(e) => setLineId(e.target.value)} />
        </label>
      </div>
      <div className="grid md:grid-cols-2 gap-3">
        {REPORTS.map((r) => (
          <div key={r.kind} className="panel p-4 flex items-center justify-between gap-3">
            <div>
              <div className="font-medium text-sm">{r.title}</div>
              <div className="text-xs text-ink-300 mt-0.5">{r.description}</div>
            </div>
            <button className="btn-primary shrink-0" disabled={busy === r.kind} onClick={() => run(r.kind)}>
              {busy === r.kind ? 'Preparing…' : 'Download CSV'}
            </button>
          </div>
        ))}
      </div>
    </>
  )
}
