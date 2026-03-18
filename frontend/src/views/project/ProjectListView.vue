<template>
  <section class="page-section">
    <el-card shadow="never">
      <div class="toolbar">
        <div>
          <h2>项目列表</h2>
          <p>按名称、类型、状态筛选，支持新建、编辑、归档和删除。</p>
        </div>
        <el-button type="primary" @click="openCreateDialog">新建项目</el-button>
      </div>

      <el-form :inline="true" class="filters">
        <el-form-item>
          <el-input v-model="projectStore.filters.name" placeholder="项目名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="projectStore.filters.projectType" placeholder="项目类型" clearable style="width: 140px">
            <el-option label="HTML" value="html" />
            <el-option label="Vue" value="vue" />
            <el-option label="React" value="react" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="projectStore.filters.status" placeholder="状态" clearable style="width: 140px">
            <el-option label="草稿" value="draft" />
            <el-option label="启用" value="active" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="projectStore.loading" :data="projectStore.projects">
        <el-table-column label="项目名称" min-width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/projects/${row.id}`)">{{ row.name }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="项目标识" width="160" />
        <el-table-column prop="projectType" label="项目类型" width="120" />
        <el-table-column prop="currentVersion" label="当前版本" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="handleArchive(row.id)">归档</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :total="projectStore.pagination.total"
          :current-page="projectStore.pagination.pageNum"
          :page-size="projectStore.pagination.pageSize"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <ProjectFormDialog
      v-model="dialogVisible"
      :project="editingProject"
      @submit="handleSubmit"
    />
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ProjectFormDialog from '@/components/common/ProjectFormDialog.vue'
import { useProjectStore } from '@/stores/projectStore'
import type { Project } from '@/types/project'

const router = useRouter()
const projectStore = useProjectStore()
const dialogVisible = ref(false)
const editingProject = ref<Project | null>(null)

onMounted(() => {
  projectStore.loadProjects()
})

function openCreateDialog() {
  editingProject.value = null
  dialogVisible.value = true
}

function openEditDialog(project: Project) {
  editingProject.value = project
  dialogVisible.value = true
}

async function handleSubmit(payload: {
  name: string
  code: string
  description: string
  projectType: 'html' | 'vue' | 'react' | 'other'
  defaultBuildProfileId: number
  status: 'draft' | 'active' | 'archived'
}) {
  try {
    if (editingProject.value) {
      await projectStore.updateProjectAction(editingProject.value.id, {
        name: payload.name,
        description: payload.description,
        projectType: payload.projectType,
        defaultBuildProfileId: payload.defaultBuildProfileId,
        status: payload.status
      })
      ElMessage.success('项目已更新')
    } else {
      await projectStore.createProjectAction({
        name: payload.name,
        code: payload.code,
        description: payload.description,
        projectType: payload.projectType,
        defaultBuildProfileId: payload.defaultBuildProfileId
      })
      ElMessage.success('项目已创建')
    }
    dialogVisible.value = false
  } catch {
    return
  }
}

async function handleArchive(id: number) {
  try {
    await ElMessageBox.confirm('归档后项目仍可查看，但不再作为活跃项目展示。是否继续？', '归档项目', {
      type: 'warning'
    })
  } catch {
    return
  }
  await projectStore.archiveProjectAction(id)
  ElMessage.success('项目已归档')
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('删除后项目将进入逻辑删除状态。是否继续？', '删除项目', {
      type: 'warning'
    })
  } catch {
    return
  }
  await projectStore.deleteProjectAction(id)
  ElMessage.success('项目已删除')
}

function handleSearch() {
  projectStore.pagination.pageNum = 1
  projectStore.loadProjects()
}

function handleReset() {
  projectStore.resetFilters()
  projectStore.loadProjects()
}

function handlePageChange(pageNum: number) {
  projectStore.pagination.pageNum = pageNum
  projectStore.loadProjects()
}

function handleSizeChange(pageSize: number) {
  projectStore.pagination.pageSize = pageSize
  projectStore.pagination.pageNum = 1
  projectStore.loadProjects()
}
</script>

<style scoped>
.page-section {
  display: grid;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

h2 {
  margin: 0 0 6px;
}

p {
  margin: 0;
  color: #6b7280;
}

.filters {
  margin: 20px 0 12px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
