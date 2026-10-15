import { api } from './client'
import type { QualityDefect, QualityInspection, ReworkOrder, ScrapRecord, Severity } from '../types/domain'

export const inspectionsApi = {
  listForOrder: (productionOrderId: number) =>
    api.get<QualityInspection[]>('/quality/inspections', { params: { productionOrderId } }).then((r) => r.data),
  record: (body: { outputId: number; sampleSize?: number; passedQuantity: number; failedQuantity: number; measurements?: string; notes?: string }) =>
    api.post<QualityInspection>('/quality/inspections', body).then((r) => r.data),
  finalize: (id: number) => api.post<QualityInspection>(`/quality/inspections/${id}/finalize`).then((r) => r.data),
}

export const defectsApi = {
  listForOrder: (productionOrderId: number) =>
    api.get<QualityDefect[]>('/quality/defects', { params: { productionOrderId } }).then((r) => r.data),
  listByStatus: (status: string) => api.get<QualityDefect[]>('/quality/defects', { params: { status } }).then((r) => r.data),
  report: (body: { defectType: string; description?: string; severity: Severity; productionOrderId?: number; productId?: number; batchId?: number }) =>
    api.post<QualityDefect>('/quality/defects', body).then((r) => r.data),
  updateStatus: (id: number, status: string, correctiveAction?: string) =>
    api.post<QualityDefect>(`/quality/defects/${id}/status`, { status, correctiveAction }).then((r) => r.data),
}

export const reworkApi = {
  listForOrder: (productionOrderId: number) =>
    api.get<ReworkOrder[]>('/quality/rework', { params: { productionOrderId } }).then((r) => r.data),
  request: (body: { outputId: number; quantity: number; assignedWorkerId?: number; machineId?: number; notes?: string; additionalMaterials?: string }) =>
    api.post<ReworkOrder>('/quality/rework', body).then((r) => r.data),
  start: (id: number) => api.post<ReworkOrder>(`/quality/rework/${id}/start`).then((r) => r.data),
  complete: (id: number, passedQuantity: number, outcome?: string) =>
    api.post<ReworkOrder>(`/quality/rework/${id}/complete`, { passedQuantity, outcome }).then((r) => r.data),
  cancel: (id: number) => api.post<ReworkOrder>(`/quality/rework/${id}/cancel`).then((r) => r.data),
}

export const scrapApi = {
  listForOrder: (productionOrderId: number) =>
    api.get<ScrapRecord[]>('/quality/scrap', { params: { productionOrderId } }).then((r) => r.data),
  scrap: (body: { outputId?: number; inventoryId?: number; productionOrderId?: number; quantity: number; reason: string }) =>
    api.post<ScrapRecord>('/quality/scrap', body).then((r) => r.data),
}
