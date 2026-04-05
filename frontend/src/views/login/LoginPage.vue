<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">MOCHU-OA 施工管理系统</h1>

      <!-- 第一步：输入账号 -->
      <div v-if="step === 1">
        <el-input
          v-model="account"
          placeholder="请输入用户名或手机号"
          size="large"
          @keyup.enter="checkAccount"
        >
          <template #prefix><el-icon><User /></el-icon></template>
        </el-input>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="checkAccount">
          下一步
        </el-button>
      </div>

      <!-- 第二步：密码登录 -->
      <div v-if="step === 2 && loginType === 'password'">
        <p class="account-info">账号: {{ account }}</p>
        <el-input
          v-model="password"
          type="password"
          placeholder="请输入密码"
          size="large"
          show-password
          @keyup.enter="loginByPassword"
        >
          <template #prefix><el-icon><Lock /></el-icon></template>
        </el-input>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="loginByPassword">
          登录
        </el-button>
        <div class="login-links">
          <el-link type="primary" @click="goBack">返回</el-link>
          <el-link type="primary">忘记密码</el-link>
        </div>
      </div>

      <!-- 第二步：验证码登录 -->
      <div v-if="step === 2 && loginType === 'sms'">
        <p class="account-info">手机号: {{ account }}</p>
        <el-input v-model="smsCode" placeholder="请输入验证码" size="large" @keyup.enter="loginBySms">
          <template #prefix><el-icon><Message /></el-icon></template>
          <template #append>
            <el-button :disabled="countdown > 0 || smsLoading" @click="sendSms">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </template>
        </el-input>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="loginBySms">
          登录
        </el-button>
        <div class="login-links">
          <el-link type="primary" @click="goBack">返回</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { checkAccount as checkAccountApi, loginByPassword as loginByPasswordApi, loginBySms as loginBySmsApi, sendSmsCode as sendSmsCodeApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const step = ref(1)
const loginType = ref('password')
const account = ref('')
const password = ref('')
const smsCode = ref('')
const loading = ref(false)
const smsLoading = ref(false)
const countdown = ref(0)
let countdownTimer = null

const goBack = () => {
  step.value = 1
  password.value = ''
  smsCode.value = ''
}

const showError = (e) => {
  const msg = e?.response?.data?.message || e?.message || '网络请求异常，请重试'
  if (msg) ElMessage.error(msg)
}

const checkAccount = async () => {
  const trimmed = account.value.trim()
  if (!trimmed) {
    ElMessage.warning('请输入用户名或手机号')
    return
  }
  account.value = trimmed
  loading.value = true
  try {
    const res = await checkAccountApi({ account: trimmed })
    if (!res.data.exists) {
      ElMessage.error('账号不存在')
      return
    }
    loginType.value = res.data.loginType
    step.value = 2
  } catch (e) {
    console.error('[checkAccount]', e)
    showError(e)
  } finally {
    loading.value = false
  }
}

const handleLoginSuccess = (res) => {
  userStore.setToken(res.data.token)
  userStore.setUserInfo(res.data.userInfo)
  userStore.setPermissions(res.data.permissions ? [...res.data.permissions] : [])
  ElMessage.success('登录成功')
  const redirect = router.currentRoute.value.query.redirect
  if (redirect && redirect.startsWith('/') && !redirect.startsWith('//')) {
    router.push(redirect)
  } else {
    router.push('/home')
  }
}

const loginByPassword = async () => {
  if (!password.value) {
    ElMessage.warning('请输入密码')
    return
  }
  loading.value = true
  try {
    const res = await loginByPasswordApi({ account: account.value, password: password.value })
    handleLoginSuccess(res)
  } catch (e) {
    console.error('[loginByPassword]', e)
    showError(e)
  } finally {
    loading.value = false
  }
}

const sendSms = async () => {
  smsLoading.value = true
  try {
    await sendSmsCodeApi(account.value)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  } catch (e) {
    console.error('[sendSms]', e)
    showError(e)
  } finally {
    smsLoading.value = false
  }
}

const loginBySms = async () => {
  if (!smsCode.value) {
    ElMessage.warning('请输入验证码')
    return
  }
  loading.value = true
  try {
    const res = await loginBySmsApi({ account: account.value, smsCode: smsCode.value })
    handleLoginSuccess(res)
  } catch (e) {
    console.error('[loginBySms]', e)
    showError(e)
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.login-title {
  text-align: center;
  margin-bottom: 32px;
  color: #303133;
  font-size: 22px;
}
.login-btn {
  width: 100%;
  margin-top: 20px;
}
.account-info {
  color: #606266;
  margin-bottom: 16px;
  font-size: 14px;
}
.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
}
</style>
