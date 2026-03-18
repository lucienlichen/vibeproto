<template>
  <el-dialog :model-value="modelValue" title="在线创建 HTML" width="720px" @close="closeDialog">
    <el-form :model="form" label-width="100px">
      <el-form-item label="页面名称">
        <el-input v-model="form.sourceName" maxlength="255" />
      </el-form-item>
      <el-form-item label="HTML 内容">
        <el-input v-model="form.htmlContent" type="textarea" :rows="12" />
      </el-form-item>
      <el-form-item label="版本说明">
        <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [{ sourceName: string; htmlContent: string; remark: string }]
}>()

const createDefaultForm = () => ({
  sourceName: 'index.html',
  htmlContent: '<!doctype html>\n<html>\n  <body>\n    <h1>Hello VibeProto</h1>\n  </body>\n</html>',
  remark: ''
})

const form = reactive(createDefaultForm())

function closeDialog() {
  Object.assign(form, createDefaultForm())
  emit('update:modelValue', false)
}

function submit() {
  emit('submit', { ...form })
}
</script>
