import { useCallback, useEffect, useState } from 'react'

/** Runs an async loader on mount (and whenever deps change) and exposes data/loading/error plus a reload. */
export function useAsync<T>(loader: () => Promise<T>, deps: unknown[] = []) {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<unknown>(null)

  const reload = useCallback(() => {
    setLoading(true)
    setError(null)
    return loader()
      .then(setData)
      .catch(setError)
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  useEffect(() => {
    reload()
  }, [reload])

  return { data, loading, error, reload }
}

/** Wraps a mutating action: tracks pending/error, runs the callback, and reloads on success. */
export function useAction(onDone?: () => void) {
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)

  const run = useCallback(
    async (fn: () => Promise<unknown>) => {
      setPending(true)
      setError(null)
      try {
        await fn()
        onDone?.()
      } catch (e) {
        setError(e)
      } finally {
        setPending(false)
      }
    },
    [onDone],
  )

  return { run, pending, error }
}
