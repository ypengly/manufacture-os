import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate } from 'react-router-dom'
import { productionOrdersApi } from '../../api/productionOrders'
import { productsApi } from '../../api/masterData'
import { bomsApi } from '../../api/boms'
import { factoriesApi } from '../../api/masterData'
import { useAuth } from '../../auth/AuthContext'
import { DataTable } from '../../components/DataTable'
import { ErrorBanner } from '../../components/ErrorBanner'
import { FormField } from '../../components/FormField'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useAsync } from '../../hooks/useAsync'
import type { OrderStatus } from '../../types/domain'

const STATUSES: OrderStatus[] = [
  'DRAFT', 'PLANNED', 'PENDING_APPROVAL', 'APPROVED', 'MATERIALS_RESERVED', 'READY',
  'IN_PROGRESS', 'PAUSED', 'ON_HOLD', 'COMPLETED', 'CANCELLED',
]

const schema = z.object({
  productId: z.coerce.number().int().positive('Choose a product'),
  plannedQuantity: z.coerce.number().positive('Quantity must be greater than zero'),
  factoryId: z.coerce.number().int().positive('Choose a factory'),
  priority: z.enum(['LOW', 'NORMAL', 'HIGH', 'URGENT']),
  plannedStart: z.string().optional(),
  plannedEnd: z.string().optional(),
})
type FormValues = z.infer<typeof schema>

export function ProductionOrdersPage() {
  const navigate = useNavigate()
  const { hasRole } = useAuth()
  const [status, setStatus] = useState<string>('')
  const [open, setOpen] = useState(false)
  const [submitError, setSubmitError] = useState<unknown>(null)

  const orders = useAsync(() => productionOrdersApi.list(status ? { status } : undefined), [status])
  const products = useAsync(() => productsApi.list())
  const factories = useAsync(() => factoriesApi.list())

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { priority: 'NORMAL' } })

  async function onSubmit(v: FormValues) {
    setSubmitError(null)
    try {
      // The order is created against the product's currently ACTIVE BOM; the backend rejects it otherwise.
      const boms = await bomsApi.listForProduct(v.productId)
      const active = boms.find((b) => b.status === 'ACTIVE')
      if (!active) throw new Error('This product has no ACTIVE bill of materials yet.')
      const created = await productionOrdersApi.create({
        productId: v.productId,
        bomId: active.id,
        plannedQuantity: v.plannedQuantity,
        factoryId: v.factoryId,
        priority: v.priority,
        plannedStart: v.plannedStart ? new Date(v.plannedStart).toISOString() : null,
        plannedEnd: v.plannedEnd ? new Date(v.plannedEnd).toISOString() : null,
      })
      setOpen(false)
      reset()
      navigate(`/production-orders/${created.id}`)
    } catch (e) {
      setSubmitError(e)
    }
  }

  const productName = (id: number) => products.data?.find((p) => p.id === id)?.name ?? `#${id}`

  return (
    <>
      <PageHeader
        title="Production orders"
        subtitle="Plan, approve, reserve materials and execute"
        actions={
          <>
            <select className="field-input w-48" value={status} onChange={(e) => setStatus(e.target.value)} aria-label="Filter by status">
              <option value="">All statuses</option>
              {STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s.replace(/_/g, ' ')}
                </option>
              ))}
            </select>
            {hasRole('ADMIN', 'PRODUCTION_MANAGER') && (
              <button className="btn-primary" onClick={() => setOpen(true)}>
                New order
              </button>
            )}
          </>
        }
      />
      <ErrorBanner error={orders.error} />
      <DataTable
        rows={orders.data ?? []}
        rowKey={(o) => o.id}
        onRowClick={(o) => navigate(`/production-orders/${o.id}`)}
        columns={[
          { header: 'Order', cell: (o) => <span className="font-mono text-xs">{o.orderNumber}</span> },
          { header: 'Product', cell: (o) => productName(o.productId) },
          { header: 'Status', cell: (o) => <StatusBadge value={o.status} /> },
          { header: 'Priority', cell: (o) => <StatusBadge value={o.priority} /> },
          { header: 'Planned', cell: (o) => o.plannedQuantity, className: 'text-right tabular-nums' },
          { header: 'Completed', cell: (o) => o.completedQuantity, className: 'text-right tabular-nums' },
          { header: 'Rejected', cell: (o) => o.rejectedQuantity, className: 'text-right tabular-nums' },
          { header: 'Planned end', cell: (o) => (o.plannedEnd ? new Date(o.plannedEnd).toLocaleDateString() : '—') },
        ]}
      />

      <Modal title="New production order" open={open} onClose={() => setOpen(false)}>
        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <ErrorBanner error={submitError} />
          <FormField label="Product" error={errors.productId?.message}>
            <select className="field-input" {...register('productId')}>
              <option value="">Select…</option>
              {products.data?.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.code} — {p.name}
                </option>
              ))}
            </select>
          </FormField>
          <FormField label="Factory" error={errors.factoryId?.message}>
            <select className="field-input" {...register('factoryId')}>
              <option value="">Select…</option>
              {factories.data?.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.code} — {f.name}
                </option>
              ))}
            </select>
          </FormField>
          <FormField label="Planned quantity" error={errors.plannedQuantity?.message}>
            <input className="field-input" inputMode="decimal" {...register('plannedQuantity')} />
          </FormField>
          <FormField label="Priority">
            <select className="field-input" {...register('priority')}>
              {['LOW', 'NORMAL', 'HIGH', 'URGENT'].map((p) => (
                <option key={p}>{p}</option>
              ))}
            </select>
          </FormField>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Planned start">
              <input type="datetime-local" className="field-input" {...register('plannedStart')} />
            </FormField>
            <FormField label="Planned end">
              <input type="datetime-local" className="field-input" {...register('plannedEnd')} />
            </FormField>
          </div>
          <div className="flex justify-end gap-2 mt-2">
            <button type="button" className="btn-secondary" onClick={() => setOpen(false)}>
              Cancel
            </button>
            <button className="btn-primary" disabled={isSubmitting}>
              Create order
            </button>
          </div>
        </form>
      </Modal>
    </>
  )
}
