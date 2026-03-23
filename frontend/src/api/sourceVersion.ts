import http from './http'
import type { ApiResponse } from '@/types/api'
import type { SourceVersion, SourceVersionGitPayload, SourceVersionHtmlPayload } from '@/types/sourceVersion'

export function fetchSourceVersions(projectId: number) {
  return http.get<ApiResponse<SourceVersion[]>, ApiResponse<SourceVersion[]>>('/source-versions', {
    params: { projectId }
  })
}

export function uploadSourceZip(
  projectId: number,
  file: File,
  remark: string,
  onProgress?: (percent: number) => void
) {
  const formData = new FormData()
  formData.append('projectId', String(projectId))
  formData.append('remark', remark)
  formData.append('file', file)
  return http.post<ApiResponse<SourceVersion>, ApiResponse<SourceVersion>>('/source-versions/upload', formData, {
    timeout: 600000,
    onUploadProgress: (e) => {
      if (onProgress && e.total) {
        onProgress(Math.round((e.loaded * 100) / e.total))
      }
    }
  })
}

export function createHtmlSource(payload: SourceVersionHtmlPayload) {
  const formData = new FormData()
  formData.append('projectId', String(payload.projectId))
  formData.append('sourceName', payload.sourceName)
  formData.append('htmlContent', payload.htmlContent)
  formData.append('remark', payload.remark)
  return http.post<ApiResponse<SourceVersion>, ApiResponse<SourceVersion>>('/source-versions/html-create', formData)
}

export function importGitSource(payload: SourceVersionGitPayload) {
  const formData = new FormData()
  formData.append('projectId', String(payload.projectId))
  formData.append('gitUrl', payload.gitUrl)
  formData.append('gitBranch', payload.gitBranch)
  formData.append('commitHash', payload.commitHash)
  formData.append('remark', payload.remark)
  return http.post<ApiResponse<SourceVersion>, ApiResponse<SourceVersion>>('/source-versions/git-import', formData, {
    timeout: 180000
  })
}

export function downloadSourceVersion(id: number) {
  return http.get(`/source-versions/${id}/download`, {
    responseType: 'blob'
  })
}

export function deleteSourceVersion(id: number) {
  return http.delete<ApiResponse<null>, ApiResponse<null>>(`/source-versions/${id}`)
}
