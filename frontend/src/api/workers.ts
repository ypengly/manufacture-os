import { api } from './client'
import type { Worker, WorkerAssignment } from '../types/domain'

export const workersApi = {
  list: (q?: string) => api.get<Worker[]>('/workers', { params: { q } }).then((r) => r.data),
  get: (id: number) => api.get<Worker>(`/workers/${id}`).then((r) => r.data),
  create: (body: Partial<Worker>) => api.post<Worker>('/workers', body).then((r) => r.data),
  update: (id: number, body: Partial<Worker>) => api.put<Worker>(`/workers/${id}`, body).then((r) => r.data),
}

export const workerSkillsApi = {
  listForWorker: (workerId: number) =>
    api.get<{ id: number; workerId: number; skill: string }[]>('/worker-skills', { params: { workerId } }).then((r) => r.data),
  add: (workerId: number, skill: string) =>
    api.post('/worker-skills', null, { params: { workerId, skill } }).then((r) => r.data),
  remove: (id: number) => api.delete(`/worker-skills/${id}`),
}

export const workerAssignmentsApi = {
  listForWorker: (workerId: number) =>
    api.get<WorkerAssignment[]>('/worker-assignments', { params: { workerId } }).then((r) => r.data),
  listForTask: (machineTaskId: number) =>
    api.get<WorkerAssignment[]>('/worker-assignments', { params: { machineTaskId } }).then((r) => r.data),
  listForOrder: (productionOrderId: number) =>
    api.get<WorkerAssignment[]>('/worker-assignments', { params: { productionOrderId } }).then((r) => r.data),
  assign: (body: { workerId: number; machineTaskId?: number; productionOrderId?: number; startTime: string; endTime: string }) =>
    api.post<WorkerAssignment>('/worker-assignments', body).then((r) => r.data),
  reassign: (id: number, newWorkerId: number, reason: string, startTime?: string, endTime?: string) =>
    api.post<WorkerAssignment>(`/worker-assignments/${id}/reassign`, { newWorkerId, reason, startTime, endTime }).then((r) => r.data),
  complete: (id: number) => api.post<WorkerAssignment>(`/worker-assignments/${id}/complete`).then((r) => r.data),
  cancel: (id: number, reason?: string) =>
    api.post<WorkerAssignment>(`/worker-assignments/${id}/cancel`, { reason }).then((r) => r.data),
}
