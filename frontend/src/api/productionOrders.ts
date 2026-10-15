import { api } from './client'
import type { MaterialRequirementView, ProductionOrder } from '../types/domain'

export interface ProductionOrderCreateInput {
  productId: number
  bomId: number
  plannedQuantity: number
  factoryId: number
  lineId?: number | null
  priority?: string
  plannedStart?: string | null
  plannedEnd?: string | null
}

export const productionOrdersApi = {
  list: (params?: { status?: string; factoryId?: number; lineId?: number }) =>
    api.get<ProductionOrder[]>('/production-orders', { params }).then((r) => r.data),
  get: (id: number) => api.get<ProductionOrder>(`/production-orders/${id}`).then((r) => r.data),
  materialCheck: (id: number) =>
    api.get<MaterialRequirementView[]>(`/production-orders/${id}/material-check`).then((r) => r.data),
  create: (body: ProductionOrderCreateInput) =>
    api.post<ProductionOrder>('/production-orders', body).then((r) => r.data),
  plan: (id: number) => api.post<ProductionOrder>(`/production-orders/${id}/plan`).then((r) => r.data),
  submit: (id: number) => api.post<ProductionOrder>(`/production-orders/${id}/submit`).then((r) => r.data),
  approve: (id: number) => api.post<ProductionOrder>(`/production-orders/${id}/approve`).then((r) => r.data),
  reserveMaterials: (id: number, allowPartial: boolean) =>
    api
      .post<ProductionOrder>(`/production-orders/${id}/reserve-materials`, null, { params: { allowPartial } })
      .then((r) => r.data),
  ready: (id: number) => api.post<ProductionOrder>(`/production-orders/${id}/ready`).then((r) => r.data),
  start: (id: number) => api.post<ProductionOrder>(`/production-orders/${id}/start`).then((r) => r.data),
  pause: (id: number) => api.post<ProductionOrder>(`/production-orders/${id}/pause`).then((r) => r.data),
  resume: (id: number) => api.post<ProductionOrder>(`/production-orders/${id}/resume`).then((r) => r.data),
  hold: (id: number, reason: string) =>
    api.post<ProductionOrder>(`/production-orders/${id}/hold`, { reason }).then((r) => r.data),
  resumeFromHold: (id: number) => api.post<ProductionOrder>(`/production-orders/${id}/resume-from-hold`).then((r) => r.data),
  cancel: (id: number, reason: string) =>
    api.post<ProductionOrder>(`/production-orders/${id}/cancel`, { reason }).then((r) => r.data),
  reschedule: (id: number, plannedStart: string, plannedEnd: string, overrideReason?: string) =>
    api
      .post<ProductionOrder>(`/production-orders/${id}/reschedule`, { plannedStart, plannedEnd, overrideReason })
      .then((r) => r.data),
  report: (id: number, body: { completedQuantity: number; rejectedQuantity: number; downtimeMinutes?: number; notes?: string; locationId?: number; idempotencyKey: string }) =>
    api.post<ProductionOrder>(`/production-orders/${id}/report`, body).then((r) => r.data),
  complete: (id: number) => api.post<ProductionOrder>(`/production-orders/${id}/complete`).then((r) => r.data),
}
