<template>
  <div class="role-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" v-permission="'role:create'" @click="showDialog = true">新增角色</el-button>
        </div>
      </template>
      <el-table :data="roles" v-loading="loading" stripe>
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="数据权限" width="120">
          <template #default="{ row }">{{ dataScopeMap[row.dataScope] || '未知' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'role:edit'" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="editingRole ? '编辑角色' : '新增角色'" width="600px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="!!editingRole" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="数据权限">
          <el-select v-model="form.dataScope">
            <el-option label="全部数据" :value="1" />
            <el-option label="本部门数据" :value="2" />
            <el-option label="本人数据" :value="3" />
            <el-option label="指定范围" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限">
          <el-tree ref="permTreeRef" :data="permTree" show-checkbox node-key="id"
            :props="{ label: 'permName', children: 'children' }" :default-checked-keys="form.permissionIds" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getRoleList, createRole, updateRole } from '@/api/role'
import { getPermissionList } from '@/api/permission'

const loading = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const editingRole = ref(null)
const roles = ref([])
const permissions = ref([])
const formRef = ref(null)
const permTreeRef = ref(null)

const dataScopeMap = { 1: '全部', 2: '本部门', 3: '本人', 4: '指定范围' }
const form = reactive({ roleCode: '', roleName: '', description: '', dataScope: 1, permissionIds: [] })
const rules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const permTree = computed(() => {
  const moduleMap = {}
  permissions.value.forEach(p => {
    if (!moduleMap[p.module]) moduleMap[p.module] = { id: 'module_' + p.module, permName: p.module, children: [] }
    moduleMap[p.module].children.push(p)
  })
  return Object.values(moduleMap)
})

const loadRoles = async () => {
  loading.value = true
  try { const res = await getRoleList(); roles.value = res.data } finally { loading.value = false }
}

const handleEdit = (row) => {
  editingRole.value = row
  Object.assign(form, { roleCode: row.roleCode, roleName: row.roleName, description: row.description, dataScope: row.dataScope, permissionIds: row.permissionIds || [] })
  showDialog.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  const checkedKeys = permTreeRef.value?.getCheckedKeys(true) || []
  const permIds = checkedKeys.filter(k => typeof k === 'number')
  try {
    const data = { ...form, permissionIds: permIds }
    if (editingRole.value) { await updateRole(editingRole.value.id, data); ElMessage.success('更新成功') }
    else { await createRole(data); ElMessage.success('创建成功') }
    showDialog.value = false
    loadRoles()
  } finally { submitting.value = false }
}

const resetForm = () => {
  editingRole.value = null
  Object.assign(form, { roleCode: '', roleName: '', description: '', dataScope: 1, permissionIds: [] })
}

onMounted(async () => { loadRoles(); const res = await getPermissionList(); permissions.value = res.data })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
