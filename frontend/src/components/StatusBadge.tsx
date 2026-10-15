const TONES: Record<string, string> = {
  // positive
  ACTIVE: 'ok', APPROVED: 'ok', COMPLETED: 'ok', RECEIVED: 'ok', PASSED: 'ok', AVAILABLE: 'ok', ACCEPTED: 'ok',
  RESOLVED: 'ok', CLOSED: 'ok', RELEASED: 'ok',
  // in motion
  IN_PROGRESS: 'info', RUNNING: 'info', IN_USE: 'info', READY: 'info', SCHEDULED: 'info', MATERIALS_RESERVED: 'info',
  SUBMITTED: 'info', PARTIALLY_RECEIVED: 'info', REQUESTED: 'info', PARTIALLY_PASSED: 'warn',
  // waiting / caution
  DRAFT: 'muted', PLANNED: 'muted', PENDING_APPROVAL: 'warn', PENDING_INSPECTION: 'warn', PAUSED: 'warn',
  ON_HOLD: 'warn', QUARANTINED: 'warn', MAINTENANCE: 'warn', OPEN: 'warn', UNDER_REVIEW: 'warn',
  CORRECTIVE_ACTION: 'warn', PENDING: 'warn', MEDIUM: 'warn',
  // negative
  CANCELLED: 'bad', FAILED: 'bad', BROKEN: 'bad', REJECTED: 'bad', CRITICAL: 'bad', HIGH: 'bad', INACTIVE: 'muted',
  ARCHIVED: 'muted', LOW: 'muted',
}

const TONE_CLASSES: Record<string, string> = {
  ok: 'bg-brand-600/20 text-brand-300 border-brand-600/40',
  info: 'bg-sky-500/15 text-sky-300 border-sky-500/30',
  warn: 'bg-amber-500/15 text-amber-500 border-amber-500/30',
  bad: 'bg-rust-500/15 text-rust-500 border-rust-500/30',
  muted: 'bg-ink-700 text-ink-300 border-ink-600',
}

export function StatusBadge({ value }: { value: string }) {
  const tone = TONES[value] ?? 'muted'
  return (
    <span
      className={`inline-flex items-center rounded border px-1.5 py-0.5 text-[11px] font-medium uppercase tracking-wide ${TONE_CLASSES[tone]}`}
    >
      {value.replace(/_/g, ' ')}
    </span>
  )
}
