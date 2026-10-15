import { api } from './client'
import type { AppNotification, PageResponse } from '../types/domain'

export const notificationsApi = {
  mine: (page = 0, size = 20) =>
    api.get<PageResponse<AppNotification>>('/notifications/mine', { params: { page, size } }).then((r) => r.data),
  markRead: (id: number) => api.post<AppNotification>(`/notifications/${id}/read`).then((r) => r.data),
}
