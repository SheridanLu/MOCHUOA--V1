import request from '@/utils/request'

export function getEmployees(params) {
  return request.get('/hr/employees', { params })
}

export function getEmployeeById(id) {
  return request.get(`/hr/employees/${id}`)
}

export function onboardEmployee(data) {
  return request.post('/hr/employees/onboard', data)
}

export function offboardEmployee(id) {
  return request.post(`/hr/employees/${id}/offboard`)
}

export function getPayrolls(params) {
  return request.get('/hr/payrolls', { params })
}

export function generatePayroll(period) {
  return request.post(`/hr/payrolls/generate?period=${encodeURIComponent(period)}`)
}

export function adjustPayroll(id, data) {
  return request.put(`/hr/payrolls/${id}/adjust`, data)
}

export function approvePayroll(id, comment) {
  return request.post(`/hr/payrolls/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function markPayrollPaid(id) {
  return request.post(`/hr/payrolls/${id}/paid`)
}

export function getReimbursements(params) {
  return request.get('/hr/reimbursements', { params })
}

export function createReimbursement(data) {
  return request.post('/hr/reimbursements', data)
}

export function approveReimbursement(id, comment) {
  return request.post(`/hr/reimbursements/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectReimbursement(id, comment) {
  return request.post(`/hr/reimbursements/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function getHrContracts(params) {
  return request.get('/hr/contracts', { params })
}

export function createHrContract(data) {
  return request.post('/hr/contracts', data)
}

export function renewHrContract(id, data) {
  return request.post(`/hr/contracts/${id}/renew`, data)
}

export function getExpiringContracts(days) {
  return request.get('/hr/contracts/expiring', { params: { days } })
}

export function getQualifications(params) {
  return request.get('/hr/qualifications', { params })
}

export function createQualification(data) {
  return request.post('/hr/qualifications', { params: data })
}

export function getExpiringQualifications(days) {
  return request.get('/hr/qualifications/expiring', { params: { days } })
}
