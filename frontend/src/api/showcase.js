import request from '@/utils/request'

export function getShowcases(params) {
  return request.get('/showcases', { params })
}

export function getShowcaseDetail(id) {
  return request.get(`/showcases/${id}`)
}

export function createShowcase(data) {
  return request.post('/showcases', data)
}

export function submitShowcase(id) {
  return request.post(`/showcases/${id}/submit`)
}

export function approveShowcase(id, comment) {
  return request.post(`/showcases/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectShowcase(id, comment) {
  return request.post(`/showcases/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function setShowcaseVisibility(id, visibility) {
  return request.patch(`/showcases/${id}/visibility?visibility=${visibility}`)
}
