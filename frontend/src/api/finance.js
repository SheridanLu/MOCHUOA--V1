import request from '@/utils/request'

// Income Split
export function getIncomeSplits(params) {
  return request.get('/finance/income-splits', { params })
}

export function createIncomeSplit(data) {
  return request.post('/finance/income-splits', data)
}

export function approveIncomeSplit(id, comment) {
  return request.post(`/finance/income-splits/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectIncomeSplit(id, comment) {
  return request.post(`/finance/income-splits/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

// Reconciliation
export function getReconciliations(params) {
  return request.get('/finance/reconciliations', { params })
}

export function generateReconciliation(period) {
  return request.post(`/finance/reconciliations/generate?period=${encodeURIComponent(period)}`)
}

export function approveReconciliation(id, comment) {
  return request.post(`/finance/reconciliations/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectReconciliation(id, comment) {
  return request.post(`/finance/reconciliations/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

// Receipt
export function registerReceipt(data) {
  return request.post('/finance/receipts', data)
}

// Payment
export function getPayments(params) {
  return request.get('/finance/payments', { params })
}

export function createPayment(data) {
  return request.post('/finance/payments', data)
}

export function approvePayment(id, comment) {
  return request.post(`/finance/payments/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectPayment(id, comment) {
  return request.post(`/finance/payments/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function markPaymentPaid(id) {
  return request.post(`/finance/payments/${id}/paid`)
}

// Invoice
export function getInvoices(params) {
  return request.get('/finance/invoices', { params })
}

export function createInvoice(data) {
  return request.post('/finance/invoices', data)
}

export function getExpiringInvoices(days) {
  return request.get('/finance/invoices/expiring', { params: { days } })
}

// Cost
export function getCosts(projectId, period) {
  return request.get('/finance/costs', { params: { projectId, period } })
}

export function aggregateCosts(projectId, period) {
  return request.post(`/finance/costs/aggregate?projectId=${projectId}&period=${encodeURIComponent(period)}`)
}
