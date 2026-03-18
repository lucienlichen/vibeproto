import http from './http'
import type { ApiResponse } from '@/types/api'
import type { BuildTask, BuildTaskCreatePayload } from '@/types/buildTask'

export function createBuildTask(payload: BuildTaskCreatePayload) {
  return http.post<ApiResponse<BuildTask>, ApiResponse<BuildTask>>('/build-tasks', payload)
}

export function fetchBuildTasks(projectId?: number) {
  return http.get<ApiResponse<BuildTask[]>, ApiResponse<BuildTask[]>>('/build-tasks', {
    params: { projectId }
  })
}

export function fetchBuildTask(id: number) {
  return http.get<ApiResponse<BuildTask>, ApiResponse<BuildTask>>(`/build-tasks/${id}`)
}

export function fetchBuildLog(id: number) {
  return http.get<ApiResponse<string>, ApiResponse<string>>(`/build-tasks/${id}/log`)
}

export function cancelBuildTask(id: number) {
  return http.post<ApiResponse<BuildTask>, ApiResponse<BuildTask>>(`/build-tasks/${id}/cancel`)
}

export function retryBuildTask(id: number) {
  return http.post<ApiResponse<BuildTask>, ApiResponse<BuildTask>>(`/build-tasks/${id}/retry`)
}
