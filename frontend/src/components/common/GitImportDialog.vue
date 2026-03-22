<template>
  <el-dialog :model-value="modelValue" title="Git 导入" width="560px" :close-on-click-modal="!importing" @close="closeDialog">
    <el-form :model="form" label-width="100px">
      <el-form-item label="Git 地址">
        <el-input v-model="form.gitUrl" placeholder="https://github.com/user/repo.git" :disabled="importing" />
      </el-form-item>
      <el-form-item label="分支">
        <el-input v-model="form.gitBranch" placeholder="main" :disabled="importing" />
      </el-form-item>
      <el-form-item label="Commit">
        <el-input v-model="form.commitHash" placeholder="可选，留空拉取最新" :disabled="importing" />
      </el-form-item>
      <el-form-item label="版本说明">
        <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit :disabled="importing" />
      </el-form-item>
      <el-form-item v-if="importing" label="状态">
        <div style="display: flex; align-items: center; gap: 8px">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span style="color: #0f766e">{{ importStatus }}</span>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button :disabled="importing" @click="closeDialog">取消</el-button>
      <el-button type="primary" :disabled="!form.gitUrl || importing" :loading="importing" @click="submit">
        {{ importing ? '导入中...' : '导入' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { Loading } from '@element-plus/icons-vue'

withDefaults(defineProps<{ modelValue: boolean; importing?: boolean; importStatus?: string }>(), {
  importing: false,
  importStatus: '正在克隆仓库...'
})
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
