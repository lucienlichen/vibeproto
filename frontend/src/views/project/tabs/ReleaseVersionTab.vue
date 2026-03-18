<template>
  <section class="tab-section">
    <PreviewInfoCard :latest-url="latestUrl" />

    <el-table v-loading="releaseStore.loading" :data="releaseStore.releases">
      <el-table-column prop="versionNo" label="版本号" width="120" />
      <el-table-column prop="previewUrl" label="预览地址" min-width="260" />
      <el-table-column label="当前版本" width="100">
        <template #default="{ row }">{{ row.isCurrent === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="发布时间" min-width="180" />
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleSetCurrent(row.id)">设为当前</el-button>
          <el-button link type="warning" @click="handleRollback(row.id)">回滚到此</el-button>
          <el-button link type="danger" :disabled="row.isCurrent === 1" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted } from 'vue'
import PreviewInfoCard from '@/components/common/PreviewInfoCard.vue'
import { useReleaseStore } from '@/stores/releaseStore'

const props = defineProps<{ projectId: number; latestUrl: string }>()
const releaseStore = useReleaseStore()

const latestUrl = computed(() => props.latestUrl)

onMounted(() => {
  releaseStore.loadReleases(props.projectId)
})

async function handleSetCurrent(id: number) {
  await releaseStore.setCurrentAction(id, props.projectId)
  ElMessage.success('当前发布版本已更新')
}

async function handleRollback(id: number) {
  await releaseStore.rollbackAction(id, props.projectId)
  ElMessage.success('已回滚到目标版本')
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('删除后该发布版本不可恢复，是否继续？', '删除发布版本', { type: 'warning' })
  } catch {
    return
  }
  await releaseStore.deleteAction(id, props.projectId)
  ElMessage.success('发布版本已删除')
}
</script>

<style scoped>
.tab-section {
  display: grid;
  gap: 16px;
}
</style>
