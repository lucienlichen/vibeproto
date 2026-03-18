import { defineStore } from 'pinia'
import { ref } from 'vue'
import { fetchSystemConfigs, updateSystemConfigs } from '@/api/system'
import type { SystemConfig } from '@/types/system'

export const useSystemStore = defineStore('system', () => {
  const configs = ref<SystemConfig[]>([])
  const loading = ref(false)

  async function load() {
    loading.value = true
    try {
      const response = await fetchSystemConfigs()
      configs.value = response.data
    } finally {
      loading.value = false
    }
  }

  async function save(items: Array<{ configKey: string; configValue: string }>) {
    loading.value = true
    try {
      const response = await updateSystemConfigs({ items })
      configs.value = response.data
    } finally {
      loading.value = false
    }
  }

  return { configs, loading, load, save }
})
