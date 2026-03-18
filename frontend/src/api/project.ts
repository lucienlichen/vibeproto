import http from './http'
import type { ApiResponse } from '@/types/api'
import type { Project, ProjectFormPayload, ProjectPage, ProjectPreviewInfo, ProjectUpdatePayload } from '@/types/project'

export interface ProjectQuery {
  pageNum: number
  pageSize: number
  name?: string
  projectType?: string
  status?: string
}

export function fetchProjects(params: ProjectQuery) {
  return http.get<ApiResponse<ProjectPage>, ApiResponse<ProjectPage>>('/projects', { params })
}

export function createProject(payload: ProjectFormPayload) {
  return http.post<ApiResponse<Project>, ApiResponse<Project>>('/projects', payload)
}

export function updateProject(id: number, payload: ProjectUpdatePayload) {
  return http.put<ApiResponse<Project>, ApiResponse<Project>>(`/projects/${id}`, payload)
}

export function deleteProject(id: number) {
  return http.delete<ApiResponse<null>, ApiResponse<null>>(`/projects/${id}`)
}

export function archiveProject(id: number) {
  return http.post<ApiResponse<Project>, ApiResponse<Project>>(`/projects/${id}/archive`)
}

export function fetchProject(id: number) {
  return http.get<ApiResponse<Project>, ApiResponse<Project>>(`/projects/${id}`)
}

export function fetchProjectPreviewInfo(id: number) {
  return http.get<ApiResponse<ProjectPreviewInfo>, ApiResponse<ProjectPreviewInfo>>(`/projects/${id}/preview-info`)
}
