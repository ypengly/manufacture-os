import type { ReactNode } from 'react'

export function FormField({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return (
    <label className="block mb-3">
      <span className="field-label">{label}</span>
      {children}
      {error && <span className="block mt-1 text-xs text-rust-500">{error}</span>}
    </label>
  )
}
