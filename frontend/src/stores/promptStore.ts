import { defineStore } from 'pinia'
import { ref } from 'vue'
import { createPrompt, deletePrompt, fetchPrompts, updatePrompt } from '@/api/prompt'
import type { PromptAsset, PromptAssetCreatePayload, PromptAssetUpdatePayload } from '@/types/prompt'

export const usePromptStore = defineStore('prompt', () => {
  const prompts = ref<PromptAsset[]>([])
  const loading = ref(false)

  async function load(projectId: number) {
    loading.value = true
    try {
      const response = await fetchPrompts(projectId)
      prompts.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function create(payload: PromptAssetCreatePayload) {
    const response = await createPrompt(payload)
    return response.data
  }

  async function update(id: number, payload: PromptAssetUpdatePayload) {
    const response = await updatePrompt(id, payload)
    return response.data
  }

  async function remove(id: number) {
    await deletePrompt(id)
  }

  return { prompts, loading, load, create, update, remove }
})
