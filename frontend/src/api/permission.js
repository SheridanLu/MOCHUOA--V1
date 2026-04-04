import request from '@/utils/request'

export function getPermissionList() {
  return request.get('/permissions')
}
