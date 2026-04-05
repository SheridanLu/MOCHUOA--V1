import request from '@/utils/request'

export function getCostReport(params) {
  return request.get('/reports/cost', { params })
}

export function getIncomeExpenseReport(params) {
  return request.get('/reports/income-expense', { params })
}

export function getProcurementReport(params) {
  return request.get('/reports/procurement', { params })
}

export function getInventoryReport(params) {
  return request.get('/reports/inventory', { params })
}

export function preComputeReports(period) {
  return request.post(`/reports/pre-compute?period=${encodeURIComponent(period)}`)
}

export function getCachedReports(reportType) {
  return request.get('/reports/cached', { params: { reportType } })
}

export function exportReportExcel(reportType, params) {
  return request.get('/reports/export/excel', { params: { reportType, ...params }, responseType: 'blob' })
}
