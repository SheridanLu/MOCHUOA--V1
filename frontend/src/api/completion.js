import request from '@/utils/request'

// Completion Report
export function getCompletionReports(params) {
  return request.get('/completion/reports', { params })
}

export function createCompletionReport(data) {
  return request.post('/completion/reports', data)
}

export function approveReport(id, comment) {
  return request.post(`/completion/reports/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectReport(id, comment) {
  return request.post(`/completion/reports/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

// Drawings
export function getDrawings(projectId) {
  return request.get('/completion/drawings', { params: { projectId } })
}

export function uploadDrawings(data) {
  return request.post('/completion/drawings', data)
}

export function uploadDrawingVersion(id, fileUrl) {
  return request.post(`/completion/drawings/${id}/version?fileUrl=${encodeURIComponent(fileUrl)}`)
}

export function approveDrawing(id, comment) {
  return request.post(`/completion/drawings/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectDrawing(id, comment) {
  return request.post(`/completion/drawings/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

// Archive
export function getArchive(projectId) {
  return request.get('/completion/archive', { params: { projectId } })
}

// Settlement
export function getSettlement(projectId) {
  return request.get('/completion/settlement', { params: { projectId } })
}

export function generateSettlement(projectId) {
  return request.post(`/completion/settlement/generate?projectId=${projectId}`)
}

// Labor Settlement
export function getLaborSettlements(params) {
  return request.get('/completion/labor-settlements', { params })
}

export function createLaborSettlement(data) {
  return request.post('/completion/labor-settlements', data)
}

export function approveLaborSettlement(id, comment) {
  return request.post(`/completion/labor-settlements/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectLaborSettlement(id, comment) {
  return request.post(`/completion/labor-settlements/${id}/reject?comment=${encodeURIComponent(comment)}`)
}
