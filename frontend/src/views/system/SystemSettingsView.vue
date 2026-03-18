<template>
  <section class="page-section">
    <div class="toolbar">
      <div>
        <h2>系统设置</h2>
        <p>配置系统参数，修改后点击保存生效。</p>
      </div>
      <el-button type="primary" :loading="store.loading" @click="handleSave">保存配置</el-button>
    </div>

    <el-card shadow="never" v-loading="store.loading">
      <el-empty v-if="store.configs.length === 0 && !store.loading" description="暂无系统配置项" />
      <template v-else>
        <el-form :model="formData" label-width="220px">
          <el-divider content-position="left">基础配置</el-divider>
          <template v-for="config in store.configs" :key="config.configKey">
            <el-form-item :label="config.configKey">
              <el-input v-model="formData[config.configKey]" :placeholder="config.description || ''" />
              <div v-if="config.description" class="field-desc">{{ config.description }}</div>
            </el-form-item>
          </template>
        </el-form>
      </template>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, watch } from 'vue'
import { useSystemStore } from '@/stores/systemStore'

const store = useSystemStore()
const formData = reactive<Record<string, string>>({})

onMounted(async () => {
  await store.load()
  syncFormData()
})

watch(() => store.configs, syncFormData)

function syncFormData() {
  for (const config of store.configs) {
    formData[config.configKey] = config.configValue
  }
}

async function handleSave() {
  const sensitiveKeys = ['base-preview-url', 'deploy.root-path', 'storage.root-path']
  const hasSensitive = store.configs.some(c => sensitiveKeys.some(k => c.configKey.includes(k)))
  if (hasSensitive) {
    try {
      await ElMessageBox.confirm('部分配置项修改后需重启服务才能生效，是否继续？', '确认保存', { type: 'warning' })
    } catch {
      return
    }
  }

  const items = store.configs.map(c => ({
    configKey: c.configKey,
    configValue: formData[c.configKey] ?? c.configValue
  }))

  await store.save(items)
  ElMessage.success('系统配置已保存')
}
</script>

<style scoped>
.page-section {
  display: grid;
  gap: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

h2 {
  margin: 0 0 6px;
}

p {
  margin: 0;
  color: #6b7280;
}

.field-desc {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}
</style>
