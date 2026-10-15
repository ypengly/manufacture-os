import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { FormField } from '../components/FormField'
import { ErrorBanner } from '../components/ErrorBanner'

const schema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
})
type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<unknown>(null)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  if (user) return <Navigate to="/" replace />

  async function onSubmit(values: FormValues) {
    setError(null)
    try {
      await login(values.username, values.password)
      navigate('/')
    } catch (e) {
      setError(e)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <form onSubmit={handleSubmit(onSubmit)} className="panel w-full max-w-sm p-6" noValidate>
        <div className="mb-5">
          <div className="text-lg font-semibold tracking-wide">
            Manufacture<span className="text-brand-400">OS</span>
          </div>
          <p className="text-sm text-ink-300">Sign in to continue</p>
        </div>
        <ErrorBanner error={error} />
        <FormField label="Username" error={errors.username?.message}>
          <input className="field-input" autoComplete="username" autoFocus {...register('username')} />
        </FormField>
        <FormField label="Password" error={errors.password?.message}>
          <input className="field-input" type="password" autoComplete="current-password" {...register('password')} />
        </FormField>
        <button className="btn-primary w-full mt-2" disabled={isSubmitting}>
          {isSubmitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
    </div>
  )
}
