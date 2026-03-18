import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { useUserStore } from '@/stores/userStore'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/',
      component: DefaultLayout,
      children: [
        {
          path: '',
          redirect: '/projects'
        },
        {
          path: '/projects',
          name: 'projects',
          component: () => import('@/views/project/ProjectListView.vue'),
          meta: { title: '项目管理', requiresAuth: true }
        },
        {
          path: '/projects/:id',
          name: 'project-detail',
          component: () => import('@/views/project/ProjectDetailView.vue'),
          meta: { title: '项目详情', requiresAuth: true }
        },
        {
          path: '/build-tasks',
          name: 'build-tasks',
          component: () => import('@/views/build/BuildTaskListView.vue'),
          meta: { title: '构建任务', requiresAuth: true }
        },
        {
          path: '/system',
          name: 'system',
          component: () => import('@/views/system/SystemSettingsView.vue'),
          meta: { title: '系统设置', requiresAuth: true }
        },
        {
          path: '/users',
          name: 'users',
          component: () => import('@/views/user/UserManageView.vue'),
          meta: { title: '用户管理', requiresAuth: true }
        },
        {
          path: '/viewer/projects',
          name: 'viewer-projects',
          component: () => import('@/views/viewer/ViewerProjectListView.vue'),
          meta: { title: '原型项目', requiresAuth: true }
        },
        {
          path: '/viewer/projects/:id',
          name: 'viewer-project-detail',
          component: () => import('@/views/viewer/ViewerProjectDetailView.vue'),
          meta: { title: '项目版本', requiresAuth: true }
        }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  if (to.path === '/login' && userStore.isLoggedIn) {
    return '/projects'
  }

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return '/login'
  }

  if (to.meta.requiresAuth && userStore.isLoggedIn && !userStore.userInfo) {
    try {
      await userStore.fetchProfile()
    } catch {
      return '/login'
    }
  }

  // Role-based routing
  if (to.meta.requiresAuth && userStore.isLoggedIn) {
    const isViewer = userStore.isViewer
    const path = to.path

    if (isViewer && !path.startsWith('/viewer') && path !== '/login') {
      return '/viewer/projects'
    }

    if (!isViewer && path.startsWith('/viewer')) {
      return '/projects'
    }
  }

  return true
})

export default router
