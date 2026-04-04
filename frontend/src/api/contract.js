import request from '@/utils/request'

export function getContractList(params) {
  return request.get('/contracts', { params })
}

export function getContractById(id) {
  return request.get(`/contracts/${id}`)
}

export function createContract(data) {
  return request.post('/contracts', data)
}

export function submitContract(id) {
  return request.post(`/contracts/${id}/submit`)
}

export function approveContract(id, comment) {
  return request.post(`/contracts/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectContract(id, comment) {
  return request.post(`/contracts/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function terminateContract(id, reason) {
  return request.post(`/contracts/${id}/terminate?reason=${encodeURIComponent(reason)}`)
}

export function createSupplement(contractId, data) {
  return request.post(`/contracts/${contractId}/supplements`, data)
}
