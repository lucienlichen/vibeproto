<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑项目' : '新建项目'"
    width="560px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="项目名称" prop="name">
        <el-input v-model="form.name" maxlength="128" show-word-limit />
      </el-form-item>
      <el-form-item label="项目标识" prop="code">
        <el-input v-model="form.code" :disabled="isEdit" maxlength="64" show-word-limit />
      </el-form-item>
      <el-form-item label="项目类型" prop="projectType">
        <el-select v-model="form.projectType" style="width: 100%">
          <el-option label="HTML" value="html" />
          <el-option label="Vue" value="vue" />
          <el-option label="React" value="react" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>
      <el-form-item label="默认模板" prop="defaultBuildProfileId">
        <el-select v-model="form.defaultBuildProfileId" style="width: 100%">
          <el-option label="HTML 默认模板" :value="1" />
          <el-option label="Vue3 默认模板" :value="2" />
          <el-option label="React 默认模板" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="isEdit" label="状态" prop="status">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="草稿" value="draft" />
          <el-option label="启用" value="active" />
          <el-option label="已归档" value="archived" />
        </el-select>
      </el-form-item>
      <el-form-item label="项目简介" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="4" maxlength="2000" show-word-limit />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { computed, reactive, ref, watch } from 'vue'
import type { Project } from '@/types/project'

const props = defineProps<{
  modelValue: boolean
  project?: Project | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: {
    name: string
    code: string
    description: string
    projectType: 'html' | 'vue' | 'react' | 'other'
    defaultBuildProfileId: number
    status: 'draft' | 'active' | 'archived'
  }]
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const isEdit = computed(() => Boolean(props.project))

const defaultForm = () => ({
  name: '',
  code: '',
  description: '',
  projectType: 'vue' as const,
  defaultBuildProfileId: 2,
  status: 'draft' as const
})

const form = reactive(defaultForm())

const rules: FormRules<typeof form> = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入项目标识', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
  defaultBuildProfileId: [{ required: true, message: '请选择默认模板', trigger: 'change' }],
  status: [{ required: true, message: '请选择项目状态', trigger: 'change' }]
}

watch(
  () => props.project,
  (project) => {
    Object.assign(form, defaultForm())
    if (project) {
      Object.assign(form, {
        name: project.name,
        code: project.code,
        description: project.description,
        projectType: project.projectType,
        defaultBuildProfileId: project.defaultBuildProfileId,
        status: project.status
      })
    }
  },
  { immediate: true }
)

function handleClose() {
  emit('update:modelValue', false)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  submitting.value = true
  try {
    emit('submit', { ...form })
  } finally {
    submitting.value = false
  }
}
</script>
