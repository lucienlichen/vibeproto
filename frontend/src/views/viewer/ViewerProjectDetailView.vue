<template>
  <div>
    <div class="toolbar">
      <el-button @click="router.push('/viewer/projects')">返回</el-button>
    </div>

    <el-card v-if="project" class="project-info" style="margin-bottom: 20px">
      <template #header>
        <span class="project-name">{{ project.name }}</span>
      </template>
      <p v-if="project.description" class="project-description">{{ project.description }}</p>
    </el-card>

    <el-table :data="releases" v-loading="loading" border style="width: 100%">
      <el-table-column prop="versionNo" label="版本号" />
      <el-table-column label="预览地址">
        <template #default="{ row }">
          <a v-if="row.previewUrl" :href="row.previewUrl" target="_blank">{{ row.previewUrl }}</a>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="当前版本" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isCurrent === 1" type="success">当前</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="window.open(row.previewUrl, '_blank')">预览</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchProject } from '@/api/project'
import { fetchReleases } from '@/api/release'
import type { Project } from '@/types/project'
import type { ReleaseVersion } from '@/types/release'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)

const loading = ref(false)
const project = ref<Project | null>(null)
const releases = ref<ReleaseVersion[]>([])
const window = globalThis.window

async function load() {
  loading.value = true
  try {
    const [projRes, relRes] = await Promise.all([
      fetchProject(id),
      fetchReleases(id)
    ])
    project.value = projRes.data
    releases.value = relRes.data
  } finally {
    loading.value = false
  }
}

function formatDate(val: string) {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 19)
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}

.project-name {
  font-size: 18px;
  font-weight: 600;
}

.project-description {
  color: #6b7280;
  margin: 0;
}
</style>
