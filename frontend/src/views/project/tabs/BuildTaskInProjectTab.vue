<template>
  <section class="tab-section">
    <div class="tab-toolbar">
      <div>
        <h3>构建任务</h3>
        <p>从项目内发起构建，并查看该项目下的任务执行记录。</p>
      </div>
      <div class="actions">
        <el-select v-model="selectedSourceVersionId" placeholder="选择源码版本" style="width: 180px">
          <el-option v-for="item in sourceVersions" :key="item.id" :label="item.versionNo" :value="item.id" />
        </el-select>
        <el-select v-model="selectedBuildProfileId" placeholder="选择构建配置" style="width: 180px">
          <el-option v-for="item in buildProfiles" :key="item.id" :label="item.profileName" :value="item.id" />
        </el-select>
        <el-button type="primary" @click="handleCreateTask">发起构建</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="records">
      <el-table-column prop="taskNo" label="任务编号" min-width="180" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="sourceVersionId" label="源码版本" width="100" />
      <el-table-column prop="buildProfileId" label="构建配置" width="100" />
      <el-table-column prop="createdAt" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="openLog(row.id)">日志</el-button>
          <el-button link type="warning" @click="handleRetry(row.id)">重试</el-button>
          <el-button link type="danger" @click="handleCancel(row.id)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <BuildLogDrawer v-model="logVisible" :content="logContent" />
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, ref, watch } from 'vue'
import { cancelBuildTask, createBuildTask, fetchBuildLog, fetchBuildTasks, retryBuildTask } from '@/api/buildTask'
import { fetchBuildProfiles } from '@/api/buildProfile'
import { fetchSourceVersions } from '@/api/sourceVersion'
import BuildLogDrawer from '@/components/common/BuildLogDrawer.vue'
import type { BuildProfile } from '@/types/buildProfile'
import type { BuildTask } from '@/types/buildTask'
import type { SourceVersion } from '@/types/sourceVersion'

const props = defineProps<{ projectId: number; active?: boolean }>()

watch(() => props.active, (val) => {
  if (val) {
    loadTasks()
    loadMeta()
  }
})

const loading = ref(false)
const records = ref<BuildTask[]>([])
const sourceVersions = ref<SourceVersion[]>([])
const buildProfiles = ref<BuildProfile[]>([])
const selectedSourceVersionId = ref<number>()
const selectedBuildProfileId = ref<number>()
const logVisible = ref(false)
const logContent = ref('')

onMounted(async () => {
  await Promise.all([loadTasks(), loadMeta()])
})

async function loadTasks() {
  loading.value = true
  try {
    const response = await fetchBuildTasks(props.projectId)
    records.value = response.data
  } finally {
    loading.value = false
  }
}

async function loadMeta() {
  const [sourceResponse, profileResponse] = await Promise.all([
    fetchSourceVersions(props.projectId),
    fetchBuildProfiles(props.projectId)
  ])
  sourceVersions.value = sourceResponse.data
  buildProfiles.value = profileResponse.data.filter((item) => item.enabled === 1)
  selectedSourceVersionId.value = sourceVersions.value[0]?.id
  selectedBuildProfileId.value = buildProfiles.value.find((item) => item.isDefault === 1)?.id || buildProfiles.value[0]?.id
}

async function handleCreateTask() {
  if (!selectedSourceVersionId.value || !selectedBuildProfileId.value) {
    ElMessage.warning('请选择源码版本和构建配置')
    return
  }
  await createBuildTask({
    projectId: props.projectId,
    sourceVersionId: selectedSourceVersionId.value,
    buildProfileId: selectedBuildProfileId.value
  })
  ElMessage.success('构建任务已创建')
  await loadTasks()
}

async function openLog(id: number) {
  const response = await fetchBuildLog(id)
  logContent.value = response.data
  logVisible.value = true
}

async function handleRetry(id: number) {
  await retryBuildTask(id)
  ElMessage.success('已创建重试任务')
  await loadTasks()
}

async function handleCancel(id: number) {
  await cancelBuildTask(id)
  ElMessage.success('任务已取消')
  await loadTasks()
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
  flex-wrap: wrap;
}
</style>
