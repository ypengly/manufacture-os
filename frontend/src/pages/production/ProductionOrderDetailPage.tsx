import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { productionOrdersApi } from '../../api/productionOrders'
import { materialsApi, storageLocationsApi } from '../../api/masterData'
import { useAuth } from '../../auth/AuthContext'
import { DataTable } from '../../components/DataTable'
import { ErrorBanner } from '../../components/ErrorBanner'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useAction, useAsync } from '../../hooks/useAsync'
import type { OrderStatus, Role } from '../../types/domain'

interface Transition {
  label: string
  from: OrderStatus[]
  roles: Role[]
  run: (id: number) => Promise<unknown>
  variant?: 'primary' | 'secondary' | 'danger'
}

const MANAGE: Role[] = ['ADMIN', 'PRODUCTION_MANAGER']
const FLOOR: Role[] = ['ADMIN', 'SUPERVISOR']

const TRANSITIONS: Transition[] = [
  { label: 'Plan', from: ['DRAFT'], roles: MANAGE, run: productionOrdersApi.plan },
  { label: 'Submit for approval', from: ['PLANNED'], roles: MANAGE, run: productionOrdersApi.submit },
  { label: 'Approve', from: ['PENDING_APPROVAL'], roles: MANAGE, run: productionOrdersApi.approve },
  { label: 'Reserve materials', from: ['APPROVED'], roles: [...MANAGE, 'INVENTORY_MANAGER'], run: (id) => productionOrdersApi.reserveMaterials(id, false) },
  { label: 'Reserve (allow partial)', from: ['APPROVED'], roles: MANAGE, run: (id) => productionOrdersApi.reserveMaterials(id, true), variant: 'secondary' },
  { label: 'Mark ready', from: ['MATERIALS_RESERVED'], roles: MANAGE, run: productionOrdersApi.ready },
  { label: 'Start', from: ['READY'], roles: FLOOR, run: productionOrdersApi.start },
  { label: 'Pause', from: ['IN_PROGRESS'], roles: FLOOR, run: productionOrdersApi.pause, variant: 'secondary' },
  { label: 'Resume', from: ['PAUSED'], roles: FLOOR, run: productionOrdersApi.resume },
  { label: 'Resume from hold', from: ['ON_HOLD'], roles: [...FLOOR, 'PRODUCTION_MANAGER'], run: productionOrdersApi.resumeFromHold },
  { label: 'Complete', from: ['IN_PROGRESS'], roles: MANAGE, run: productionOrdersApi.complete },
]

const HOLDABLE: OrderStatus[] = ['APPROVED', 'MATERIALS_RESERVED', 'READY', 'IN_PROGRESS', 'PAUSED']
const CANCELLABLE: OrderStatus[] = ['DRAFT', 'PLANNED', 'PENDING_APPROVAL', 'APPROVED', 'MATERIALS_RESERVED', 'READY', 'PAUSED', 'ON_HOLD']

export function ProductionOrderDetailPage() {
  const { id } = useParams()
  const orderId = Number(id)
  const { hasRole } = useAuth()
  const order = useAsync(() => productionOrdersApi.get(orderId), [orderId])
  const check = useAsync(() => productionOrdersApi.materialCheck(orderId), [orderId])
  const materials = useAsync(() => materialsApi.list())
  const locations = useAsync(() => storageLocationsApi.list())
  const refresh = () => {
    order.reload()
    check.reload()
  }
  const action = useAction(refresh)

  const [reasonFor, setReasonFor] = useState<'hold' | 'cancel' | null>(null)
  const [reason, setReason] = useState('')
  const [reportOpen, setReportOpen] = useState(false)
  const [completed, setCompleted] = useState('')
  const [rejected, setRejected] = useState('')
  const [downtime, setDowntime] = useState('0')
  const [locationId, setLocationId] = useState('')

  const o = order.data
  const materialName = (mid: number) => materials.data?.find((m) => m.id === mid)?.name ?? `Material #${mid}`

  if (!o) {
    return (
      <>
        <ErrorBanner error={order.error} />
        {order.loading && <p className="text-ink-300">Loading…</p>}
      </>
    )
  }

  const available = TRANSITIONS.filter((t) => t.from.includes(o.status) && hasRole(...t.roles))
  const canReport = o.status === 'IN_PROGRESS' && hasRole('ADMIN', 'SUPERVISOR', 'WORKER')

  async function submitReason() {
    if (!reasonFor || !reason.trim()) return
    await action.run(() => (reasonFor === 'hold' ? productionOrdersApi.hold(orderId, reason) : productionOrdersApi.cancel(orderId, reason)))
    setReasonFor(null)
    setReason('')
  }

  async function submitReport() {
    // A fresh key per submit click; reusing it on retry is what makes a double-click or network retry safe.
    const idempotencyKey = crypto.randomUUID()
    await action.run(() =>
      productionOrdersApi.report(orderId, {
        completedQuantity: Number(completed || 0),
        rejectedQuantity: Number(rejected || 0),
        downtimeMinutes: Number(downtime || 0),
        locationId: locationId ? Number(locationId) : undefined,
        idempotencyKey,
      }),
    )
    setReportOpen(false)
    setCompleted('')
    setRejected('')
  }

  return (
    <>
      <PageHeader
        title={o.orderNumber}
        subtitle={`Planned ${o.plannedQuantity} · completed ${o.completedQuantity} · rejected ${o.rejectedQuantity}`}
        actions={<StatusBadge value={o.status} />}
      />
      <ErrorBanner error={action.error ?? order.error} />
      {o.statusReason && <p className="mb-4 text-sm text-ink-300">Reason: {o.statusReason}</p>}

      <div className="panel p-4 mb-5 flex flex-wrap gap-2">
        {available.map((t) => (
          <button
            key={t.label}
            className={t.variant === 'secondary' ? 'btn-secondary' : 'btn-primary'}
            disabled={action.pending}
            onClick={() => action.run(() => t.run(orderId))}
          >
            {t.label}
          </button>
        ))}
        {canReport && (
          <button className="btn-primary" onClick={() => setReportOpen(true)}>
            Report production
          </button>
        )}
        {hasRole('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR') && HOLDABLE.includes(o.status) && (
          <button className="btn-secondary" onClick={() => setReasonFor('hold')}>
            Put on hold
          </button>
        )}
        {hasRole(...MANAGE) && CANCELLABLE.includes(o.status) && (
          <button className="btn-danger" onClick={() => setReasonFor('cancel')}>
            Cancel order
          </button>
        )}
        {available.length === 0 && !canReport && <span className="text-sm text-ink-400">No actions available for your role at this stage.</span>}
      </div>

      <h2 className="text-sm font-medium mb-2">Material requirements</h2>
      <DataTable
        rows={check.data ?? []}
        rowKey={(r) => r.materialId}
        columns={[
          { header: 'Material', cell: (r) => materialName(r.materialId) },
          { header: 'Required', cell: (r) => r.requiredQuantity, className: 'text-right tabular-nums' },
          { header: 'Available', cell: (r) => r.availableQuantity, className: 'text-right tabular-nums' },
          {
            header: 'Shortage',
            cell: (r) => (r.shortageQuantity > 0 ? <span className="text-rust-500">{r.shortageQuantity}</span> : '—'),
            className: 'text-right tabular-nums',
          },
        ]}
      />

      <h2 className="text-sm font-medium mt-6 mb-2">Order lines</h2>
      <DataTable
        rows={o.items}
        rowKey={(i) => i.id}
        columns={[
          { header: 'Material', cell: (i) => materialName(i.materialId) },
          { header: 'Required', cell: (i) => i.requiredQuantity, className: 'text-right tabular-nums' },
          { header: 'Reserved', cell: (i) => i.reservedQuantity, className: 'text-right tabular-nums' },
          { header: 'Consumed', cell: (i) => i.consumedQuantity, className: 'text-right tabular-nums' },
        ]}
      />

      <Modal title={reasonFor === 'hold' ? 'Put order on hold' : 'Cancel order'} open={reasonFor !== null} onClose={() => setReasonFor(null)}>
        <label className="block mb-3">
          <span className="field-label">Reason (required)</span>
          <textarea className="field-input" rows={3} value={reason} onChange={(e) => setReason(e.target.value)} />
        </label>
        <div className="flex justify-end gap-2">
          <button className="btn-secondary" onClick={() => setReasonFor(null)}>
            Back
          </button>
          <button className={reasonFor === 'cancel' ? 'btn-danger' : 'btn-primary'} disabled={!reason.trim() || action.pending} onClick={submitReason}>
            Confirm
          </button>
        </div>
      </Modal>

      <Modal title="Report production" open={reportOpen} onClose={() => setReportOpen(false)}>
        <div className="grid grid-cols-2 gap-3">
          <label>
            <span className="field-label">Completed</span>
            <input className="field-input" inputMode="decimal" value={completed} onChange={(e) => setCompleted(e.target.value)} />
          </label>
          <label>
            <span className="field-label">Rejected</span>
            <input className="field-input" inputMode="decimal" value={rejected} onChange={(e) => setRejected(e.target.value)} />
          </label>
          <label>
            <span className="field-label">Downtime (min)</span>
            <input className="field-input" inputMode="numeric" value={downtime} onChange={(e) => setDowntime(e.target.value)} />
          </label>
          <label>
            <span className="field-label">Output location</span>
            <select className="field-input" value={locationId} onChange={(e) => setLocationId(e.target.value)}>
              <option value="">Select…</option>
              {locations.data?.map((l) => (
                <option key={l.id} value={l.id}>
                  {l.code} — {l.name}
                </option>
              ))}
            </select>
          </label>
        </div>
        <p className="text-xs text-ink-400 mt-3">Completed output is held in quarantine until Quality inspects and releases it.</p>
        <div className="flex justify-end gap-2 mt-4">
          <button className="btn-secondary" onClick={() => setReportOpen(false)}>
            Cancel
          </button>
          <button className="btn-primary" disabled={action.pending} onClick={submitReport}>
            Submit report
          </button>
        </div>
      </Modal>
    </>
  )
}
