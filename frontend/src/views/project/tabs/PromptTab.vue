<template>
  <section class="tab-section">
    <div class="tab-toolbar">
      <div>
        <h3>提示词资产</h3>
        <p>管理项目的 AI 提示词，支持一键复制。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增提示词</el-button>
    </div>

    <div class="content-grid">
      <el-card shadow="never" class="list-card">
        <el-table v-loading="loading" :data="prompts" @row-click="selectPrompt">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="promptType" label="类型" width="120">
            <template #default="{ row }">
              <el-tag size="small">{{ PROMPT_TYPE_LABELS[row.promptType as PromptType] || row.promptType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click.stop="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="preview-card">
        <template v-if="activePrompt">
          <div class="preview-header">
            <div class="preview-title">{{ activePrompt.title }}</div>
            <div class="preview-actions">
              <el-tag size="small">{{ PROMPT_TYPE_LABELS[activePrompt.promptType] || activePrompt.promptType }}</el-tag>
              <el-button size="small" @click="copyContent">复制内容</el-button>
            </div>
          </div>
          <pre class="preview-content">{{ activePrompt.content }}</pre>
        </template>
        <el-empty v-else description="选择左侧提示词查看内容" />
      </el-card>
    </div>

    <el-dialog :model-value="dialogVisible" :title="editingPrompt ? '编辑提示词' : '新增提示词'" width="760px" @close="closeDialog">
      <el-form :model="form" label-width="100px">
        <el-form-item label="提示词类型">
          <el-select v-model="form.promptType" style="width: 100%">
            <el-option v-for="(label, value) in PROMPT_TYPE_LABELS" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="255" />
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
import { storeToRefs } from 'pinia'
import { usePromptStore } from '@/stores/promptStore'
import type { PromptAsset, PromptType } from '@/types/prompt'
import { PROMPT_TYPE_LABELS } from '@/types/prompt'

const props = defineProps<{ projectId: number }>()

const store = usePromptStore()
const { prompts, loading } = storeToRefs(store)
const activePrompt = ref<PromptAsset | null>(null)
const dialogVisible = ref(false)
const editingPrompt = ref<PromptAsset | null>(null)

const defaultForm = () => ({
  promptType: 'page_gen' as PromptType,
  title: '',
  content: ''
})

const form = reactive(defaultForm())

onMounted(loadPrompts)

async function loadPrompts() {
  await store.load(props.projectId)
  activePrompt.value = prompts.value[0] || null
}

function selectPrompt(prompt: PromptAsset) {
  activePrompt.value = prompt
}

function openCreate() {
  editingPrompt.value = null
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

function openEdit(prompt: PromptAsset) {
  editingPrompt.value = prompt
  Object.assign(form, {
    promptType: prompt.promptType,
    title: prompt.title,
    content: prompt.content
  })
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
}

async function handleSubmit() {
  if (editingPrompt.value) {
    await store.update(editingPrompt.value.id, {
      title: form.title,
      promptType: form.promptType,
      content: form.content,
      relatedSourceVersionId: null,
      relatedReleaseId: null
    })
    ElMessage.success('提示词已更新')
  } else {
    await store.create({
      projectId: props.projectId,
      title: form.title,
      promptType: form.promptType,
      content: form.content,
      relatedSourceVersionId: null,
      relatedReleaseId: null
    })
    ElMessage.success('提示词已创建')
  }
  dialogVisible.value = false
  await loadPrompts()
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('删除后不可恢复，是否继续？', '删除提示词', { type: 'warning' })
  } catch {
    return
  }
  await store.remove(id)
  ElMessage.success('提示词已删除')
  await loadPrompts()
}

async function copyContent() {
  if (!activePrompt.value?.content) return
  await navigator.clipboard.writeText(activePrompt.value.content)
  ElMessage.success('已复制提示词内容')
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
  grid-template-columns: 1fr 1fr;
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

.preview-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 960px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
