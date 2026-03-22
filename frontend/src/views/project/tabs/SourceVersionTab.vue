<template>
  <section class="tab-section">
    <div class="tab-toolbar">
      <div>
        <h3>源码版本</h3>
        <p>支持 ZIP 上传、Git 导入和在线创建 HTML。</p>
      </div>
      <div class="actions">
        <el-button @click="gitDialogVisible = true">Git 导入</el-button>
        <el-button @click="htmlDialogVisible = true">在线创建 HTML</el-button>
        <el-button type="primary" @click="uploadDialogVisible = true">上传 ZIP</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="records">
      <el-table-column prop="versionNo" label="版本号" width="180" />
      <el-table-column prop="sourceType" label="来源类型" width="120" />
      <el-table-column prop="sourceName" label="来源信息" min-width="220" />
      <el-table-column prop="remark" label="说明" min-width="220" />
      <el-table-column prop="createdAt" label="提交时间" min-width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <UploadZipDialog v-model="uploadDialogVisible" :uploading="uploading" :progress="uploadProgress" @submit="handleUpload" />
    <HtmlCreateDialog v-model="htmlDialogVisible" @submit="handleHtmlCreate" />
    <GitImportDialog v-model="gitDialogVisible" :importing="gitImporting" :import-status="gitImportStatus" @submit="handleGitImport" />
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import GitImportDialog from '@/components/common/GitImportDialog.vue'
import HtmlCreateDialog from '@/components/common/HtmlCreateDialog.vue'
import UploadZipDialog from '@/components/common/UploadZipDialog.vue'
import { createHtmlSource, deleteSourceVersion, fetchSourceVersions, importGitSource, uploadSourceZip } from '@/api/sourceVersion'
import type { SourceVersion } from '@/types/sourceVersion'

const props = defineProps<{ projectId: number }>()

const loading = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const gitImporting = ref(false)
const gitImportStatus = ref('正在克隆仓库...')
const records = ref<SourceVersion[]>([])
const uploadDialogVisible = ref(false)
const htmlDialogVisible = ref(false)
const gitDialogVisible = ref(false)

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const response = await fetchSourceVersions(props.projectId)
    records.value = response.data
  } finally {
    loading.value = false
  }
}

async function handleUpload(payload: { file: File; remark: string }) {
  uploading.value = true
  uploadProgress.value = 0
  try {
    await uploadSourceZip(props.projectId, payload.file, payload.remark, (percent) => {
      uploadProgress.value = percent
    })
    uploadDialogVisible.value = false
    ElMessage.success('ZIP 已上传')
    await loadData()
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

async function handleHtmlCreate(payload: { sourceName: string; htmlContent: string; remark: string }) {
  await createHtmlSource({ projectId: props.projectId, ...payload })
  htmlDialogVisible.value = false
  ElMessage.success('HTML 源码已创建')
  await loadData()
}

async function handleGitImport(payload: { gitUrl: string; gitBranch: string; commitHash: string; remark: string }) {
  gitImporting.value = true
  gitImportStatus.value = '正在克隆仓库...'
  // Simulate progress stages (actual clone is server-side, we show time-based stages)
  const timer = setTimeout(() => { gitImportStatus.value = '正在打包源码...' }, 5000)
  const timer2 = setTimeout(() => { gitImportStatus.value = '正在存储文件...' }, 10000)
  try {
    await importGitSource({ projectId: props.projectId, ...payload })
    gitDialogVisible.value = false
    ElMessage.success('Git 源码导入成功')
    await loadData()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || 'Git 导入失败')
  } finally {
    gitImporting.value = false
    clearTimeout(timer)
    clearTimeout(timer2)
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('删除后源码版本记录将不可恢复，是否继续？', '删除源码版本', { type: 'warning' })
  } catch {
    return
  }
  await deleteSourceVersion(id)
  ElMessage.success('源码版本已删除')
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

.actions {
  display: flex;
  gap: 12px;
}
</style>
