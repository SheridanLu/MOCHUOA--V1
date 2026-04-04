import request from '@/utils/request'

export function getSupplierList(params) {
  return request.get('/suppliers', { params })
}

export function getSupplierById(id) {
  return request.get(`/suppliers/${id}`)
}

export function getEnabledSuppliers() {
  return request.get('/suppliers/enabled')
}

export function createSupplier(data) {
  return request.post('/suppliers', data)
}

export function updateSupplier(id, data) {
  return request.put(`/suppliers/${id}`, data)
}

export function updateSupplierStatus(id, status) {
  return request.patch(`/suppliers/${id}/status?status=${status}`)
}
