import request from '@/utils/request'

export function getAuditLogs(params) {
  return request.get('/audit-logs', { params })
}

export function exportAuditExcel(params) {
  return request.get('/audit-logs/export/excel', { params, responseType: 'blob' })
}

export function exportAuditJson(params) {
  return request.get('/audit-logs/export/json', { params, responseType: 'blob' })
}
