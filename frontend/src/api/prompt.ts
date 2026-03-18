import http from './http'
import type { ApiResponse } from '@/types/api'
import type { PromptAsset, PromptAssetCreatePayload, PromptAssetUpdatePayload } from '@/types/prompt'

export function fetchPrompts(projectId: number) {
  return http.get<ApiResponse<PromptAsset[]>, ApiResponse<PromptAsset[]>>('/prompts', {
    params: { projectId }
  })
}

export function createPrompt(payload: PromptAssetCreatePayload) {
  return http.post<ApiResponse<PromptAsset>, ApiResponse<PromptAsset>>('/prompts', payload)
}

export function updatePrompt(id: number, payload: PromptAssetUpdatePayload) {
  return http.put<ApiResponse<PromptAsset>, ApiResponse<PromptAsset>>(`/prompts/${id}`, payload)
}

export function deletePrompt(id: number) {
  return http.delete<ApiResponse<null>, ApiResponse<null>>(`/prompts/${id}`)
}
