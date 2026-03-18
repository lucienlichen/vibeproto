import http from './http'
import type { ApiResponse } from '@/types/api'
import type { SysUser, SysRole, UserCreatePayload, UserUpdatePayload } from '@/types/user'

export interface UserPage {
  total: number
  pageNum: number
  pageSize: number
  records: SysUser[]
}

export function fetchUsers(pageNum: number, pageSize: number) {
  return http.get<ApiResponse<UserPage>, ApiResponse<UserPage>>('/users', {
    params: { pageNum, pageSize }
  })
}

export function createUser(data: UserCreatePayload) {
  return http.post<ApiResponse<SysUser>, ApiResponse<SysUser>>('/users', data)
}

export function updateUser(id: number, data: UserUpdatePayload) {
  return http.put<ApiResponse<SysUser>, ApiResponse<SysUser>>(`/users/${id}`, data)
}

export function deleteUser(id: number) {
  return http.delete<ApiResponse<null>, ApiResponse<null>>(`/users/${id}`)
}

export function changePassword(id: number, newPassword: string) {
  return http.put<ApiResponse<null>, ApiResponse<null>>(`/users/${id}/password`, { newPassword })
}

export function fetchRoles() {
  return http.get<ApiResponse<SysRole[]>, ApiResponse<SysRole[]>>('/roles')
}
