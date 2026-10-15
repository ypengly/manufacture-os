import { useState } from 'react'
import { machinesApi, machineTasksApi, maintenanceApi } from '../../api/machines'
import { useAuth } from '../../auth/AuthContext'
import { DataTable } from '../../components/DataTable'
import { ErrorBanner } from '../../components/ErrorBanner'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useAction, useAsync } from '../../hooks/useAsync'
import type { Machine } from '../../types/domain'

export function MachinesPage() {
  const { hasRole } = useAuth()
  const machines = useAsync(() => machinesApi.list())
  const [selected, setSelected] = useState<Machine | null>(null)
  const tasks = useAsync(() => (selected ? machineTasksApi.listForMachine(selected.id) : Promise.resolve([])), [selected?.id])
  const maint = useAsync(() => (selected ? maintenanceApi.listForMachine(selected.id) : Promise.resolve([])), [selected?.id])
  const reloadAll = () => {
    machines.reload()
    tasks.reload()
    maint.reload()
  }
  const action = useAction(reloadAll)
  const canManage = hasRole('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')

  const [mOpen, setMOpen] = useState(false)
  const [m, setM] = useState({ maintenanceType: 'PREVENTIVE', description: '', scheduledStart: '', scheduledEnd: '', technician: '' })

  async function scheduleMaintenance() {
    if (!selected) return
    await action.run(() =>
      maintenanceApi.schedule({
        machineId: selected.id,
        maintenanceType: m.maintenanceType,
        description: m.description || undefined,
        scheduledStart: new Date(m.scheduledStart).toISOString(),
        scheduledEnd: new Date(m.scheduledEnd).toISOString(),
        technician: m.technician || undefined,
      }),
    )
    setMOpen(false)
  }

  return (
    <>
      <PageHeader title="Machines" subtitle="Status, schedule and maintenance. Overlapping tasks are rejected by the server." />
      <ErrorBanner error={action.error ?? machines.error} />
      <DataTable
        rows={machines.data ?? []}
        rowKey={(r) => r.id}
        onRowClick={setSelected}
        columns={[
          { header: 'Code', cell: (r) => <span className="font-mono text-xs">{r.code}</span> },
          { header: 'Name', cell: (r) => r.name },
          { header: 'Type', cell: (r) => r.machineType },
          { header: 'Status', cell: (r) => <StatusBadge value={r.status} /> },
          { header: 'Capacity/h', cell: (r) => r.capacityPerHour, className: 'text-right tabular-nums' },
          { header: 'Skill', cell: (r) => r.requiredSkill ?? '—' },
          { header: 'Next maintenance', cell: (r) => r.nextMaintenanceDate ?? '—' },
        ]}
      />

      {selected && (
        <div className="mt-6 space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-semibold">
              {selected.code} · {selected.name}
            </h2>
            {canManage && (
              <button className="btn-secondary" onClick={() => setMOpen(true)}>
                Schedule maintenance
              </button>
            )}
          </div>

          <section>
            <h3 className="text-sm font-medium mb-2">Tasks</h3>
            <DataTable
              rows={tasks.data ?? []}
              rowKey={(t) => t.id}
              columns={[
                { header: 'Order', cell: (t) => `#${t.productionOrderId}` },
                { header: 'Window', cell: (t) => `${new Date(t.startTime).toLocaleString()} → ${new Date(t.endTime).toLocaleString()}` },
                { header: 'Status', cell: (t) => <StatusBadge value={t.status} /> },
                {
                  header: '',
                  cell: (t) => (
                    <div className="flex gap-1 justify-end">
                      {['SCHEDULED', 'READY'].includes(t.status) && (
                        <button className="btn-primary" onClick={() => action.run(() => machineTasksApi.start(t.id))}>
                          Start
                        </button>
                      )}
                      {t.status === 'RUNNING' && (
                        <>
                          <button className="btn-secondary" onClick={() => action.run(() => machineTasksApi.pause(t.id))}>
                            Pause
                          </button>
                          <button className="btn-primary" onClick={() => action.run(() => machineTasksApi.complete(t.id))}>
                            Complete
                          </button>
                        </>
                      )}
                      {t.status === 'PAUSED' && (
                        <button className="btn-primary" onClick={() => action.run(() => machineTasksApi.resume(t.id))}>
                          Resume
                        </button>
                      )}
                      {canManage && ['SCHEDULED', 'READY', 'RUNNING', 'PAUSED'].includes(t.status) && (
                        <button className="btn-ghost" onClick={() => action.run(() => machineTasksApi.cancel(t.id))}>
                          Cancel
                        </button>
                      )}
                    </div>
                  ),
                },
              ]}
            />
          </section>

          <section>
            <h3 className="text-sm font-medium mb-2">Maintenance</h3>
            <DataTable
              rows={maint.data ?? []}
              rowKey={(t) => t.id}
              columns={[
                { header: 'Type', cell: (t) => t.maintenanceType },
                { header: 'Window', cell: (t) => `${new Date(t.scheduledStart).toLocaleString()} → ${new Date(t.scheduledEnd).toLocaleString()}` },
                { header: 'Status', cell: (t) => <StatusBadge value={t.status} /> },
                {
                  header: '',
                  cell: (t) =>
                    canManage ? (
                      <div className="flex gap-1 justify-end">
                        {t.status === 'SCHEDULED' && (
                          <button className="btn-primary" onClick={() => action.run(() => maintenanceApi.start(t.id))}>
                            Start
                          </button>
                        )}
                        {t.status === 'IN_PROGRESS' && (
                          <button className="btn-primary" onClick={() => action.run(() => maintenanceApi.complete(t.id))}>
                            Complete
                          </button>
                        )}
                        {['SCHEDULED', 'IN_PROGRESS'].includes(t.status) && (
                          <button className="btn-ghost" onClick={() => action.run(() => maintenanceApi.cancel(t.id))}>
                            Cancel
                          </button>
                        )}
                      </div>
                    ) : null,
                },
              ]}
            />
          </section>
        </div>
      )}

      <Modal title="Schedule maintenance" open={mOpen} onClose={() => setMOpen(false)}>
        <label className="block mb-3">
          <span className="field-label">Type</span>
          <select className="field-input" value={m.maintenanceType} onChange={(e) => setM({ ...m, maintenanceType: e.target.value })}>
            {['PREVENTIVE', 'CORRECTIVE', 'EMERGENCY', 'INSPECTION'].map((t) => (
              <option key={t}>{t}</option>
            ))}
          </select>
        </label>
        <div className="grid grid-cols-2 gap-3 mb-3">
          <label>
            <span className="field-label">Start</span>
            <input type="datetime-local" className="field-input" value={m.scheduledStart} onChange={(e) => setM({ ...m, scheduledStart: e.target.value })} />
          </label>
          <label>
            <span className="field-label">End</span>
            <input type="datetime-local" className="field-input" value={m.scheduledEnd} onChange={(e) => setM({ ...m, scheduledEnd: e.target.value })} />
          </label>
        </div>
        <label className="block mb-3">
          <span className="field-label">Technician</span>
          <input className="field-input" value={m.technician} onChange={(e) => setM({ ...m, technician: e.target.value })} />
        </label>
        <label className="block mb-3">
          <span className="field-label">Description</span>
          <textarea className="field-input" rows={2} value={m.description} onChange={(e) => setM({ ...m, description: e.target.value })} />
        </label>
        <div className="flex justify-end gap-2">
          <button className="btn-secondary" onClick={() => setMOpen(false)}>
            Cancel
          </button>
          <button className="btn-primary" disabled={action.pending || !m.scheduledStart || !m.scheduledEnd} onClick={scheduleMaintenance}>
            Schedule
          </button>
        </div>
      </Modal>
    </>
  )
}
