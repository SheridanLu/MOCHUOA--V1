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
          <el-link type="primary" @click="step = 1">返回</el-link>
          <el-link type="primary">忘记密码</el-link>
        </div>
      </div>

      <!-- 第二步：验证码登录 -->
      <div v-if="step === 2 && loginType === 'sms'">
        <p class="account-info">手机号: {{ account }}</p>
        <el-input v-model="smsCode" placeholder="请输入验证码" size="large" @keyup.enter="loginBySms">
          <template #prefix><el-icon><Message /></el-icon></template>
          <template #append>
            <el-button :disabled="countdown > 0" @click="sendSms">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </template>
        </el-input>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="loginBySms">
          登录
        </el-button>
        <div class="login-links">
          <el-link type="primary" @click="step = 1">返回</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const step = ref(1)
const loginType = ref('password')
const account = ref('')
const password = ref('')
const smsCode = ref('')
const loading = ref(false)
const countdown = ref(0)

const checkAccount = async () => {
  if (!account.value) {
    ElMessage.warning('请输入用户名或手机号')
    return
  }
  // TODO: 调用 check-account 接口
  // 临时逻辑：手机号格式进入验证码登录
  const isPhone = /^1\d{10}$/.test(account.value)
  loginType.value = isPhone ? 'sms' : 'password'
  step.value = 2
}

const loginByPassword = async () => {
  if (!password.value) {
    ElMessage.warning('请输入密码')
    return
  }
  loading.value = true
  try {
    // TODO: 调用 login-by-password 接口
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}

const sendSms = async () => {
  // TODO: 调用 send-sms 接口
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

const loginBySms = async () => {
  if (!smsCode.value) {
    ElMessage.warning('请输入验证码')
    return
  }
  loading.value = true
  try {
    // TODO: 调用 login-by-sms 接口
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
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
