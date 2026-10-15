import { useState } from 'react'
import { bomsApi } from '../../api/boms'
import { materialsApi, productsApi } from '../../api/masterData'
import { DataTable } from '../../components/DataTable'
import { ErrorBanner } from '../../components/ErrorBanner'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useAction, useAsync } from '../../hooks/useAsync'
import type { BomComponentInput } from '../../types/domain'

export function BomsPage() {
  const products = useAsync(() => productsApi.list())
  const materials = useAsync(() => materialsApi.list())
  const [productId, setProductId] = useState<number | null>(null)
  const boms = useAsync(() => (productId ? bomsApi.listForProduct(productId) : Promise.resolve([])), [productId])
  const action = useAction(boms.reload)

  const [open, setOpen] = useState(false)
  const [components, setComponents] = useState<BomComponentInput[]>([{ materialId: 0, quantity: 1, wastePercent: 0 }])
  const materialName = (id: number) => materials.data?.find((m) => m.id === id)?.name ?? `#${id}`

  async function create() {
    if (!productId) return
    await action.run(() => bomsApi.create({ productId, components: components.filter((c) => c.materialId > 0) }))
    setOpen(false)
    setComponents([{ materialId: 0, quantity: 1, wastePercent: 0 }])
  }

  return (
    <>
      <PageHeader
        title="Bills of material"
        subtitle="Versioned. Only one ACTIVE BOM per product — approving a new one archives the previous."
        actions={
          <>
            <select className="field-input w-64" value={productId ?? ''} onChange={(e) => setProductId(e.target.value ? Number(e.target.value) : null)} aria-label="Product">
              <option value="">Select a product…</option>
              {products.data?.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.code} — {p.name}
                </option>
              ))}
            </select>
            <button className="btn-primary" disabled={!productId} onClick={() => setOpen(true)}>
              New version
            </button>
          </>
        }
      />
      <ErrorBanner error={action.error ?? boms.error} />
      {!productId && <p className="text-ink-400">Choose a product to see its BOM versions.</p>}
      <div className="space-y-4">
        {boms.data?.map((b) => (
          <div key={b.id} className="panel p-4">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <span className="font-mono text-sm">{b.bomNumber}</span>
                <span className="text-xs text-ink-400">v{b.version}</span>
                <StatusBadge value={b.status} />
              </div>
              <div className="flex gap-2">
                {b.status === 'DRAFT' && (
                  <button className="btn-secondary" disabled={action.pending} onClick={() => action.run(() => bomsApi.submit(b.id))}>
                    Submit
                  </button>
                )}
                {b.status === 'PENDING_APPROVAL' && (
                  <button className="btn-primary" disabled={action.pending} onClick={() => action.run(() => bomsApi.approve(b.id))}>
                    Approve
                  </button>
                )}
                {b.status !== 'ARCHIVED' && (
                  <button className="btn-ghost" disabled={action.pending} onClick={() => action.run(() => bomsApi.archive(b.id))}>
                    Archive
                  </button>
                )}
              </div>
            </div>
            <DataTable
              rows={b.components}
              rowKey={(c) => c.id}
              columns={[
                { header: 'Material', cell: (c) => materialName(c.materialId) },
                { header: 'Qty per unit', cell: (c) => c.quantity, className: 'text-right tabular-nums' },
                { header: 'Waste %', cell: (c) => c.wastePercent, className: 'text-right tabular-nums' },
              ]}
            />
          </div>
        ))}
      </div>

      <Modal title="New BOM version" open={open} onClose={() => setOpen(false)}>
        {components.map((c, i) => (
          <div key={i} className="grid grid-cols-[1fr_5rem_5rem] gap-2 mb-2">
            <select
              className="field-input"
              value={c.materialId}
              onChange={(e) => setComponents(components.map((x, j) => (j === i ? { ...x, materialId: Number(e.target.value) } : x)))}
              aria-label="Material"
            >
              <option value={0}>Material…</option>
              {materials.data?.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.name}
                </option>
              ))}
            </select>
            <input
              className="field-input"
              inputMode="decimal"
              aria-label="Quantity"
              value={c.quantity}
              onChange={(e) => setComponents(components.map((x, j) => (j === i ? { ...x, quantity: Number(e.target.value) } : x)))}
            />
            <input
              className="field-input"
              inputMode="decimal"
              aria-label="Waste percent"
              value={c.wastePercent}
              onChange={(e) => setComponents(components.map((x, j) => (j === i ? { ...x, wastePercent: Number(e.target.value) } : x)))}
            />
          </div>
        ))}
        <button className="btn-ghost" onClick={() => setComponents([...components, { materialId: 0, quantity: 1, wastePercent: 0 }])}>
          + Add component
        </button>
        <div className="flex justify-end gap-2 mt-4">
          <button className="btn-secondary" onClick={() => setOpen(false)}>
            Cancel
          </button>
          <button className="btn-primary" disabled={action.pending || components.every((c) => c.materialId === 0)} onClick={create}>
            Create draft
          </button>
        </div>
      </Modal>
    </>
  )
}
