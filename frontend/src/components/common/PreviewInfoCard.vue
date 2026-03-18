<template>
  <el-card shadow="never">
    <div class="preview-card">
      <div>
        <div class="label">Latest 地址</div>
        <div class="url">{{ latestUrl || '-' }}</div>
      </div>
      <el-button type="primary" plain :disabled="!latestUrl" @click="copyUrl">复制地址</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

const props = defineProps<{
  latestUrl: string
}>()

async function copyUrl() {
  if (!props.latestUrl) {
    return
  }
  await navigator.clipboard.writeText(props.latestUrl)
  ElMessage.success('已复制 latest 地址')
}
</script>

<style scoped>
.preview-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.label {
  font-size: 12px;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.url {
  margin-top: 8px;
  word-break: break-all;
}
</style>
