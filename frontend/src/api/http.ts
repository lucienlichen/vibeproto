import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'
import { useUserStore } from '@/stores/userStore'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResponse<unknown>
    if (payload.code !== 0) {
      ElMessage.error(payload.message || '请求失败')
      return Promise.reject(new Error(payload.message || 'Request failed'))
    }
    return payload
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.clearSession()
      router.push('/login')
      ElMessage.error('登录已失效，请重新登录')
    } else {
      ElMessage.error(error.message || '网络请求失败')
    }
    return Promise.reject(error)
  }
)

export default http
