<template>
  <div class="user-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" v-permission="'user:create'" @click="showCreateDialog = true">新增用户</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="用户名/姓名/手机号" clearable @keyup.enter="loadUsers" />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select v-model="query.deptId" :data="deptTree" :props="{ label: 'deptName', value: 'id', children: 'children' }" clearable placeholder="选择部门" check-strictly />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUsers">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role.id" size="small" class="role-tag">{{ role.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'user:edit'" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" v-permission="'user:disable'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="warning" v-permission="'user:edit'" @click="handleResetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadUsers"
        @current-change="loadUsers"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="showCreateDialog" :title="editingUser ? '编辑用户' : '新增用户'" width="500px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username" v-if="!editingUser">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!editingUser">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select v-model="form.deptId" :data="deptTree" :props="{ label: 'deptName', value: 'id', children: 'children' }" clearable check-strictly />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="选择角色">
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, updateUserStatus, resetUserPassword } from '@/api/user'
import { getRoleList } from '@/api/role'
import { getDeptTree } from '@/api/department'

const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const editingUser = ref(null)
const users = ref([])
const total = ref(0)
const roles = ref([])
const deptTree = ref([])
const formRef = ref(null)

const query = reactive({ keyword: '', deptId: null, status: null, page: 1, size: 20 })
const form = reactive({ username: '', password: '', realName: '', phone: '', email: '', deptId: null, roleIds: [] })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList(query)
    users.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(query, { keyword: '', deptId: null, status: null, page: 1 })
  loadUsers()
}

const handleEdit = (row) => {
  editingUser.value = row
  Object.assign(form, { realName: row.realName, phone: row.phone, email: row.email, deptId: row.deptId, roleIds: row.roles?.map(r => r.id) || [] })
  showCreateDialog.value = true
}

const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '停用' : '启用'
  ElMessageBox.confirm(`确定要${action}用户 ${row.realName} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await updateUserStatus(row.id, newStatus)
      ElMessage.success(`${action}成功`)
      loadUsers()
    }).catch(() => {})
}

const handleResetPassword = (row) => {
  ElMessageBox.confirm(`确定要重置用户 ${row.realName} 的密码吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await resetUserPassword(row.id)
      ElMessage.success('密码重置成功')
    }).catch(() => {})
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (editingUser.value) {
      await updateUser(editingUser.value.id, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    showCreateDialog.value = false
    loadUsers()
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  editingUser.value = null
  Object.assign(form, { username: '', password: '', realName: '', phone: '', email: '', deptId: null, roleIds: [] })
}

onMounted(async () => {
  loadUsers()
  const [roleRes, deptRes] = await Promise.all([getRoleList(), getDeptTree()])
  roles.value = roleRes.data
  deptTree.value = deptRes.data
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.role-tag { margin-right: 4px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
