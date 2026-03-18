import http from './http'
import type { ApiResponse } from '@/types/api'
import type { BuildProfile, BuildProfileCreatePayload, BuildProfileUpdatePayload } from '@/types/buildProfile'

export function fetchBuildProfiles(projectId: number) {
  return http.get<ApiResponse<BuildProfile[]>, ApiResponse<BuildProfile[]>>('/build-profiles', {
    params: { projectId }
  })
}

export function createBuildProfile(payload: BuildProfileCreatePayload) {
  return http.post<ApiResponse<BuildProfile>, ApiResponse<BuildProfile>>('/build-profiles', payload)
}

export function updateBuildProfile(id: number, payload: BuildProfileUpdatePayload) {
  return http.put<ApiResponse<BuildProfile>, ApiResponse<BuildProfile>>(`/build-profiles/${id}`, payload)
}

export function deleteBuildProfile(id: number) {
  return http.delete<ApiResponse<null>, ApiResponse<null>>(`/build-profiles/${id}`)
}
