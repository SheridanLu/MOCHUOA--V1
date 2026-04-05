import request from '@/utils/request'

// Announcements
export function getAnnouncements(params) {
  return request.get('/announcements', { params })
}

export function createAnnouncement(data) {
  return request.post('/announcements', data)
}

export function publishAnnouncement(id) {
  return request.post(`/announcements/${id}/publish`)
}

export function offlineAnnouncement(id) {
  return request.post(`/announcements/${id}/offline`)
}

// Notifications
export function getMyNotifications(params) {
  return request.get('/announcements/notifications', { params })
}

export function markNotificationRead(id) {
  return request.post(`/announcements/notifications/${id}/read`)
}

export function markAllRead() {
  return request.post('/announcements/notifications/read-all')
}

export function getUnreadCount() {
  return request.get('/announcements/notifications/unread-count')
}
