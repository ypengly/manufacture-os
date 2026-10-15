import { useState } from 'react'
import { purchaseOrdersApi } from '../../api/purchaseOrders'
import { factoriesApi, materialsApi, storageLocationsApi, suppliersApi } from '../../api/masterData'
import { useAuth } from '../../auth/AuthContext'
import { DataTable } from '../../components/DataTable'
import { ErrorBanner } from '../../components/ErrorBanner'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useAction, useAsync } from '../../hooks/useAsync'
import type { PurchaseOrder } from '../../types/domain'

export function PurchaseOrdersPage() {
  const { hasRole } = useAuth()
  const orders = useAsync(() => purchaseOrdersApi.list())
  const suppliers = useAsync(() => suppliersApi.list())
  const factories = useAsync(() => factoriesApi.list())
  const materials = useAsync(() => materialsApi.list())
  const locations = useAsync(() => storageLocationsApi.list())
  const action = useAction(orders.reload)

  const [createOpen, setCreateOpen] = useState(false)
  const [header, setHeader] = useState({ supplierId: '', factoryId: '', expectedDate: '' })
  const [lines, setLines] = useState([{ materialId: '', quantity: '', unitCost: '' }])

  const [receiving, setReceiving] = useState<PurchaseOrder | null>(null)
  const [locationId, setLocationId] = useState('')
  const [qtys, setQtys] = useState<Record<number, string>>({})

  const supplierName = (id: number) => suppliers.data?.find((s) => s.id === id)?.companyName ?? `#${id}`
  const materialName = (id: number) => materials.data?.find((m) => m.id === id)?.name ?? `#${id}`

  async function create() {
    await action.run(() =>
      purchaseOrdersApi.create({
        supplierId: Number(header.supplierId),
        factoryId: Number(header.factoryId),
        expectedDate: header.expectedDate || undefined,
        items: lines
          .filter((l) => l.materialId && l.quantity)
          .map((l) => ({ materialId: Number(l.materialId), quantity: Number(l.quantity), unitCost: l.unitCost ? Number(l.unitCost) : undefined })),
      }),
    )
    setCreateOpen(false)
  }

  async function receive() {
    if (!receiving) return
    const toReceive = receiving.items
      .filter((i) => Number(qtys[i.id] ?? 0) > 0)
      .map((i) => ({ itemId: i.id, quantity: Number(qtys[i.id]), locationId: Number(locationId) }))
    await action.run(() => purchaseOrdersApi.receive(receiving.id, toReceive))
    setReceiving(null)
    setQtys({})
  }

  return (
    <>
      <PageHeader
        title="Purchasing"
        subtitle="Purchase orders and goods receiving"
        actions={
          <button className="btn-primary" onClick={() => setCreateOpen(true)}>
            New purchase order
          </button>
        }
      />
      <ErrorBanner error={action.error ?? orders.error} />
      <DataTable
        rows={orders.data ?? []}
        rowKey={(o) => o.id}
        columns={[
          { header: 'PO', cell: (o) => <span className="font-mono text-xs">{o.poNumber}</span> },
          { header: 'Supplier', cell: (o) => supplierName(o.supplierId) },
          { header: 'Status', cell: (o) => <StatusBadge value={o.status} /> },
          {
            header: 'Lines (received / ordered)',
            cell: (o) => o.items.map((i) => `${materialName(i.materialId)}: ${i.receivedQuantity}/${i.quantity}`).join(' · '),
          },
          { header: 'Expected', cell: (o) => o.expectedDate ?? '—' },
          {
            header: '',
            cell: (o) => (
              <div className="flex gap-1 justify-end">
                {o.status === 'DRAFT' && (
                  <button className="btn-secondary" onClick={() => action.run(() => purchaseOrdersApi.submit(o.id))}>
                    Submit
                  </button>
                )}
                {o.status === 'SUBMITTED' && hasRole('ADMIN') && (
                  <button className="btn-primary" onClick={() => action.run(() => purchaseOrdersApi.approve(o.id))}>
                    Approve
                  </button>
                )}
                {(o.status === 'APPROVED' || o.status === 'PARTIALLY_RECEIVED') && hasRole('ADMIN', 'INVENTORY_MANAGER') && (
                  <button className="btn-primary" onClick={() => setReceiving(o)}>
                    Receive
                  </button>
                )}
                {['DRAFT', 'SUBMITTED', 'APPROVED'].includes(o.status) && (
                  <button className="btn-ghost" onClick={() => action.run(() => purchaseOrdersApi.cancel(o.id))}>
                    Cancel
                  </button>
                )}
              </div>
            ),
          },
        ]}
      />

      <Modal title="New purchase order" open={createOpen} onClose={() => setCreateOpen(false)}>
        <div className="grid grid-cols-2 gap-3 mb-2">
          <label>
            <span className="field-label">Supplier</span>
            <select className="field-input" value={header.supplierId} onChange={(e) => setHeader({ ...header, supplierId: e.target.value })}>
              <option value="">Select…</option>
              {suppliers.data?.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.companyName}
                </option>
              ))}
            </select>
          </label>
          <label>
            <span className="field-label">Deliver to factory</span>
            <select className="field-input" value={header.factoryId} onChange={(e) => setHeader({ ...header, factoryId: e.target.value })}>
              <option value="">Select…</option>
              {factories.data?.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </select>
          </label>
          <label className="col-span-2">
            <span className="field-label">Expected date</span>
            <input type="date" className="field-input" value={header.expectedDate} onChange={(e) => setHeader({ ...header, expectedDate: e.target.value })} />
          </label>
        </div>
        {lines.map((l, i) => (
          <div key={i} className="grid grid-cols-[1fr_5rem_5rem] gap-2 mb-2">
            <select className="field-input" aria-label="Material" value={l.materialId} onChange={(e) => setLines(lines.map((x, j) => (j === i ? { ...x, materialId: e.target.value } : x)))}>
              <option value="">Material…</option>
              {materials.data?.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.name}
                </option>
              ))}
            </select>
            <input className="field-input" placeholder="Qty" inputMode="decimal" value={l.quantity} onChange={(e) => setLines(lines.map((x, j) => (j === i ? { ...x, quantity: e.target.value } : x)))} />
            <input className="field-input" placeholder="Cost" inputMode="decimal" value={l.unitCost} onChange={(e) => setLines(lines.map((x, j) => (j === i ? { ...x, unitCost: e.target.value } : x)))} />
          </div>
        ))}
        <button className="btn-ghost" onClick={() => setLines([...lines, { materialId: '', quantity: '', unitCost: '' }])}>
          + Add line
        </button>
        <div className="flex justify-end gap-2 mt-4">
          <button className="btn-secondary" onClick={() => setCreateOpen(false)}>
            Cancel
          </button>
          <button className="btn-primary" disabled={action.pending || !header.supplierId || !header.factoryId} onClick={create}>
            Create draft
          </button>
        </div>
      </Modal>

      <Modal title={`Receive ${receiving?.poNumber ?? ''}`} open={receiving !== null} onClose={() => setReceiving(null)}>
        <label className="block mb-3">
          <span className="field-label">Storage location</span>
          <select className="field-input" value={locationId} onChange={(e) => setLocationId(e.target.value)}>
            <option value="">Select…</option>
            {locations.data
              ?.filter((l) => l.factoryId === receiving?.factoryId)
              .map((l) => (
                <option key={l.id} value={l.id}>
                  {l.code} — {l.name}
                </option>
              ))}
          </select>
        </label>
        {receiving?.items.map((i) => (
          <label key={i.id} className="flex items-center justify-between gap-3 mb-2">
            <span className="text-sm">
              {materialName(i.materialId)} <span className="text-ink-400">(outstanding {i.quantity - i.receivedQuantity})</span>
            </span>
            <input className="field-input w-24" inputMode="decimal" value={qtys[i.id] ?? ''} onChange={(e) => setQtys({ ...qtys, [i.id]: e.target.value })} />
          </label>
        ))}
        <div className="flex justify-end gap-2 mt-4">
          <button className="btn-secondary" onClick={() => setReceiving(null)}>
            Cancel
          </button>
          <button className="btn-primary" disabled={action.pending || !locationId} onClick={receive}>
            Confirm receipt
          </button>
        </div>
      </Modal>
    </>
  )
}
