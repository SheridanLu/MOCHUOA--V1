import request from '@/utils/request'

export function getUserList(params) {
  return request.get('/users', { params })
}

export function getUserById(id) {
  return request.get(`/users/${id}`)
}

export function createUser(data) {
  return request.post('/users', data)
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

export function updateUserStatus(id, status) {
  return request.patch(`/users/${id}/status?status=${status}`)
}

export function resetUserPassword(id) {
  return request.post(`/users/${id}/reset-password`)
}
