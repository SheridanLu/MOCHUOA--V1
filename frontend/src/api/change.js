import request from '@/utils/request'

export function createSiteVisa(data) {
  return request.post('/changes/site-visas', data)
}

export function approveSiteVisa(id, comment) {
  return request.post(`/changes/site-visas/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectSiteVisa(id, comment) {
  return request.post(`/changes/site-visas/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function createOwnerChange(data) {
  return request.post('/changes/owner-changes', data)
}

export function approveOwnerChange(id, comment) {
  return request.post(`/changes/owner-changes/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectOwnerChange(id, comment) {
  return request.post(`/changes/owner-changes/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function createLaborVisa(data) {
  return request.post('/changes/labor-visas', data)
}

export function approveLaborVisa(id, comment) {
  return request.post(`/changes/labor-visas/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectLaborVisa(id, comment) {
  return request.post(`/changes/labor-visas/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function getChangeLedger(params) {
  return request.get('/changes/ledger', { params })
}
