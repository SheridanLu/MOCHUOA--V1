import request from '@/utils/request'

export function getContactList(params) {
  return request.get('/contacts', { params })
}

export function searchContacts(keyword) {
  return request.get('/contacts/search', { params: { keyword } })
}

export function syncContactFromUser(userId) {
  return request.post(`/contacts/sync/${userId}`)
}

export function hideContactByUser(userId) {
  return request.post(`/contacts/hide/${userId}`)
}
