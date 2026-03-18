<template>
  <el-dialog :model-value="modelValue" title="Git 导入" width="560px" @close="closeDialog">
    <el-form :model="form" label-width="100px">
      <el-form-item label="Git 地址">
        <el-input v-model="form.gitUrl" placeholder="https://example.com/repo.git" />
      </el-form-item>
      <el-form-item label="分支">
        <el-input v-model="form.gitBranch" placeholder="main" />
      </el-form-item>
      <el-form-item label="Commit">
        <el-input v-model="form.commitHash" placeholder="可选" />
      </el-form-item>
      <el-form-item label="版本说明">
        <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" @click="submit">导入</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [{ gitUrl: string; gitBranch: string; commitHash: string; remark: string }]
}>()

const createDefaultForm = () => ({
  gitUrl: '',
  gitBranch: 'main',
  commitHash: '',
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
