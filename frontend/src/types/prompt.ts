export type PromptType = 'ui_style' | 'page_gen' | 'code_impl' | 'iteration_task'

export const PROMPT_TYPE_LABELS: Record<PromptType, string> = {
  ui_style: 'UI 风格',
  page_gen: '页面生成',
  code_impl: '代码实现',
  iteration_task: '迭代任务'
}

export interface PromptAsset {
  id: number
  projectId: number
  title: string
  promptType: PromptType
  content: string
  relatedSourceVersionId: number | null
  relatedReleaseId: number | null
  createdBy: number | null
  createdAt: string
  updatedAt: string
}

export interface PromptAssetCreatePayload {
  projectId: number
  title: string
  promptType: PromptType
  content: string
  relatedSourceVersionId: number | null
  relatedReleaseId: number | null
}

export interface PromptAssetUpdatePayload {
  title: string
  promptType: PromptType
  content: string
  relatedSourceVersionId: number | null
  relatedReleaseId: number | null
}
