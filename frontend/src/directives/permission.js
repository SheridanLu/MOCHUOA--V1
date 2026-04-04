/**
 * 权限指令 v-permission
 * 用法: v-permission="'project:create'" 或 v-permission="['project:create', 'project:edit']"
 */
import { useUserStore } from '@/stores/user'

export const permissionDirective = {
  mounted(el, binding) {
    const { value } = binding
    const userStore = useUserStore()

    if (value) {
      const perms = Array.isArray(value) ? value : [value]
      const hasPermission = perms.some(perm => userStore.hasPermission(perm))
      if (!hasPermission) {
        el.parentNode?.removeChild(el)
      }
    }
  }
}
