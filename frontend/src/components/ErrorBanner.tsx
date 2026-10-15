import { ApiError } from '../api/client'

export function errorMessage(err: unknown): string {
  if (err instanceof ApiError) return err.message
  if (err instanceof Error) return err.message
  return 'Something went wrong.'
}

export function ErrorBanner({ error }: { error: unknown }) {
  if (!error) return null
  return (
    <div role="alert" className="mb-4 rounded-md border border-rust-500/40 bg-rust-500/10 px-3 py-2 text-sm text-rust-500">
      {errorMessage(error)}
    </div>
  )
}
