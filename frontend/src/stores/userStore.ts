import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { fetchCurrentUser, login, type LoginPayload } from '@/api/auth'
import type { UserInfo } from '@/types/user'

const TOKEN_KEY = 'vibeproto-token'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => Boolean(token.value))

  const roleCode = computed(() => userInfo.value?.roleCode ?? null)
  const isAdmin = computed(() => roleCode.value === 'SUPER_ADMIN')
  const canManage = computed(() => roleCode.value === 'SUPER_ADMIN' || roleCode.value === 'PRODUCT_MANAGER')
  const isViewer = computed(() => !isAdmin.value && !canManage.value)

  function setSession(nextToken: string, nextUser: UserInfo) {
    token.value = nextToken
    userInfo.value = nextUser
    localStorage.setItem(TOKEN_KEY, nextToken)
  }

  function clearSession() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  async function loginAction(payload: LoginPayload) {
    const response = await login(payload)
    setSession(response.data.token, response.data.userInfo)
  }

  async function fetchProfile() {
    if (!token.value) {
      return
    }
    const response = await fetchCurrentUser()
    userInfo.value = response.data
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    roleCode,
    isAdmin,
    canManage,
    isViewer,
    setSession,
    clearSession,
    loginAction,
    fetchProfile
  }
})
