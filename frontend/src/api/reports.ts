import { api } from './client'

export type ReportKind = 'production' | 'production/output' | 'machines' | 'quality' | 'defects' | 'workers'

export interface ReportFilters {
  factoryId?: number
  lineId?: number
  from?: string
  to?: string
}

/** Fetches a CSV report through the authenticated client and triggers a browser download. */
export async function downloadReport(kind: ReportKind, filters: ReportFilters = {}) {
  const response = await api.get<Blob>(`/reports/${kind}`, { params: filters, responseType: 'blob' })
  const url = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = url
  link.download = `${kind.replace('/', '-')}.csv`
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}
