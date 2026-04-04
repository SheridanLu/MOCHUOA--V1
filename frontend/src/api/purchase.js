import request from '@/utils/request'

export function getPurchaseList(params) {
  return request.get('/purchases', { params })
}

export function getPurchaseById(id) {
  return request.get(`/purchases/${id}`)
}

export function createPurchase(data) {
  return request.post('/purchases', data)
}

export function submitPurchase(id) {
  return request.post(`/purchases/${id}/submit`)
}

export function approvePurchase(id, comment) {
  return request.post(`/purchases/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function rejectPurchase(id, comment) {
  return request.post(`/purchases/${id}/reject?comment=${encodeURIComponent(comment)}`)
}

export function requestPurchaseChange(id) {
  return request.post(`/purchases/${id}/change`)
}

export function getBenchmarkPrices(keyword) {
  return request.get('/purchases/benchmark-prices', { params: { keyword } })
}

export function createBenchmarkPrice(data) {
  return request.post('/purchases/benchmark-prices', data)
}

export function submitBenchmarkPrice(id) {
  return request.post(`/purchases/benchmark-prices/${id}/submit`)
}

export function approveBenchmarkPrice(id, comment) {
  return request.post(`/purchases/benchmark-prices/${id}/approve?comment=${encodeURIComponent(comment)}`)
}

export function getPriceHistory(materialName, spec) {
  return request.get('/purchases/price-history', { params: { materialName, spec } })
}
