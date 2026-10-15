import { useState } from 'react'
import { inventoryApi } from '../../api/inventory'
import { materialsApi, productsApi, storageLocationsApi } from '../../api/masterData'
import { useAuth } from '../../auth/AuthContext'
import { DataTable } from '../../components/DataTable'
import { ErrorBanner } from '../../components/ErrorBanner'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useAction, useAsync } from '../../hooks/useAsync'
import type { InventoryCategory } from '../../types/domain'

const CATEGORIES: InventoryCategory[] = ['RAW_MATERIAL', 'WORK_IN_PROGRESS', 'FINISHED_GOOD', 'DAMAGED', 'QUARANTINED']

export function InventoryPage() {
  const { hasRole } = useAuth()
  const [tab, setTab] = useState<'stock' | 'transactions'>('stock')
  const [category, setCategory] = useState('')
  const stock = useAsync(() => inventoryApi.list(category ? { category: category as InventoryCategory } : undefined), [category])
  const tx = useAsync(() => inventoryApi.transactions({ page: 0, size: 50 }), [])
  const materials = useAsync(() => materialsApi.list())
  const products = useAsync(() => productsApi.list())
  const locations = useAsync(() => storageLocationsApi.list())

  const [receiveOpen, setReceiveOpen] = useState(false)
  const [adjustTarget, setAdjustTarget] = useState<number | null>(null)
  const [form, setForm] = useState({ materialId: '', locationId: '', quantity: '', unitCost: '', batchNumber: '', expirationDate: '' })
  const [adjust, setAdjust] = useState({ delta: '', reason: '' })
  const action = useAction(() => {
    stock.reload()
    tx.reload()
  })

  const canMutate = hasRole('ADMIN', 'INVENTORY_MANAGER')
  const itemName = (row: { materialId: number | null; productId: number | null }) =>
    row.materialId
      ? (materials.data?.find((m) => m.id === row.materialId)?.name ?? `Material #${row.materialId}`)
      : row.productId
        ? (products.data?.find((p) => p.id === row.productId)?.name ?? `Product #${row.productId}`)
        : '—'

  async function submitReceive() {
    const location = locations.data?.find((l) => l.id === Number(form.locationId))
    if (!location) return
    await action.run(() =>
      inventoryApi.receive({
        materialId: Number(form.materialId),
        factoryId: location.factoryId,
        locationId: location.id,
        quantity: Number(form.quantity),
        unitCost: form.unitCost ? Number(form.unitCost) : undefined,
        batchNumber: form.batchNumber || undefined,
        expirationDate: form.expirationDate || undefined,
      }),
    )
    setReceiveOpen(false)
  }

  async function submitAdjust() {
    if (adjustTarget == null) return
    await action.run(() => inventoryApi.adjust({ inventoryId: adjustTarget, deltaQuantity: Number(adjust.delta), reason: adjust.reason }))
    setAdjustTarget(null)
    setAdjust({ delta: '', reason: '' })
  }

  return (
    <>
      <PageHeader
        title="Inventory"
        subtitle="Stock by item, batch and location. Available = on hand − reserved."
        actions={
          canMutate && (
            <button className="btn-primary" onClick={() => setReceiveOpen(true)}>
              Receive material
            </button>
          )
        }
      />
      <ErrorBanner error={action.error ?? stock.error} />
      <div className="flex gap-2 mb-4" role="tablist">
        {(['stock', 'transactions'] as const).map((t) => (
          <button key={t} role="tab" aria-selected={tab === t} className={tab === t ? 'btn-primary' : 'btn-secondary'} onClick={() => setTab(t)}>
            {t === 'stock' ? 'Stock' : 'Transaction ledger'}
          </button>
        ))}
        {tab === 'stock' && (
          <select className="field-input w-48 ml-auto" value={category} onChange={(e) => setCategory(e.target.value)} aria-label="Filter by category">
            <option value="">All categories</option>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {c.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        )}
      </div>

      {tab === 'stock' ? (
        <DataTable
          rows={stock.data ?? []}
          rowKey={(r) => r.id}
          columns={[
            { header: 'Item', cell: (r) => itemName(r) },
            { header: 'Batch', cell: (r) => <span className="font-mono text-xs">{r.batchNumber ?? r.batchId}</span> },
            { header: 'Category', cell: (r) => <StatusBadge value={r.category} /> },
            { header: 'On hand', cell: (r) => r.quantityOnHand, className: 'text-right tabular-nums' },
            { header: 'Reserved', cell: (r) => r.reservedQuantity, className: 'text-right tabular-nums' },
            { header: 'Available', cell: (r) => r.availableQuantity, className: 'text-right tabular-nums' },
            { header: 'Expires', cell: (r) => r.expirationDate ?? '—' },
            {
              header: '',
              cell: (r) =>
                canMutate ? (
                  <button className="btn-ghost" onClick={() => setAdjustTarget(r.id)}>
                    Adjust
                  </button>
                ) : null,
            },
          ]}
        />
      ) : (
        <DataTable
          rows={tx.data?.content ?? []}
          rowKey={(r) => r.id}
          columns={[
            { header: 'When', cell: (r) => new Date(r.createdAt).toLocaleString() },
            { header: 'Txn', cell: (r) => <span className="font-mono text-xs">{r.transactionNumber}</span> },
            { header: 'Type', cell: (r) => <StatusBadge value={r.type} /> },
            { header: 'Qty', cell: (r) => r.quantity, className: 'text-right tabular-nums' },
            { header: 'Before → after', cell: (r) => `${r.previousQuantity} → ${r.newQuantity}`, className: 'tabular-nums' },
            { header: 'Reference', cell: (r) => (r.referenceType ? `${r.referenceType} #${r.referenceId ?? ''}` : '—') },
            { header: 'Notes', cell: (r) => r.notes ?? '' },
          ]}
        />
      )}

      <Modal title="Receive material" open={receiveOpen} onClose={() => setReceiveOpen(false)}>
        <div className="grid grid-cols-2 gap-3">
          <label className="col-span-2">
            <span className="field-label">Material</span>
            <select className="field-input" value={form.materialId} onChange={(e) => setForm({ ...form, materialId: e.target.value })}>
              <option value="">Select…</option>
              {materials.data?.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.code} — {m.name}
                </option>
              ))}
            </select>
          </label>
          <label className="col-span-2">
            <span className="field-label">Storage location</span>
            <select className="field-input" value={form.locationId} onChange={(e) => setForm({ ...form, locationId: e.target.value })}>
              <option value="">Select…</option>
              {locations.data?.map((l) => (
                <option key={l.id} value={l.id}>
                  {l.code} — {l.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            <span className="field-label">Quantity</span>
            <input className="field-input" inputMode="decimal" value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} />
          </label>
          <label>
            <span className="field-label">Unit cost</span>
            <input className="field-input" inputMode="decimal" value={form.unitCost} onChange={(e) => setForm({ ...form, unitCost: e.target.value })} />
          </label>
          <label>
            <span className="field-label">Batch no. (optional)</span>
            <input className="field-input" value={form.batchNumber} onChange={(e) => setForm({ ...form, batchNumber: e.target.value })} />
          </label>
          <label>
            <span className="field-label">Expiry (optional)</span>
            <input type="date" className="field-input" value={form.expirationDate} onChange={(e) => setForm({ ...form, expirationDate: e.target.value })} />
          </label>
        </div>
        <div className="flex justify-end gap-2 mt-4">
          <button className="btn-secondary" onClick={() => setReceiveOpen(false)}>
            Cancel
          </button>
          <button className="btn-primary" disabled={action.pending || !form.materialId || !form.locationId || !form.quantity} onClick={submitReceive}>
            Receive
          </button>
        </div>
      </Modal>

      <Modal title="Adjust stock" open={adjustTarget !== null} onClose={() => setAdjustTarget(null)}>
        <label className="block mb-3">
          <span className="field-label">Change (negative to remove)</span>
          <input className="field-input" inputMode="decimal" value={adjust.delta} onChange={(e) => setAdjust({ ...adjust, delta: e.target.value })} />
        </label>
        <label className="block mb-3">
          <span className="field-label">Reason (required, audited)</span>
          <textarea className="field-input" rows={2} value={adjust.reason} onChange={(e) => setAdjust({ ...adjust, reason: e.target.value })} />
        </label>
        <div className="flex justify-end gap-2">
          <button className="btn-secondary" onClick={() => setAdjustTarget(null)}>
            Cancel
          </button>
          <button className="btn-primary" disabled={action.pending || !adjust.delta || !adjust.reason.trim()} onClick={submitAdjust}>
            Apply
          </button>
        </div>
      </Modal>
    </>
  )
}
