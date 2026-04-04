import request from '@/utils/request'

export function getTemplateList(params) {
  return request.get('/contract-templates', { params })
}

export function getTemplateById(id) {
  return request.get(`/contract-templates/${id}`)
}

export function createTemplate(data) {
  return request.post('/contract-templates', data)
}

export function updateTemplate(id, data) {
  return request.put(`/contract-templates/${id}`, data)
}

export function submitTemplate(id) {
  return request.post(`/contract-templates/${id}/submit`)
}

export function approveTemplate(id, comment) {
  return request.post(`/contract-templates/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectTemplate(id, comment) {
  return request.post(`/contract-templates/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function renderTemplate(id) {
  return request.get(`/contract-templates/${id}/render`)
}
