<template>
  <el-drawer :model-value="modelValue" title="构建日志" size="50%" @close="closeDrawer">
    <pre class="log-content">{{ content || '暂无日志' }}</pre>
  </el-drawer>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: boolean
  content: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

function closeDrawer() {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.log-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
</style>
