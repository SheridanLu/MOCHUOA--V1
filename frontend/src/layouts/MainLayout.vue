<template>
  <el-container class="main-layout">
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-show="!isCollapsed">MOCHU-OA</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="isCollapsed"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-sub-menu index="/admin" v-if="userStore.hasPermission('user:view') || userStore.hasPermission('role:create') || userStore.hasPermission('dept:create')">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/users" v-if="userStore.hasPermission('user:view')">用户管理</el-menu-item>
          <el-menu-item index="/admin/roles" v-if="userStore.hasPermission('role:create')">角色管理</el-menu-item>
          <el-menu-item index="/admin/departments" v-if="userStore.hasPermission('dept:create')">部门管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/projects" v-if="userStore.hasPermission('project:view')">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-sub-menu index="/contract-group" v-if="userStore.hasPermission('contract:view')">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>合同管理</span>
          </template>
          <el-menu-item index="/contracts">合同列表</el-menu-item>
          <el-menu-item index="/contract-templates">合同模板</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/suppliers" v-if="userStore.hasPermission('supplier:view')">
          <el-icon><OfficeBuilding /></el-icon>
          <span>供应商管理</span>
        </el-menu-item>
        <el-menu-item index="/contacts" v-if="userStore.hasPermission('contact:view')">
          <el-icon><User /></el-icon>
          <span>通讯录</span>
        </el-menu-item>
        <el-menu-item index="/purchases" v-if="userStore.hasPermission('purchase:view')">
          <el-icon><ShoppingCart /></el-icon>
          <span>采购管理</span>
        </el-menu-item>
        <el-menu-item index="/materials" v-if="userStore.hasPermission('material:view')">
          <el-icon><Box /></el-icon>
          <span>物资管理</span>
        </el-menu-item>
        <el-menu-item index="/hr" v-if="userStore.hasPermission('hr:view')">
          <el-icon><Avatar /></el-icon>
          <span>人力资源</span>
        </el-menu-item>
        <el-menu-item index="/progress" v-if="userStore.hasPermission('progress:view')">
          <el-icon><DataLine /></el-icon>
          <span>施工进度</span>
        </el-menu-item>
        <el-menu-item index="/changes" v-if="userStore.hasPermission('change:view')">
          <el-icon><Switch /></el-icon>
          <span>变更管理</span>
        </el-menu-item>
        <el-menu-item index="/finance" v-if="userStore.hasPermission('finance:view')">
          <el-icon><Money /></el-icon>
          <span>财务管理</span>
        </el-menu-item>
        <el-menu-item index="/completion" v-if="userStore.hasPermission('completion:view')">
          <el-icon><Finished /></el-icon>
          <span>竣工管理</span>
        </el-menu-item>
        <el-menu-item index="/announcements" v-if="userStore.hasPermission('announcement:view')">
          <el-icon><Bell /></el-icon>
          <span>通知公告</span>
        </el-menu-item>
        <el-menu-item index="/showcases" v-if="userStore.hasPermission('showcase:view')">
          <el-icon><Picture /></el-icon>
          <span>案例展示</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <el-icon class="collapse-btn" @click="isCollapsed = !isCollapsed">
          <Fold v-if="!isCollapsed" />
          <Expand v-else />
        </el-icon>
        <div class="header-right">
          <span class="welcome">您好，{{ userStore.userInfo?.realName || '用户' }}</span>
          <el-dropdown @command="handleCommand">
            <el-avatar :size="32" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { HomeFilled, Setting, Fold, Expand, Folder, Document, OfficeBuilding, User, ShoppingCart, Box, Avatar, DataLine, Switch, Money, Finished, Bell, Picture } from '@element-plus/icons-vue'
import { logout as logoutApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const isCollapsed = ref(false)

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
      .then(() => {
        logoutApi().catch(() => {})
        userStore.logout()
        router.push('/login')
      })
      .catch(() => {})
  }
}
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
}
.sidebar {
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.welcome {
  font-size: 14px;
  color: #606266;
}
.main-content {
  background: #f0f2f5;
  overflow-y: auto;
}
</style>
