import { useState } from 'react'
import { workerAssignmentsApi, workerSkillsApi, workersApi } from '../../api/workers'
import { useAuth } from '../../auth/AuthContext'
import { DataTable } from '../../components/DataTable'
import { ErrorBanner } from '../../components/ErrorBanner'
import { Modal } from '../../components/Modal'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { useAction, useAsync } from '../../hooks/useAsync'
import type { Worker, WorkerAssignment } from '../../types/domain'

export function WorkersPage() {
  const { hasRole } = useAuth()
  const canManage = hasRole('ADMIN', 'PRODUCTION_MANAGER', 'SUPERVISOR')
  const workers = useAsync(() => workersApi.list())
  const [selected, setSelected] = useState<Worker | null>(null)
  const skills = useAsync(() => (selected ? workerSkillsApi.listForWorker(selected.id) : Promise.resolve([])), [selected?.id])
  const assignments = useAsync(() => (selected ? workerAssignmentsApi.listForWorker(selected.id) : Promise.resolve([])), [selected?.id])
  const action = useAction(() => {
    skills.reload()
    assignments.reload()
  })
  const [skill, setSkill] = useState('')
  const [reassigning, setReassigning] = useState<WorkerAssignment | null>(null)
  const [newWorkerId, setNewWorkerId] = useState('')
  const [reason, setReason] = useState('')
  const workerName = (id: number) => workers.data?.find((w) => w.id === id)?.fullName ?? `#${id}`

  async function reassign() {
    if (!reassigning) return
    await action.run(() => workerAssignmentsApi.reassign(reassigning.id, Number(newWorkerId), reason))
    setReassigning(null)
    setReason('')
    setNewWorkerId('')
  }

  return (
    <>
      <PageHeader title="Workers" subtitle="Skills and assignments. Double-booking and skill mismatches are rejected by the server." />
      <ErrorBanner error={action.error ?? workers.error} />
      <DataTable
        rows={workers.data ?? []}
        rowKey={(w) => w.id}
        onRowClick={setSelected}
        columns={[
          { header: 'Employee', cell: (w) => <span className="font-mono text-xs">{w.employeeId}</span> },
          { header: 'Name', cell: (w) => w.fullName },
          { header: 'Department', cell: (w) => w.department ?? '—' },
          { header: 'Title', cell: (w) => w.jobTitle ?? '—' },
          { header: 'Skill level', cell: (w) => w.skillLevel ?? '—' },
          { header: 'Status', cell: (w) => <StatusBadge value={w.employmentStatus} /> },
        ]}
      />
      {selected && (
        <div className="mt-6 space-y-6">
          <h2 className="text-base font-semibold">{selected.fullName}</h2>
          <section>
            <h3 className="text-sm font-medium mb-2">Skills</h3>
            <div className="flex flex-wrap gap-2 mb-2">
              {skills.data?.map((s) => (
                <span key={s.id} className="panel px-2 py-1 text-xs flex items-center gap-2">
                  {s.skill}
                  {canManage && (
                    <button aria-label={`Remove ${s.skill}`} className="text-ink-400 hover:text-rust-500" onClick={() => action.run(() => workerSkillsApi.remove(s.id))}>
                      ×
                    </button>
                  )}
                </span>
              ))}
              {skills.data?.length === 0 && <span className="text-ink-400 text-sm">No skills recorded.</span>}
            </div>
            {canManage && (
              <div className="flex gap-2 max-w-sm">
                <input className="field-input" placeholder="Add skill" value={skill} onChange={(e) => setSkill(e.target.value)} />
                <button
                  className="btn-secondary"
                  disabled={!skill.trim() || action.pending}
                  onClick={async () => {
                    await action.run(() => workerSkillsApi.add(selected.id, skill.trim()))
                    setSkill('')
                  }}
                >
                  Add
                </button>
              </div>
            )}
          </section>
          <section>
            <h3 className="text-sm font-medium mb-2">Assignments</h3>
            <DataTable
              rows={assignments.data ?? []}
              rowKey={(a) => a.id}
              columns={[
                { header: 'Task / order', cell: (a) => (a.machineTaskId ? `Task #${a.machineTaskId}` : `Order #${a.productionOrderId}`) },
                { header: 'Window', cell: (a) => `${new Date(a.startTime).toLocaleString()} → ${new Date(a.endTime).toLocaleString()}` },
                { header: 'Status', cell: (a) => <StatusBadge value={a.status} /> },
                { header: 'Reason', cell: (a) => a.reason ?? '' },
                {
                  header: '',
                  cell: (a) =>
                    canManage && a.status === 'ACTIVE' ? (
                      <div className="flex gap-1 justify-end">
                        <button className="btn-secondary" onClick={() => setReassigning(a)}>
                          Reassign
                        </button>
                        <button className="btn-primary" onClick={() => action.run(() => workerAssignmentsApi.complete(a.id))}>
                          Complete
                        </button>
                      </div>
                    ) : null,
                },
              ]}
            />
          </section>
        </div>
      )}
      <Modal title="Reassign worker" open={reassigning !== null} onClose={() => setReassigning(null)}>
        <label className="block mb-3">
          <span className="field-label">New worker</span>
          <select className="field-input" value={newWorkerId} onChange={(e) => setNewWorkerId(e.target.value)}>
            <option value="">Select…</option>
            {workers.data
              ?.filter((w) => w.id !== reassigning?.workerId)
              .map((w) => (
                <option key={w.id} value={w.id}>
                  {w.fullName}
                </option>
              ))}
          </select>
        </label>
        <label className="block mb-3">
          <span className="field-label">Reason (required)</span>
          <textarea className="field-input" rows={2} value={reason} onChange={(e) => setReason(e.target.value)} />
        </label>
        <p className="text-xs text-ink-400 mb-3">From {reassigning ? workerName(reassigning.workerId) : ''}</p>
        <div className="flex justify-end gap-2">
          <button className="btn-secondary" onClick={() => setReassigning(null)}>
            Cancel
          </button>
          <button className="btn-primary" disabled={action.pending || !newWorkerId || !reason.trim()} onClick={reassign}>
            Reassign
          </button>
        </div>
      </Modal>
    </>
  )
}
