<template>
  <div>
    <div class="page-header">
      <div class="page-title">原型项目</div>
      <div class="page-subtitle">点击项目查看可预览的原型版本</div>
    </div>

    <el-table :data="projects" v-loading="loading" border style="width: 100%">
      <el-table-column prop="name" label="项目名称" />
      <el-table-column prop="projectType" label="项目类型" />
      <el-table-column prop="currentVersion" label="当前版本" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="router.push(`/viewer/projects/${row.id}`)">
            查看版本
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchProjects } from '@/api/project'
import type { Project } from '@/types/project'

const router = useRouter()
const loading = ref(false)
const projects = ref<Project[]>([])

async function loadProjects() {
  loading.value = true
  try {
    const res = await fetchProjects({ pageNum: 1, pageSize: 50 })
    projects.value = res.data.records
  } finally {
    loading.value = false
  }
}

onMounted(loadProjects)
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.page-subtitle {
  color: #6b7280;
  margin-top: 4px;
  font-size: 14px;
}
</style>
