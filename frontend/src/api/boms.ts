import { api } from './client'
import type { BomComponentInput, BomDetail } from '../types/domain'

export const bomsApi = {
  get: (id: number) => api.get<BomDetail>(`/boms/${id}`).then((r) => r.data),
  listForProduct: (productId: number) => api.get<BomDetail[]>('/boms', { params: { productId } }).then((r) => r.data),
  create: (body: { productId: number; effectiveFrom?: string; effectiveTo?: string; components: BomComponentInput[] }) =>
    api.post<BomDetail>('/boms', body).then((r) => r.data),
  submit: (id: number) => api.post<BomDetail>(`/boms/${id}/submit`).then((r) => r.data),
  approve: (id: number) => api.post<BomDetail>(`/boms/${id}/approve`).then((r) => r.data),
  archive: (id: number) => api.post<BomDetail>(`/boms/${id}/archive`).then((r) => r.data),
}
