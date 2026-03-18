import { defineStore } from 'pinia'
import { ref } from 'vue'
import { deleteRelease, fetchReleases, rollbackRelease, setCurrentRelease } from '@/api/release'
import type { ReleaseVersion } from '@/types/release'

export const useReleaseStore = defineStore('release', () => {
  const releases = ref<ReleaseVersion[]>([])
  const loading = ref(false)

  async function loadReleases(projectId?: number) {
    loading.value = true
    try {
      const response = await fetchReleases(projectId)
      releases.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function setCurrentAction(id: number, projectId?: number) {
    await setCurrentRelease(id)
    await loadReleases(projectId)
  }

  async function rollbackAction(id: number, projectId?: number) {
    await rollbackRelease(id)
    await loadReleases(projectId)
  }

  async function deleteAction(id: number, projectId?: number) {
    await deleteRelease(id)
    await loadReleases(projectId)
  }

  return {
    releases,
    loading,
    loadReleases,
    setCurrentAction,
    rollbackAction,
    deleteAction
  }
})
