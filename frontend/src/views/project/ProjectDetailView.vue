<template>
  <section class="page-section">
    <el-card shadow="never">
      <template #header>
        <div class="detail-header">
          <div>
            <div class="title">{{ project?.name || '项目详情' }}</div>
            <div class="subtitle">{{ project?.description || '后续将在这里扩展源码版本、构建配置和发布版本标签页。' }}</div>
          </div>
          <div class="actions">
            <el-button type="primary" plain @click="copyUrl" :disabled="!previewInfo?.latestUrl">复制 latest 地址</el-button>
            <el-button @click="router.push('/projects')">返回列表</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border v-if="project">
        <el-descriptions-item label="项目标识">{{ project.code }}</el-descriptions-item>
        <el-descriptions-item label="项目类型">{{ project.projectType }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ project.status }}</el-descriptions-item>
        <el-descriptions-item label="当前版本">{{ project.currentVersion }}</el-descriptions-item>
        <el-descriptions-item label="latest 地址" :span="2">{{ previewInfo?.latestUrl || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" v-if="project">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="源码版本" name="source">
          <SourceVersionTab :project-id="project.id" />
        </el-tab-pane>
        <el-tab-pane label="构建配置" name="profile">
          <BuildProfileTab :project-id="project.id" />
        </el-tab-pane>
        <el-tab-pane label="构建任务" name="task">
          <BuildTaskInProjectTab :project-id="project.id" :active="activeTab === 'task'" />
        </el-tab-pane>
        <el-tab-pane label="发布版本" name="release">
          <ReleaseVersionTab :project-id="project.id" :latest-url="previewInfo?.latestUrl || ''" />
        </el-tab-pane>
        <el-tab-pane label="需求文档" name="document">
          <DocumentTab :project-id="project.id" />
        </el-tab-pane>
        <el-tab-pane label="提示词资产" name="prompt">
          <PromptTab :project-id="project.id" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchProject, fetchProjectPreviewInfo } from '@/api/project'
import BuildProfileTab from '@/views/project/tabs/BuildProfileTab.vue'
import BuildTaskInProjectTab from '@/views/project/tabs/BuildTaskInProjectTab.vue'
import DocumentTab from '@/views/project/tabs/DocumentTab.vue'
import PromptTab from '@/views/project/tabs/PromptTab.vue'
import ReleaseVersionTab from '@/views/project/tabs/ReleaseVersionTab.vue'
import SourceVersionTab from '@/views/project/tabs/SourceVersionTab.vue'
import type { Project, ProjectPreviewInfo } from '@/types/project'

const route = useRoute()
const router = useRouter()
const project = ref<Project | null>(null)
const previewInfo = ref<ProjectPreviewInfo | null>(null)
const activeTab = ref('source')

onMounted(async () => {
  const id = Number(route.params.id)
  const [projectResponse, previewResponse] = await Promise.all([
    fetchProject(id),
    fetchProjectPreviewInfo(id)
  ])
  project.value = projectResponse.data
  previewInfo.value = previewResponse.data
})

async function copyUrl() {
  if (!previewInfo.value?.latestUrl) {
    return
  }
  await navigator.clipboard.writeText(previewInfo.value.latestUrl)
  ElMessage.success('已复制 latest 地址')
}
</script>

<style scoped>
.page-section {
  display: grid;
  gap: 16px;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.title {
  font-size: 24px;
  font-weight: 700;
}

.subtitle {
  margin-top: 8px;
  color: #6b7280;
}

.actions {
  display: flex;
  gap: 12px;
}
</style>
