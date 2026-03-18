import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  cancelBuildTask,
  createBuildTask,
  fetchBuildLog,
  fetchBuildTasks,
  retryBuildTask
} from '@/api/buildTask'
import type { BuildTask, BuildTaskCreatePayload } from '@/types/buildTask'

export const useBuildStore = defineStore('build', () => {
  const tasks = ref<BuildTask[]>([])
  const loading = ref(false)
  const currentTask = ref<BuildTask | null>(null)

  async function loadTasks(projectId?: number) {
    loading.value = true
    try {
      const response = await fetchBuildTasks(projectId)
      tasks.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function createBuild(payload: BuildTaskCreatePayload) {
    const response = await createBuildTask(payload)
    return response.data
  }

  async function fetchLog(id: number): Promise<string> {
    const response = await fetchBuildLog(id)
    return response.data
  }

  async function cancelTask(id: number) {
    const response = await cancelBuildTask(id)
    return response.data
  }

  async function retryTask(id: number) {
    const response = await retryBuildTask(id)
    return response.data
  }

  return { tasks, loading, currentTask, loadTasks, createBuild, fetchLog, cancelTask, retryTask }
})
