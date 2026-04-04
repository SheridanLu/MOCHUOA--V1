import request from '@/utils/request'

export function getPlans(params) {
  return request.get('/progress/plans', { params })
}

export function createPlan(data) {
  return request.post('/progress/plans', data)
}

export function approvePlan(id, comment) {
  return request.post(`/progress/plans/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function getGanttTasks(planId) {
  return request.get('/progress/tasks', { params: { planId } })
}

export function createTask(data) {
  return request.post('/progress/tasks', data)
}

export function updateProgress(data) {
  return request.post('/progress/progress/update', data)
}

export function batchUpdateProgress(data) {
  return request.post('/progress/progress/batch-update', data)
}

export function getDeviations(params) {
  return request.get('/progress/deviations', { params })
}

export function scanDeviations(projectId) {
  return request.post(`/progress/deviations/scan?projectId=${projectId}`)
}
