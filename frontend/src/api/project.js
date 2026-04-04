import request from '@/utils/request'

export function getProjectList(params) {
  return request.get('/projects', { params })
}

export function getProjectById(id) {
  return request.get(`/projects/${id}`)
}

export function createProject(data) {
  return request.post('/projects', data)
}

export function updateProject(id, data) {
  return request.put(`/projects/${id}`, data)
}

export function submitProject(id) {
  return request.post(`/projects/${id}/submit`)
}

export function approveProject(id, comment) {
  return request.post(`/projects/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectProject(id, comment) {
  return request.post(`/projects/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function pauseProject(id) {
  return request.post(`/projects/${id}/pause`)
}

export function resumeProject(id) {
  return request.post(`/projects/${id}/resume`)
}

export function closeProject(id) {
  return request.post(`/projects/${id}/close`)
}

export function terminateProject(id, reason) {
  return request.post(`/projects/${id}/terminate?reason=${encodeURIComponent(reason)}`)
}

export function convertProject(id) {
  return request.post(`/projects/${id}/convert`)
}
