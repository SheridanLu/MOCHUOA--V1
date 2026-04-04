import request from '@/utils/request'

export function getInbounds(params) {
  return request.get('/materials/inbounds', { params })
}

export function getInboundById(id) {
  return request.get(`/materials/inbounds/${id}`)
}

export function createInbound(data) {
  return request.post('/materials/inbounds', data)
}

export function submitInbound(id) {
  return request.post(`/materials/inbounds/${id}/submit`)
}

export function approveInbound(id, comment) {
  return request.post(`/materials/inbounds/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectInbound(id, comment) {
  return request.post(`/materials/inbounds/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function getOutbounds(params) {
  return request.get('/materials/outbounds', { params })
}

export function getOutboundById(id) {
  return request.get(`/materials/outbounds/${id}`)
}

export function createOutbound(data) {
  return request.post('/materials/outbounds', data)
}

export function submitOutbound(id) {
  return request.post(`/materials/outbounds/${id}/submit`)
}

export function approveOutbound(id, comment) {
  return request.post(`/materials/outbounds/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectOutbound(id, comment) {
  return request.post(`/materials/outbounds/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function getReturns(params) {
  return request.get('/materials/returns', { params })
}

export function createReturn(data) {
  return request.post('/materials/returns', data)
}

export function approveReturn(id, comment) {
  return request.post(`/materials/returns/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function getInventory(params) {
  return request.get('/materials/inventory', { params })
}
