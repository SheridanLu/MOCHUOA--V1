import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'

export const useUserStore = defineStore('user', () => {
  const token = ref(Cookies.get('mochu_token') || '')
  const userInfo = ref(null)
  const permissions = ref([])

  const isLoggedIn = computed(() => !!token.value)

  function setToken(newToken) {
    token.value = newToken
    Cookies.set('mochu_token', newToken, { expires: 30 })
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  function setPermissions(perms) {
    permissions.value = perms
  }

  function hasPermission(perm) {
    return permissions.value.includes(perm) || permissions.value.includes('*')
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    Cookies.remove('mochu_token')
  }

  return {
    token, userInfo, permissions, isLoggedIn,
    setToken, setUserInfo, setPermissions, hasPermission, logout
  }
})
