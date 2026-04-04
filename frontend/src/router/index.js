import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

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
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/common/NotFound.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

NProgress.configure({ showSpinner: false })

router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = to.meta.title ? `${to.meta.title} - MOCHU-OA` : 'MOCHU-OA'

  if (to.meta.requiresAuth !== false) {
    const userStore = useUserStore()
    if (!userStore.token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
