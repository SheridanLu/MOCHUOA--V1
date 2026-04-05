import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

function generateRequestId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    try {
      return crypto.randomUUID()
    } catch (_) { /* insecure context, fall through */ }
  }
  // Fallback: works in all contexts including HTTP
  const buf = new Uint8Array(16)
  if (typeof crypto !== 'undefined' && crypto.getRandomValues) {
    crypto.getRandomValues(buf)
  } else {
    for (let i = 0; i < 16; i++) buf[i] = (Math.random() * 256) | 0
  }
  buf[6] = (buf[6] & 0x0f) | 0x40
  buf[8] = (buf[8] & 0x3f) | 0x80
  const hex = Array.from(buf, b => b.toString(16).padStart(2, '0')).join('')
  return `${hex.slice(0,8)}-${hex.slice(8,12)}-${hex.slice(12,16)}-${hex.slice(16,20)}-${hex.slice(20)}`
}

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
  config.headers['X-Request-Id'] = generateRequestId()
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
        case 401: {
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push({ name: 'Login' })
          break
        }
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
