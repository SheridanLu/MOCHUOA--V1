import request from '@/utils/request'

export function getHomeData() {
  return request.get('/home')
}
