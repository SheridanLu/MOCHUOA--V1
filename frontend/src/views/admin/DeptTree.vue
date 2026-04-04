<template>
  <div class="dept-tree-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <el-button type="primary" v-permission="'dept:create'" @click="handleCreate">新增部门</el-button>
        </div>
      </template>
      <el-tree :data="tree" :props="{ label: 'deptName', children: 'children' }" node-key="id" default-expand-all>
        <template #default="{ node, data }">
          <div class="tree-node">
            <span>{{ data.deptName }}</span>
            <span class="tree-actions">
              <el-tag size="small" :type="data.status === 1 ? 'success' : 'info'">{{ data.status === 1 ? '启用' : '停用' }}</el-tag>
              <el-button link type="primary" v-permission="'dept:edit'" @click.stop="handleEdit(data)">编辑</el-button>
              <el-button link :type="data.status === 1 ? 'danger' : 'success'" v-permission="'dept:edit'" @click.stop="handleToggleStatus(data)">
                {{ data.status === 1 ? '停用' : '启用' }}
              </el-button>
            </span>
          </div>
        </template>
      </el-tree>
    </el-card>

    <el-dialog v-model="showDialog" :title="editingDept ? '编辑部门' : '新增部门'" width="400px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" />
        </el-form-item>
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select v-model="form.parentId" :data="treeWithRoot" :props="{ label: 'deptName', value: 'id', children: 'children' }" check-strictly />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeptTree, createDept, updateDept, updateDeptStatus } from '@/api/department'

const tree = ref([])
const showDialog = ref(false)
const editingDept = ref(null)
const formRef = ref(null)
const form = reactive({ deptName: '', parentId: 0, sortOrder: 0 })
const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择上级部门', trigger: 'change' }]
}

const treeWithRoot = computed(() => [{ id: 0, deptName: '顶级部门', children: tree.value }])

const loadTree = async () => { const res = await getDeptTree(); tree.value = res.data }

const handleCreate = () => {
  editingDept.value = null
  Object.assign(form, { deptName: '', parentId: 0, sortOrder: 0 })
  showDialog.value = true
}

const handleEdit = (data) => {
  editingDept.value = data
  Object.assign(form, { deptName: data.deptName, parentId: data.parentId, sortOrder: data.sortOrder })
  showDialog.value = true
}

const handleToggleStatus = (data) => {
  const newStatus = data.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '停用' : '启用'
  ElMessageBox.confirm(`确定要${action}部门 ${data.deptName} 吗？`, '提示', { type: 'warning' })
    .then(async () => { await updateDeptStatus(data.id, newStatus); ElMessage.success(`${action}成功`); loadTree() })
    .catch(() => {})
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  if (editingDept.value) { await updateDept(editingDept.value.id, form); ElMessage.success('更新成功') }
  else { await createDept(form); ElMessage.success('创建成功') }
  showDialog.value = false
  loadTree()
}

onMounted(loadTree)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.tree-node { display: flex; justify-content: space-between; align-items: center; width: 100%; padding-right: 8px; }
.tree-actions { display: flex; align-items: center; gap: 8px; }
</style>
