import http from './http'
import type { ApiResponse } from '@/types/api'
import type { ReleaseVersion } from '@/types/release'

export function fetchReleases(projectId?: number) {
  return http.get<ApiResponse<ReleaseVersion[]>, ApiResponse<ReleaseVersion[]>>('/releases', {
    params: { projectId }
  })
}

export function fetchRelease(id: number) {
  return http.get<ApiResponse<ReleaseVersion>, ApiResponse<ReleaseVersion>>(`/releases/${id}`)
}

export function setCurrentRelease(id: number) {
  return http.post<ApiResponse<ReleaseVersion>, ApiResponse<ReleaseVersion>>(`/releases/${id}/set-current`)
}

export function rollbackRelease(id: number) {
  return http.post<ApiResponse<ReleaseVersion>, ApiResponse<ReleaseVersion>>(`/releases/${id}/rollback`)
}

export function deleteRelease(id: number) {
  return http.delete<ApiResponse<null>, ApiResponse<null>>(`/releases/${id}`)
}
