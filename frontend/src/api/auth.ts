import http from './http'
import type { ApiResponse } from '@/types/api'
import type { UserInfo } from '@/types/user'

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  userInfo: UserInfo
}

export function login(payload: LoginPayload) {
  return http.post<ApiResponse<LoginResult>, ApiResponse<LoginResult>>('/auth/login', payload)
}

export function fetchCurrentUser() {
  return http.get<ApiResponse<UserInfo>, ApiResponse<UserInfo>>('/auth/me')
}
