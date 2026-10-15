import { api } from './client'
import type { Machine, MachineMaintenance, MachineTask } from '../types/domain'

export const machinesApi = {
  list: (q?: string) => api.get<Machine[]>('/machines', { params: { q } }).then((r) => r.data),
  get: (id: number) => api.get<Machine>(`/machines/${id}`).then((r) => r.data),
  create: (body: Partial<Machine>) => api.post<Machine>('/machines', body).then((r) => r.data),
  update: (id: number, body: Partial<Machine>) => api.put<Machine>(`/machines/${id}`, body).then((r) => r.data),
}

export const machineTasksApi = {
  listForMachine: (machineId: number) => api.get<MachineTask[]>('/machine-tasks', { params: { machineId } }).then((r) => r.data),
  listForOrder: (productionOrderId: number) =>
    api.get<MachineTask[]>('/machine-tasks', { params: { productionOrderId } }).then((r) => r.data),
  schedule: (body: { machineId: number; productionOrderId: number; startTime: string; endTime: string }) =>
    api.post<MachineTask>('/machine-tasks', body).then((r) => r.data),
  reschedule: (id: number, startTime: string, endTime: string) =>
    api.post<MachineTask>(`/machine-tasks/${id}/reschedule`, { startTime, endTime }).then((r) => r.data),
  ready: (id: number) => api.post<MachineTask>(`/machine-tasks/${id}/ready`).then((r) => r.data),
  start: (id: number) => api.post<MachineTask>(`/machine-tasks/${id}/start`).then((r) => r.data),
  pause: (id: number) => api.post<MachineTask>(`/machine-tasks/${id}/pause`).then((r) => r.data),
  resume: (id: number) => api.post<MachineTask>(`/machine-tasks/${id}/resume`).then((r) => r.data),
  complete: (id: number) => api.post<MachineTask>(`/machine-tasks/${id}/complete`).then((r) => r.data),
  cancel: (id: number) => api.post<MachineTask>(`/machine-tasks/${id}/cancel`).then((r) => r.data),
}

export const maintenanceApi = {
  listForMachine: (machineId: number) =>
    api.get<MachineMaintenance[]>('/maintenance', { params: { machineId } }).then((r) => r.data),
  schedule: (body: {
    machineId: number
    maintenanceType: string
    description?: string
    scheduledStart: string
    scheduledEnd: string
    technician?: string
  }) => api.post<MachineMaintenance>('/maintenance', body).then((r) => r.data),
  start: (id: number) => api.post<MachineMaintenance>(`/maintenance/${id}/start`).then((r) => r.data),
  complete: (id: number, notes?: string, cost?: number) =>
    api.post<MachineMaintenance>(`/maintenance/${id}/complete`, { notes, cost }).then((r) => r.data),
  cancel: (id: number) => api.post<MachineMaintenance>(`/maintenance/${id}/cancel`).then((r) => r.data),
}
