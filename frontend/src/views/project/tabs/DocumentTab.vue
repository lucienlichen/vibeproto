<template>
  <section class="tab-section">
    <div class="tab-toolbar">
      <div>
        <h3>需求文档</h3>
        <p>先以 Markdown 文本方式维护 PRD、页面说明、交互说明和技术说明。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增文档</el-button>
    </div>

    <div class="content-grid">
      <el-card shadow="never" class="doc-list-card">
        <el-table v-loading="loading" :data="documents" @row-click="selectDocument">
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column prop="docType" label="类型" width="160" />
          <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click.stop="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="doc-preview-card">
        <template v-if="activeDocument">
          <div class="preview-header">
            <div class="preview-title">{{ activeDocument.title }}</div>
            <el-tag>{{ activeDocument.docType }}</el-tag>
          </div>
          <pre class="preview-content">{{ activeDocument.content }}</pre>
        </template>
        <el-empty v-else description="选择左侧文档查看内容" />
      </el-card>
    </div>

    <el-dialog :model-value="dialogVisible" :title="editingDocument ? '编辑文档' : '新增文档'" width="760px" @close="closeDialog">
      <el-form :model="form" label-width="100px">
        <el-form-item label="文档类型">
          <el-select v-model="form.docType" style="width: 100%">
            <el-option label="PRD" value="prd" />
            <el-option label="页面说明" value="page_spec" />
            <el-option label="交互说明" value="interaction_spec" />
            <el-option label="技术说明" value="tech_note" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="255" />
        </el-form-item>
        <el-form-item label="关联发布">
          <el-select v-model="form.relatedReleaseId" clearable style="width: 100%">
            <el-option v-for="item in releases" :key="item.id" :label="item.versionNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="16" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { createDocument, deleteDocument, fetchDocuments, updateDocument } from '@/api/document'
import { fetchReleases } from '@/api/release'
import type { ProjectDocument } from '@/types/document'
import type { ReleaseVersion } from '@/types/release'

const props = defineProps<{ projectId: number }>()

const loading = ref(false)
const documents = ref<ProjectDocument[]>([])
const releases = ref<ReleaseVersion[]>([])
const activeDocument = ref<ProjectDocument | null>(null)
const dialogVisible = ref(false)
const editingDocument = ref<ProjectDocument | null>(null)

const defaultForm = () => ({
  docType: 'prd' as const,
  title: '',
  content: '',
  relatedReleaseId: null as number | null
})

const form = reactive(defaultForm())

onMounted(async () => {
  await Promise.all([loadDocuments(), loadReleases()])
})

async function loadDocuments() {
  loading.value = true
  try {
    const response = await fetchDocuments(props.projectId)
    documents.value = response.data
    activeDocument.value = documents.value[0] || null
  } finally {
    loading.value = false
  }
}

async function loadReleases() {
  const response = await fetchReleases(props.projectId)
  releases.value = response.data
}

function selectDocument(document: ProjectDocument) {
  activeDocument.value = document
}

function openCreate() {
  editingDocument.value = null
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

function openEdit(document: ProjectDocument) {
  editingDocument.value = document
  Object.assign(form, {
    docType: document.docType,
    title: document.title,
    content: document.content,
    relatedReleaseId: document.relatedReleaseId
  })
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
}

async function handleSubmit() {
  if (editingDocument.value) {
    await updateDocument(editingDocument.value.id, { ...form })
    ElMessage.success('文档已更新')
  } else {
    await createDocument({ projectId: props.projectId, ...form })
    ElMessage.success('文档已创建')
  }
  dialogVisible.value = false
  await loadDocuments()
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('删除后文档不可恢复，是否继续？', '删除文档', { type: 'warning' })
  } catch {
    return
  }
  await deleteDocument(id)
  ElMessage.success('文档已删除')
  await loadDocuments()
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

.content-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 16px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.preview-title {
  font-size: 18px;
  font-weight: 600;
}

.preview-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 960px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
