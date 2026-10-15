import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'

const ACCESS_TOKEN_KEY = 'mos_access_token'
const REFRESH_TOKEN_KEY = 'mos_refresh_token'

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}
export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}
export function setTokens(accessToken: string, refreshToken: string) {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}
export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

/** Shape the backend's GlobalExceptionHandler always returns on an error response. */
export interface ApiErrorBody {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}

export class ApiError extends Error {
  status: number
  code: string
  constructor(body: ApiErrorBody) {
    super(body.message)
    this.status = body.status
    this.code = body.error
  }
}

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
})

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let refreshPromise: Promise<string> | null = null

async function performRefresh(): Promise<string> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) throw new Error('No refresh token available')
  const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL ?? '/api'}/auth/refresh`, {
    refreshToken,
  })
  const { accessToken, refreshToken: newRefreshToken } = response.data
  setTokens(accessToken, newRefreshToken)
  return accessToken
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiErrorBody>) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retried?: boolean }) | undefined

    if (error.response?.status === 401 && original && !original._retried && getRefreshToken()) {
      original._retried = true
      try {
        refreshPromise ??= performRefresh().finally(() => {
          refreshPromise = null
        })
        const newAccessToken = await refreshPromise
        original.headers = original.headers ?? {}
        original.headers.Authorization = `Bearer ${newAccessToken}`
        return api.request(original)
      } catch {
        clearTokens()
        window.location.href = '/login'
        return Promise.reject(error)
      }
    }

    if (error.response?.data?.message) {
      return Promise.reject(new ApiError(error.response.data))
    }
    return Promise.reject(error)
  },
)
