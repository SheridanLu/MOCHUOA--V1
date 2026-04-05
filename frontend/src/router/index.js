import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginPage.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/HomePage.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'admin/users',
        name: 'UserList',
        component: () => import('@/views/admin/UserList.vue'),
        meta: { title: '用户管理', permission: 'user:view' }
      },
      {
        path: 'admin/roles',
        name: 'RoleList',
        component: () => import('@/views/admin/RoleList.vue'),
        meta: { title: '角色管理', permission: 'role:create' }
      },
      {
        path: 'admin/departments',
        name: 'DeptTree',
        component: () => import('@/views/admin/DeptTree.vue'),
        meta: { title: '部门管理', permission: 'dept:create' }
      },
      {
        path: 'projects',
        name: 'ProjectList',
        component: () => import('@/views/project/ProjectList.vue'),
        meta: { title: '项目管理', permission: 'project:view' }
      },
      {
        path: 'contracts',
        name: 'ContractList',
        component: () => import('@/views/contract/ContractList.vue'),
        meta: { title: '合同管理', permission: 'contract:view' }
      },
      {
        path: 'contract-templates',
        name: 'TemplateList',
        component: () => import('@/views/contract/TemplateList.vue'),
        meta: { title: '合同模板', permission: 'contract:view' }
      },
      {
        path: 'suppliers',
        name: 'SupplierList',
        component: () => import('@/views/supplier/SupplierList.vue'),
        meta: { title: '供应商管理', permission: 'supplier:view' }
      },
      {
        path: 'contacts',
        name: 'ContactList',
        component: () => import('@/views/contact/ContactList.vue'),
        meta: { title: '通讯录', permission: 'contact:view' }
      },
      {
        path: 'purchases',
        name: 'PurchaseList',
        component: () => import('@/views/purchase/PurchaseList.vue'),
        meta: { title: '采购管理', permission: 'purchase:view' }
      },
      {
        path: 'materials',
        name: 'MaterialPage',
        component: () => import('@/views/material/MaterialPage.vue'),
        meta: { title: '物资管理', permission: 'material:view' }
      },
      {
        path: 'hr',
        name: 'HrPage',
        component: () => import('@/views/hr/HrPage.vue'),
        meta: { title: '人力资源', permission: 'hr:view' }
      },
      {
        path: 'progress',
        name: 'ProgressPage',
        component: () => import('@/views/progress/ProgressPage.vue'),
        meta: { title: '施工进度', permission: 'progress:view' }
      },
      {
        path: 'changes',
        name: 'ChangePage',
        component: () => import('@/views/change/ChangePage.vue'),
        meta: { title: '变更管理', permission: 'change:view' }
      },
      {
        path: 'finance',
        name: 'FinancePage',
        component: () => import('@/views/finance/FinancePage.vue'),
        meta: { title: '财务管理', permission: 'finance:view' }
      },
      {
        path: 'completion',
        name: 'CompletionPage',
        component: () => import('@/views/completion/CompletionPage.vue'),
        meta: { title: '竣工管理', permission: 'completion:view' }
      },
      {
        path: 'announcements',
        name: 'AnnouncementPage',
        component: () => import('@/views/announcement/AnnouncementPage.vue'),
        meta: { title: '通知公告', permission: 'announcement:view' }
      },
      {
        path: 'showcases',
        name: 'ShowcasePage',
        component: () => import('@/views/showcase/ShowcasePage.vue'),
        meta: { title: '案例展示', permission: 'showcase:view' }
      },
      {
        path: 'reports',
        name: 'ReportPage',
        component: () => import('@/views/report/ReportPage.vue'),
        meta: { title: '报表统计', permission: 'report:view' }
      },
      {
        path: 'audit-logs',
        name: 'AuditLogPage',
        component: () => import('@/views/audit/AuditLogPage.vue'),
        meta: { title: '审计日志', permission: 'audit:view' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/common/NotFound.vue'),
    meta: { title: '404', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

NProgress.configure({ showSpinner: false })

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  document.title = to.meta.title ? `${to.meta.title} - MOCHU-OA` : 'MOCHU-OA'

  const userStore = useUserStore()

  // Redirect authenticated users away from login
  if (to.name === 'Login' && userStore.token) {
    next({ path: '/' })
    return
  }

  // Check if route requires auth
  if (to.meta.requiresAuth !== false) {
    if (!userStore.token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // Re-fetch user info if token exists but userInfo is lost (page refresh)
    if (!userStore.userInfo) {
      const success = await userStore.fetchUserInfo()
      if (!success) {
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
    }

    // Check route-level permission
    if (to.meta.permission && !userStore.hasPermission(to.meta.permission)) {
      ElMessage.error('您没有权限访问该页面')
      next({ path: '/home' })
      return
    }
  }
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
