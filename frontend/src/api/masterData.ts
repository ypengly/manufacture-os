import { api } from './client'
import type { Factory, Material, Product, Supplier } from '../types/domain'

function crud<T>(path: string) {
  return {
    list: (q?: string) => api.get<T[]>(path, { params: { q } }).then((r) => r.data),
    get: (id: number) => api.get<T>(`${path}/${id}`).then((r) => r.data),
    create: (body: Partial<T>) => api.post<T>(path, body).then((r) => r.data),
    update: (id: number, body: Partial<T>) => api.put<T>(`${path}/${id}`, body).then((r) => r.data),
  }
}

export interface StorageLocation {
  id: number
  factoryId: number
  code: string
  name: string
  type: string | null
}

export interface ProductionLine {
  id: number
  factoryId: number
  code: string
  name: string
  active: boolean
}

export const productsApi = crud<Product>('/products')
export const materialsApi = crud<Material>('/materials')
export const suppliersApi = crud<Supplier>('/suppliers')
export const factoriesApi = crud<Factory>('/factories')
export const storageLocationsApi = crud<StorageLocation>('/storage-locations')
export const productionLinesApi = crud<ProductionLine>('/production-lines')
