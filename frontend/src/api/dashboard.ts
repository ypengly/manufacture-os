import { api } from './client'

export type DashboardSummary = Record<string, number | string>

export const dashboardApi = {
  admin: () => api.get<DashboardSummary>('/dashboard/admin').then((r) => r.data),
  productionManager: () => api.get<DashboardSummary>('/dashboard/production-manager').then((r) => r.data),
  supervisor: () => api.get<DashboardSummary>('/dashboard/supervisor').then((r) => r.data),
  qualityInspector: () => api.get<DashboardSummary>('/dashboard/quality-inspector').then((r) => r.data),
  inventoryManager: () => api.get<DashboardSummary>('/dashboard/inventory-manager').then((r) => r.data),
  worker: () => api.get<DashboardSummary>('/dashboard/worker').then((r) => r.data),
}
