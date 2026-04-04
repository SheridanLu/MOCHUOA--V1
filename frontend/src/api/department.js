import request from '@/utils/request'

export function getDeptTree() {
  return request.get('/departments/tree')
}

export function createDept(data) {
  return request.post('/departments', data)
}

export function updateDept(id, data) {
  return request.put(`/departments/${id}`, data)
}

export function updateDeptStatus(id, status) {
  return request.patch(`/departments/${id}/status?status=${status}`)
}
