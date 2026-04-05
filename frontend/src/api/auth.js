import request from '@/utils/request'

export function checkAccount(data) {
  return request.post('/auth/check-account', data)
}

export function loginByPassword(data) {
  return request.post('/auth/login/password', data)
}

export function loginBySms(data) {
  return request.post('/auth/login/sms', data)
}

export function sendSmsCode(phone) {
  return request.post(`/auth/sms/send?phone=${phone}`)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getUserInfo() {
  return request.get('/auth/me')
}

export function resetPassword(data) {
  return request.post(`/auth/reset-password?account=${data.account}&smsCode=${data.smsCode}&newPassword=${data.newPassword}`)
}
