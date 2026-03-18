<template>
  <section class="page-section">
    <div class="toolbar">
      <div>
        <h2>构建任务</h2>
        <p>查看全局构建任务状态，并支持重试、取消和日志查看。</p>
      </div>
    </div>

    <el-card shadow="never">
      <el-table v-loading="store.loading" :data="store.tasks">
        <el-table-column prop="taskNo" label="任务编号" min-width="200" />
        <el-table-column label="项目" min-width="160">
          <template #default="{ row }">{{ row.projectName || row.projectId }}</template>
        </el-table-column>
        <el-table-column label="源码版本" width="120">
          <template #default="{ row }">{{ row.sourceVersionNo || row.sourceVersionId }}</template>
        </el-table-column>
        <el-table-column label="构建配置" min-width="140">
          <template #default="{ row }">{{ row.buildProfileName || row.buildProfileId }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="openLog(row.id)">日志</el-button>
            <el-button link type="warning" @click="handleRetry(row.id)">重试</el-button>
            <el-button link type="danger" @click="handleCancel(row.id)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <BuildLogDrawer v-model="logVisible" :content="logContent" />
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import BuildLogDrawer from '@/components/common/BuildLogDrawer.vue'
import { useBuildStore } from '@/stores/buildStore'

const store = useBuildStore()
const logVisible = ref(false)
const logContent = ref('')

onMounted(() => store.loadTasks())

function statusType(status: string) {
  const map: Record<string, string> = {
    pending: 'info', running: 'warning', success: 'success', failed: 'danger', canceled: ''
  }
  return map[status] ?? 'info'
}

async function openLog(id: number) {
  logContent.value = await store.fetchLog(id)
  logVisible.value = true
}

async function handleRetry(id: number) {
  await store.retryTask(id)
  ElMessage.success('已创建重试任务')
  await store.loadTasks()
}

async function handleCancel(id: number) {
  await store.cancelTask(id)
  ElMessage.success('任务已取消')
  await store.loadTasks()
}
</script>

<style scoped>
.page-section {
  display: grid;
  gap: 16px;
}

h2 {
  margin: 0 0 6px;
}

p {
  margin: 0;
  color: #6b7280;
}
</style>
