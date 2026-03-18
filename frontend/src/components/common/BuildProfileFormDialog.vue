<template>
  <el-dialog :model-value="modelValue" :title="profile ? '编辑构建配置' : '新增构建配置'" width="680px" @close="closeDialog">
    <el-form :model="form" label-width="110px">
      <el-form-item label="配置名称">
        <el-input v-model="form.profileName" maxlength="128" />
      </el-form-item>
      <el-form-item label="Node 版本">
        <el-input v-model="form.nodeVersion" placeholder="node:18-alpine" />
      </el-form-item>
      <el-form-item label="安装命令">
        <el-input v-model="form.installCommand" placeholder="npm install" />
      </el-form-item>
      <el-form-item label="构建命令">
        <el-input v-model="form.buildCommand" placeholder="npm run build" />
      </el-form-item>
      <el-form-item label="产物目录">
        <el-input v-model="form.outputDir" placeholder="dist" />
      </el-form-item>
      <el-form-item label="环境变量 JSON">
        <el-input v-model="form.envJson" type="textarea" :rows="4" placeholder="{&quot;NODE_ENV&quot;:&quot;production&quot;}" />
      </el-form-item>
      <el-form-item label="默认配置">
        <el-switch v-model="defaultSwitch" />
      </el-form-item>
      <el-form-item label="启用">
        <el-switch v-model="enabledSwitch" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import type { BuildProfile } from '@/types/buildProfile'

const props = defineProps<{
  modelValue: boolean
  profile?: BuildProfile | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: {
    profileName: string
    nodeVersion: string
    installCommand: string
    buildCommand: string
    outputDir: string
    envJson: string
    isDefault: number
    enabled: number
  }]
}>()

const defaultForm = () => ({
  profileName: '',
  nodeVersion: 'node:18-alpine',
  installCommand: 'npm install',
  buildCommand: 'npm run build',
  outputDir: 'dist',
  envJson: '{}',
  isDefault: 0,
  enabled: 1
})

const form = reactive(defaultForm())
const defaultSwitch = computed({
  get: () => form.isDefault === 1,
  set: (value: boolean) => {
    form.isDefault = value ? 1 : 0
  }
})
const enabledSwitch = computed({
  get: () => form.enabled === 1,
  set: (value: boolean) => {
    form.enabled = value ? 1 : 0
  }
})

watch(
  () => props.profile,
  (profile) => {
    Object.assign(form, defaultForm())
    if (profile) {
      Object.assign(form, {
        profileName: profile.profileName,
        nodeVersion: profile.nodeVersion,
        installCommand: profile.installCommand,
        buildCommand: profile.buildCommand,
        outputDir: profile.outputDir,
        envJson: profile.envJson || '{}',
        isDefault: profile.isDefault,
        enabled: profile.enabled
      })
    }
  },
  { immediate: true }
)

function closeDialog() {
  emit('update:modelValue', false)
}

function submit() {
  emit('submit', { ...form })
}
</script>
