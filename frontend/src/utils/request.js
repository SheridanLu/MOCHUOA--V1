import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import { v4 as uuidv4 } from 'uuid'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器
request.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  config.headers['X-Request-Id'] = uuidv4()
  config.headers['X-Client-Type'] = 'pc'
  config.headers['X-Timestamp'] = Date.now().toString()
  return config
})

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 检查是否有新 Token
    const newToken = response.headers['x-new-token']
    if (newToken) {
      const userStore = useUserStore()
      userStore.setToken(newToken)
    }
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    const { response } = error
    if (response) {
      switch (response.status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push({ name: 'Login' })
          break
        case 403:
          ElMessage.error('您没有权限执行此操作')
          break
        case 429:
          ElMessage.error('操作过于频繁，请稍后重试')
          break
        default:
          ElMessage.error(response.data?.message || '系统繁忙，请稍后重试')
      }
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
