import { api } from './client'
import type { InventoryCategory, InventoryTransaction, InventoryView, MaterialRequirementView, PageResponse } from '../types/domain'

export const inventoryApi = {
  list: (params?: { factoryId?: number; category?: InventoryCategory }) =>
    api.get<InventoryView[]>('/inventory', { params }).then((r) => r.data),
  transactions: (params?: { factoryId?: number; referenceType?: string; referenceId?: number; page?: number; size?: number }) =>
    api.get<PageResponse<InventoryTransaction>>('/inventory/transactions', { params }).then((r) => r.data),
  mrp: (params: { bomId: number; plannedQuantity: number; factoryId: number }) =>
    api.get<MaterialRequirementView[]>('/inventory/mrp', { params }).then((r) => r.data),
  receive: (body: {
    materialId: number
    factoryId: number
    locationId: number
    supplierId?: number
    purchaseOrderId?: number
    batchNumber?: string
    quantity: number
    unitCost?: number
    manufacturingDate?: string
    expirationDate?: string
  }) => api.post<InventoryView>('/inventory/receive', body).then((r) => r.data),
  adjust: (body: { inventoryId: number; deltaQuantity: number; reason: string }) =>
    api.post<InventoryView>('/inventory/adjustments', body).then((r) => r.data),
}
