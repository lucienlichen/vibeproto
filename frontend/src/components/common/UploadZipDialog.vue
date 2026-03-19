<template>
  <el-dialog :model-value="modelValue" title="上传 ZIP" width="520px" :close-on-click-modal="!uploading" @close="closeDialog">
    <el-form label-width="100px">
      <el-form-item label="选择文件">
        <el-upload
          :auto-upload="false"
          :show-file-list="true"
          :limit="1"
          accept=".zip"
          :on-change="handleChange"
          :on-remove="handleRemove"
        >
          <el-button :disabled="uploading">选择 ZIP</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="版本说明">
        <el-input v-model="remark" type="textarea" :rows="4" maxlength="500" show-word-limit :disabled="uploading" />
      </el-form-item>
      <el-form-item v-if="uploading" label="上传进度">
        <el-progress :percentage="progress" :stroke-width="10" style="width: 100%" />
        <span style="font-size: 12px; color: #6b7280; margin-top: 4px; display: block">
          {{ progress < 100 ? '正在上传，请勿关闭...' : '上传完成，处理中...' }}
        </span>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button :disabled="uploading" @click="closeDialog">取消</el-button>
      <el-button type="primary" :disabled="!file || uploading" :loading="uploading" @click="submit">
        {{ uploading ? (progress < 100 ? `上传中 ${progress}%` : '处理中...') : '上传' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { UploadFile, UploadFiles } from 'element-plus'
import { ref } from 'vue'

withDefaults(defineProps<{ modelValue: boolean; uploading?: boolean; progress?: number }>(), {
  uploading: false,
  progress: 0
})
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [{ file: File; remark: string }]
}>()

const file = ref<File | null>(null)
const remark = ref('')

function handleChange(uploadFile: UploadFile, _files: UploadFiles) {
  file.value = uploadFile.raw || null
}

function handleRemove() {
  file.value = null
}

function closeDialog() {
  if (file.value) file.value = null
  remark.value = ''
  emit('update:modelValue', false)
}

function submit() {
  if (!file.value) return
  emit('submit', { file: file.value, remark: remark.value })
}
</script>
