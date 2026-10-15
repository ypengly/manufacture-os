import { api, clearTokens, getRefreshToken, setTokens } from './client'
import type { CurrentUser } from '../types/domain'

interface TokenPairResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresInSeconds: number
  user: CurrentUser
}

export async function login(username: string, password: string): Promise<CurrentUser> {
  const { data } = await api.post<TokenPairResponse>('/auth/login', { username, password })
  setTokens(data.accessToken, data.refreshToken)
  return data.user
}

export async function logout(): Promise<void> {
  const refreshToken = getRefreshToken()
  try {
    if (refreshToken) {
      await api.post('/auth/logout', { refreshToken })
    }
  } finally {
    clearTokens()
  }
}

export async function fetchCurrentUser(): Promise<CurrentUser> {
  const { data } = await api.get<CurrentUser>('/auth/me')
  return data
}
