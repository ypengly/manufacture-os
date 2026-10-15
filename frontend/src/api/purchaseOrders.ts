import { api } from './client'
import type { PurchaseOrder } from '../types/domain'

export const purchaseOrdersApi = {
  list: (params?: { status?: string; supplierId?: number }) =>
    api.get<PurchaseOrder[]>('/purchase-orders', { params }).then((r) => r.data),
  get: (id: number) => api.get<PurchaseOrder>(`/purchase-orders/${id}`).then((r) => r.data),
  create: (body: { supplierId: number; factoryId: number; expectedDate?: string; items: { materialId: number; quantity: number; unitCost?: number }[] }) =>
    api.post<PurchaseOrder>('/purchase-orders', body).then((r) => r.data),
  submit: (id: number) => api.post<PurchaseOrder>(`/purchase-orders/${id}/submit`).then((r) => r.data),
  approve: (id: number) => api.post<PurchaseOrder>(`/purchase-orders/${id}/approve`).then((r) => r.data),
  cancel: (id: number) => api.post<PurchaseOrder>(`/purchase-orders/${id}/cancel`).then((r) => r.data),
  receive: (id: number, lines: { itemId: number; quantity: number; locationId: number; batchNumber?: string; manufacturingDate?: string; expirationDate?: string }[]) =>
    api.post<PurchaseOrder>(`/purchase-orders/${id}/receive`, { lines }).then((r) => r.data),
}
