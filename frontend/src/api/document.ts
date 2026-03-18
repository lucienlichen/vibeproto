import http from './http'
import type { ApiResponse } from '@/types/api'
import type { ProjectDocument, ProjectDocumentCreatePayload, ProjectDocumentUpdatePayload } from '@/types/document'

export function fetchDocuments(projectId: number, relatedReleaseId?: number) {
  return http.get<ApiResponse<ProjectDocument[]>, ApiResponse<ProjectDocument[]>>('/documents', {
    params: { projectId, relatedReleaseId }
  })
}

export function createDocument(payload: ProjectDocumentCreatePayload) {
  return http.post<ApiResponse<ProjectDocument>, ApiResponse<ProjectDocument>>('/documents', payload)
}

export function updateDocument(id: number, payload: ProjectDocumentUpdatePayload) {
  return http.put<ApiResponse<ProjectDocument>, ApiResponse<ProjectDocument>>(`/documents/${id}`, payload)
}

export function deleteDocument(id: number) {
  return http.delete<ApiResponse<null>, ApiResponse<null>>(`/documents/${id}`)
}
