import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

function generateRequestId() {
  try {
    if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
      return crypto.randomUUID()
    }
  } catch (_) { /* insecure context */ }
  try {
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
  } catch (_) {
    return Date.now().toString(36) + Math.random().toString(36).slice(2, 10)
  }
}

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器 — wrapped in try/catch to prevent silent failures
request.interceptors.request.use(
  config => {
    try {
      const userStore = useUserStore()
      if (userStore.token) {
        config.headers.Authorization = `Bearer ${userStore.token}`
      }
    } catch (e) {
      console.warn('[request] useUserStore failed:', e)
    }
    config.headers['X-Request-Id'] = generateRequestId()
    config.headers['X-Client-Type'] = 'pc'
    config.headers['X-Timestamp'] = Date.now().toString()
    return config
  },
  error => {
    console.error('[request interceptor error]', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 检查是否有新 Token
    try {
      const newToken = response.headers['x-new-token']
      if (newToken) {
        const userStore = useUserStore()
        userStore.setToken(newToken)
      }
    } catch (_) { /* ignore token refresh failure */ }

    const res = response.data
    if (res.code !== 200) {
      // Don't show ElMessage here — let the caller handle it via catch
      // This avoids double-display of error messages
      const err = new Error(res.message || '请求失败')
      err._handled = false
      err._bizCode = res.code
      return Promise.reject(err)
    }
    return res
  },
  error => {
    const { response } = error
    if (response) {
      switch (response.status) {
        case 401: {
          ElMessage.error('登录已过期，请重新登录')
          try {
            const userStore = useUserStore()
            userStore.logout()
          } catch (_) {}
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
          // Don't show here — let caller show
          break
      }
    }
    return Promise.reject(error)
  }
)

export default request
