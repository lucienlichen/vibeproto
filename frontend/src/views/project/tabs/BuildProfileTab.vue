<template>
  <section class="tab-section">
    <div class="tab-toolbar">
      <div>
        <h3>构建配置</h3>
        <p>为项目维护安装命令、构建命令、产物目录和默认模板。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增配置</el-button>
    </div>

    <el-table v-loading="loading" :data="records">
      <el-table-column prop="profileName" label="配置名称" min-width="180" />
      <el-table-column prop="nodeVersion" label="Node 版本" width="140" />
      <el-table-column prop="installCommand" label="安装命令" min-width="180" />
      <el-table-column prop="buildCommand" label="构建命令" min-width="180" />
      <el-table-column prop="outputDir" label="产物目录" width="120" />
      <el-table-column label="默认" width="90">
        <template #default="{ row }">{{ row.isDefault === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="启用" width="90">
        <template #default="{ row }">{{ row.enabled === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="danger" :disabled="row.projectId === null" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <BuildProfileFormDialog v-model="dialogVisible" :profile="editingProfile" @submit="handleSubmit" />
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import { createBuildProfile, deleteBuildProfile, fetchBuildProfiles, updateBuildProfile } from '@/api/buildProfile'
import BuildProfileFormDialog from '@/components/common/BuildProfileFormDialog.vue'
import type { BuildProfile } from '@/types/buildProfile'

const props = defineProps<{ projectId: number }>()

const loading = ref(false)
const records = ref<BuildProfile[]>([])
const dialogVisible = ref(false)
const editingProfile = ref<BuildProfile | null>(null)

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const response = await fetchBuildProfiles(props.projectId)
    records.value = response.data
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingProfile.value = null
  dialogVisible.value = true
}

function openEditDialog(profile: BuildProfile) {
  editingProfile.value = profile
  dialogVisible.value = true
}

async function handleSubmit(payload: {
  profileName: string
  nodeVersion: string
  installCommand: string
  buildCommand: string
  outputDir: string
  envJson: string
  isDefault: number
  enabled: number
}) {
  if (editingProfile.value) {
    await updateBuildProfile(editingProfile.value.id, payload)
    ElMessage.success('构建配置已更新')
  } else {
    await createBuildProfile({ projectId: props.projectId, ...payload })
    ElMessage.success('构建配置已创建')
  }
  dialogVisible.value = false
  await loadData()
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('删除后该构建配置不可恢复，是否继续？', '删除构建配置', { type: 'warning' })
  } catch {
    return
  }
  await deleteBuildProfile(id)
  ElMessage.success('构建配置已删除')
  await loadData()
}
</script>

<style scoped>
.tab-section {
  display: grid;
  gap: 16px;
}

.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.tab-toolbar h3 {
  margin: 0 0 6px;
}

.tab-toolbar p {
  margin: 0;
  color: #6b7280;
}
</style>
