import http from './http'
import type { ApiResponse } from '@/types/api'
import type { SystemConfig, SystemConfigUpdatePayload } from '@/types/system'

export function fetchSystemConfigs() {
  return http.get<ApiResponse<SystemConfig[]>, ApiResponse<SystemConfig[]>>('/system/configs')
}

export function updateSystemConfigs(payload: SystemConfigUpdatePayload) {
  return http.put<ApiResponse<SystemConfig[]>, ApiResponse<SystemConfig[]>>('/system/configs', payload)
}
