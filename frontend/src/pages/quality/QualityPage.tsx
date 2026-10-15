import { useState } from 'react'
import { defectsApi, inspectionsApi, reworkApi, scrapApi } from '../../api/quality'
import { productionOrdersApi } from '../../api/productionOrders'
import { useAuth } from '../../auth/AuthContext'
import { DataTable } from '../../components/DataTable'
import { ErrorBanner } from '../../components/ErrorBanner'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useAction, useAsync } from '../../hooks/useAsync'
import type { Severity } from '../../types/domain'

const SCRAP_THRESHOLD = 50

export function QualityPage() {
  const { hasRole } = useAuth()
  const canInspect = hasRole('ADMIN', 'QUALITY_INSPECTOR')
  const canManage = hasRole('ADMIN', 'PRODUCTION_MANAGER')
  const orders = useAsync(() => productionOrdersApi.list())
  const [orderId, setOrderId] = useState<number | null>(null)
  const load = <T,>(fn: (id: number) => Promise<T[]>) => () => (orderId ? fn(orderId) : Promise.resolve([] as T[]))
  const inspections = useAsync(load(inspectionsApi.listForOrder), [orderId])
  const defects = useAsync(load(defectsApi.listForOrder), [orderId])
  const rework = useAsync(load(reworkApi.listForOrder), [orderId])
  const scrap = useAsync(load(scrapApi.listForOrder), [orderId])
  const action = useAction(() => {
    inspections.reload()
    defects.reload()
    rework.reload()
    scrap.reload()
  })

  const [inspOpen, setInspOpen] = useState(false)
  const [insp, setInsp] = useState({ outputId: '', passedQuantity: '', failedQuantity: '', notes: '' })
  const [defOpen, setDefOpen] = useState(false)
  const [def, setDef] = useState({ defectType: '', description: '', severity: 'MEDIUM' as Severity })
  const [scrapOpen, setScrapOpen] = useState(false)
  const [sc, setSc] = useState({ outputId: '', quantity: '', reason: '' })
  const [rwOpen, setRwOpen] = useState(false)
  const [rw, setRw] = useState({ outputId: '', quantity: '' })
  const [completing, setCompleting] = useState<number | null>(null)
  const [passed, setPassed] = useState('')

  const scrapNeedsApproval = Number(sc.quantity) >= SCRAP_THRESHOLD

  return (
    <>
      <PageHeader
        title="Quality"
        subtitle="Finished goods are quarantined until inspected. Finalizing releases only the passed quantity."
        actions={
          <select className="field-input w-72" aria-label="Production order" value={orderId ?? ''} onChange={(e) => setOrderId(e.target.value ? Number(e.target.value) : null)}>
            <option value="">Select a production order…</option>
            {orders.data?.map((o) => (
              <option key={o.id} value={o.id}>
                {o.orderNumber} ({o.status})
              </option>
            ))}
          </select>
        }
      />
      <ErrorBanner error={action.error ?? orders.error} />
      {!orderId ? (
        <p className="text-ink-400">Choose a production order to see its quality records.</p>
      ) : (
        <div className="space-y-8">
          <section>
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-sm font-medium">Inspections</h3>
              {canInspect && (
                <button className="btn-primary" onClick={() => setInspOpen(true)}>
                  Record inspection
                </button>
              )}
            </div>
            <DataTable
              rows={inspections.data ?? []}
              rowKey={(i) => i.id}
              columns={[
                { header: 'No.', cell: (i) => <span className="font-mono text-xs">{i.inspectionNumber}</span> },
                { header: 'Output', cell: (i) => `#${i.outputId}` },
                { header: 'Passed', cell: (i) => i.passedQuantity, className: 'text-right tabular-nums' },
                { header: 'Failed', cell: (i) => i.failedQuantity, className: 'text-right tabular-nums' },
                { header: 'Result', cell: (i) => <StatusBadge value={i.result} /> },
                { header: 'Finalized', cell: (i) => (i.finalized ? 'Yes' : 'No') },
                {
                  header: '',
                  cell: (i) =>
                    canInspect && !i.finalized ? (
                      <button className="btn-primary" onClick={() => action.run(() => inspectionsApi.finalize(i.id))}>
                        Finalize
                      </button>
                    ) : null,
                },
              ]}
            />
          </section>

          <section>
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-sm font-medium">Defects</h3>
              {canInspect && (
                <button className="btn-secondary" onClick={() => setDefOpen(true)}>
                  Report defect
                </button>
              )}
            </div>
            <DataTable
              rows={defects.data ?? []}
              rowKey={(d) => d.id}
              columns={[
                { header: 'Code', cell: (d) => <span className="font-mono text-xs">{d.defectCode}</span> },
                { header: 'Type', cell: (d) => d.defectType },
                { header: 'Severity', cell: (d) => <StatusBadge value={d.severity} /> },
                { header: 'Status', cell: (d) => <StatusBadge value={d.status} /> },
                {
                  header: '',
                  cell: (d) =>
                    canInspect && d.status !== 'CLOSED' ? (
                      <select
                        className="field-input w-40"
                        aria-label="Update status"
                        value=""
                        onChange={(e) => e.target.value && action.run(() => defectsApi.updateStatus(d.id, e.target.value))}
                      >
                        <option value="">Move to…</option>
                        {['UNDER_REVIEW', 'CORRECTIVE_ACTION', 'RESOLVED', 'CLOSED'].filter((s) => s !== d.status).map((s) => (
                          <option key={s}>{s}</option>
                        ))}
                      </select>
                    ) : null,
                },
              ]}
            />
          </section>

          <section>
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-sm font-medium">Rework</h3>
              {(canInspect || canManage) && (
                <button className="btn-secondary" onClick={() => setRwOpen(true)}>
                  Request rework
                </button>
              )}
            </div>
            <DataTable
              rows={rework.data ?? []}
              rowKey={(r) => r.id}
              columns={[
                { header: 'No.', cell: (r) => <span className="font-mono text-xs">{r.reworkNumber}</span> },
                { header: 'Qty', cell: (r) => r.quantity, className: 'text-right tabular-nums' },
                { header: 'Passed', cell: (r) => r.passedQuantity, className: 'text-right tabular-nums' },
                { header: 'Status', cell: (r) => <StatusBadge value={r.status} /> },
                {
                  header: '',
                  cell: (r) => (
                    <div className="flex gap-1 justify-end">
                      {r.status === 'REQUESTED' && (
                        <button className="btn-primary" onClick={() => action.run(() => reworkApi.start(r.id))}>
                          Start
                        </button>
                      )}
                      {r.status === 'IN_PROGRESS' && (
                        <button className="btn-primary" onClick={() => setCompleting(r.id)}>
                          Complete
                        </button>
                      )}
                      {['REQUESTED', 'IN_PROGRESS'].includes(r.status) && (
                        <button className="btn-ghost" onClick={() => action.run(() => reworkApi.cancel(r.id))}>
                          Cancel
                        </button>
                      )}
                    </div>
                  ),
                },
              ]}
            />
          </section>

          <section>
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-sm font-medium">Scrap</h3>
              {(canInspect || canManage) && (
                <button className="btn-danger" onClick={() => setScrapOpen(true)}>
                  Scrap quantity
                </button>
              )}
            </div>
            <DataTable
              rows={scrap.data ?? []}
              rowKey={(s) => s.id}
              columns={[
                { header: 'No.', cell: (s) => <span className="font-mono text-xs">{s.scrapNumber}</span> },
                { header: 'Qty', cell: (s) => s.quantity, className: 'text-right tabular-nums' },
                { header: 'Reason', cell: (s) => s.reason },
                { header: 'When', cell: (s) => new Date(s.createdAt).toLocaleString() },
              ]}
            />
          </section>
        </div>
      )}

      <Modal title="Record inspection" open={inspOpen} onClose={() => setInspOpen(false)}>
        <div className="grid grid-cols-3 gap-3 mb-3">
          <label>
            <span className="field-label">Output ID</span>
            <input className="field-input" value={insp.outputId} onChange={(e) => setInsp({ ...insp, outputId: e.target.value })} />
          </label>
          <label>
            <span className="field-label">Passed</span>
            <input className="field-input" inputMode="decimal" value={insp.passedQuantity} onChange={(e) => setInsp({ ...insp, passedQuantity: e.target.value })} />
          </label>
          <label>
            <span className="field-label">Failed</span>
            <input className="field-input" inputMode="decimal" value={insp.failedQuantity} onChange={(e) => setInsp({ ...insp, failedQuantity: e.target.value })} />
          </label>
        </div>
        <label className="block mb-3">
          <span className="field-label">Notes</span>
          <textarea className="field-input" rows={2} value={insp.notes} onChange={(e) => setInsp({ ...insp, notes: e.target.value })} />
        </label>
        <div className="flex justify-end gap-2">
          <button className="btn-secondary" onClick={() => setInspOpen(false)}>
            Cancel
          </button>
          <button
            className="btn-primary"
            disabled={action.pending || !insp.outputId || insp.passedQuantity === '' || insp.failedQuantity === ''}
            onClick={async () => {
              await action.run(() =>
                inspectionsApi.record({ outputId: Number(insp.outputId), passedQuantity: Number(insp.passedQuantity), failedQuantity: Number(insp.failedQuantity), notes: insp.notes || undefined }),
              )
              setInspOpen(false)
            }}
          >
            Record
          </button>
        </div>
      </Modal>

      <Modal title="Report defect" open={defOpen} onClose={() => setDefOpen(false)}>
        <label className="block mb-3">
          <span className="field-label">Defect type</span>
          <input className="field-input" value={def.defectType} onChange={(e) => setDef({ ...def, defectType: e.target.value })} />
        </label>
        <label className="block mb-3">
          <span className="field-label">Severity</span>
          <select className="field-input" value={def.severity} onChange={(e) => setDef({ ...def, severity: e.target.value as Severity })}>
            {['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
        </label>
        <label className="block mb-3">
          <span className="field-label">Description</span>
          <textarea className="field-input" rows={2} value={def.description} onChange={(e) => setDef({ ...def, description: e.target.value })} />
        </label>
        <div className="flex justify-end gap-2">
          <button className="btn-secondary" onClick={() => setDefOpen(false)}>
            Cancel
          </button>
          <button
            className="btn-primary"
            disabled={action.pending || !def.defectType.trim()}
            onClick={async () => {
              await action.run(() => defectsApi.report({ ...def, description: def.description || undefined, productionOrderId: orderId ?? undefined }))
              setDefOpen(false)
            }}
          >
            Report
          </button>
        </div>
      </Modal>

      <Modal title="Request rework" open={rwOpen} onClose={() => setRwOpen(false)}>
        <div className="grid grid-cols-2 gap-3 mb-3">
          <label>
            <span className="field-label">Output ID</span>
            <input className="field-input" value={rw.outputId} onChange={(e) => setRw({ ...rw, outputId: e.target.value })} />
          </label>
          <label>
            <span className="field-label">Quantity</span>
            <input className="field-input" inputMode="decimal" value={rw.quantity} onChange={(e) => setRw({ ...rw, quantity: e.target.value })} />
          </label>
        </div>
        <div className="flex justify-end gap-2">
          <button className="btn-secondary" onClick={() => setRwOpen(false)}>
            Cancel
          </button>
          <button
            className="btn-primary"
            disabled={action.pending || !rw.outputId || !rw.quantity}
            onClick={async () => {
              await action.run(() => reworkApi.request({ outputId: Number(rw.outputId), quantity: Number(rw.quantity) }))
              setRwOpen(false)
            }}
          >
            Request
          </button>
        </div>
      </Modal>

      <Modal title="Complete rework" open={completing !== null} onClose={() => setCompleting(null)}>
        <label className="block mb-3">
          <span className="field-label">Quantity that now passes</span>
          <input className="field-input" inputMode="decimal" value={passed} onChange={(e) => setPassed(e.target.value)} />
        </label>
        <div className="flex justify-end gap-2">
          <button className="btn-secondary" onClick={() => setCompleting(null)}>
            Cancel
          </button>
          <button
            className="btn-primary"
            disabled={action.pending || passed === ''}
            onClick={async () => {
              if (completing == null) return
              await action.run(() => reworkApi.complete(completing, Number(passed)))
              setCompleting(null)
              setPassed('')
            }}
          >
            Complete
          </button>
        </div>
      </Modal>

      <Modal title="Scrap quantity" open={scrapOpen} onClose={() => setScrapOpen(false)}>
        <div className="grid grid-cols-2 gap-3 mb-3">
          <label>
            <span className="field-label">Output ID</span>
            <input className="field-input" value={sc.outputId} onChange={(e) => setSc({ ...sc, outputId: e.target.value })} />
          </label>
          <label>
            <span className="field-label">Quantity</span>
            <input className="field-input" inputMode="decimal" value={sc.quantity} onChange={(e) => setSc({ ...sc, quantity: e.target.value })} />
          </label>
        </div>
        <label className="block mb-3">
          <span className="field-label">Reason (required)</span>
          <textarea className="field-input" rows={2} value={sc.reason} onChange={(e) => setSc({ ...sc, reason: e.target.value })} />
        </label>
        {scrapNeedsApproval && (
          <p className="text-xs text-amber-400 mb-3">
            Scrap of {SCRAP_THRESHOLD} or more requires an Admin or Production Manager.{canManage ? '' : ' You do not have that role; the server will reject this.'}
          </p>
        )}
        <div className="flex justify-end gap-2">
          <button className="btn-secondary" onClick={() => setScrapOpen(false)}>
            Cancel
          </button>
          <button
            className="btn-danger"
            disabled={action.pending || !sc.outputId || !sc.quantity || !sc.reason.trim()}
            onClick={async () => {
              await action.run(() => scrapApi.scrap({ outputId: Number(sc.outputId), productionOrderId: orderId ?? undefined, quantity: Number(sc.quantity), reason: sc.reason }))
              setScrapOpen(false)
            }}
          >
            Scrap
          </button>
        </div>
      </Modal>
    </>
  )
}
