<template>
  <div class="layout-shell">
    <aside class="layout-sidebar">
      <div class="brand">VibeProto</div>
      <el-menu
        :default-active="route.path"
        class="menu"
        router
      >
        <template v-if="userStore.isViewer">
          <el-menu-item index="/viewer/projects">原型项目</el-menu-item>
        </template>
        <template v-else>
          <el-menu-item index="/projects">项目管理</el-menu-item>
          <el-menu-item index="/build-tasks">构建任务</el-menu-item>
          <el-menu-item index="/system">系统设置</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/users">用户管理</el-menu-item>
        </template>
      </el-menu>
    </aside>
    <section class="layout-main">
      <header class="layout-header">
        <div class="header-title">{{ pageTitle }}</div>
        <div class="header-actions">
          <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '未登录' }}</span>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </header>
      <main class="layout-content">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitle = computed(() => (route.meta.title as string) || 'VibeCoding 原型管理系统')

function handleLogout() {
  userStore.clearSession()
  router.push('/login')
}
</script>

<style scoped>
.layout-shell {
  display: grid;
  grid-template-columns: 240px 1fr;
  min-height: 100vh;
}

.layout-sidebar {
  padding: 24px 16px;
  background: linear-gradient(180deg, #134e4a 0%, #0f766e 100%);
  color: #fff;
}

.brand {
  margin-bottom: 24px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.menu {
  border-right: none;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.88);
  border-radius: 12px;
}

.menu :deep(.el-menu-item.is-active) {
  color: #134e4a;
  background: #ecfdf5;
}

.layout-main {
  display: flex;
  flex-direction: column;
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 28px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(15, 118, 110, 0.12);
}

.header-title {
  font-size: 20px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: #4b5563;
}

.layout-content {
  padding: 24px 28px;
}

@media (max-width: 960px) {
  .layout-shell {
    grid-template-columns: 1fr;
  }

  .layout-sidebar {
    padding-bottom: 8px;
  }
}
</style>
