import request from '@/utils/request'

export function getRoleList() {
  return request.get('/roles')
}

export function getRoleById(id) {
  return request.get(`/roles/${id}`)
}

export function createRole(data) {
  return request.post('/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/roles/${id}`, data)
}

export function assignRoles(userId, roleIds) {
  return request.post(`/roles/assign/${userId}`, roleIds)
}
