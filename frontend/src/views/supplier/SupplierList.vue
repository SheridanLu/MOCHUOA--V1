<template>
  <div class="supplier-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>供应商管理</span>
          <el-button type="primary" v-permission="'supplier:create'" @click="handleCreate">新增供应商</el-button>
        </div>
      </template>
      <el-form :inline="true" class="search-form" @submit.prevent="loadList">
        <el-form-item>
          <el-input v-model="queryForm.keyword" placeholder="供应商名称/编码/联系人" clearable @clear="loadList" style="width:240px" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.category" placeholder="分类" clearable @change="loadList" style="width:120px">
            <el-option label="材料" :value="1" /><el-option label="劳务" :value="2" /><el-option label="设备" :value="3" /><el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.status" placeholder="状态" clearable @change="loadList" style="width:100px">
            <el-option label="启用" :value="1" /><el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="supplierCode" label="编码" width="120" />
        <el-table-column prop="supplierName" label="供应商名称" min-width="180" />
        <el-table-column label="分类" width="80">
          <template #default="{ row }">{{ categoryMap[row.category] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="评级" width="80">
          <template #default="{ row }">{{ row.rating ? '★'.repeat(row.rating) : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'supplier:edit'" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" v-permission="'supplier:disable'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        class="pagination"
        :current-page="queryForm.page"
        :page-size="queryForm.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="(p) => { queryForm.page = p; loadList() }"
      />
    </el-card>

    <el-dialog v-model="showDialog" :title="editingItem ? '编辑供应商' : '新增供应商'" width="600px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="供应商编码" prop="supplierCode">
          <el-input v-model="form.supplierCode" :disabled="!!editingItem" />
        </el-form-item>
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="form.supplierName" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="请选择">
            <el-option label="材料" :value="1" /><el-option label="劳务" :value="2" /><el-option label="设备" :value="3" /><el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="开户银行">
          <el-input v-model="form.bankName" />
        </el-form-item>
        <el-form-item label="银行账号">
          <el-input v-model="form.bankAccount" />
        </el-form-item>
        <el-form-item label="税号">
          <el-input v-model="form.taxNo" />
        </el-form-item>
        <el-form-item label="评级">
          <el-rate v-model="form.rating" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSupplierList, createSupplier, updateSupplier, updateSupplierStatus } from '@/api/supplier'

const categoryMap = { 1: '材料', 2: '劳务', 3: '设备', 4: '其他' }
const loading = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const editingItem = ref(null)
const list = ref([])
const total = ref(0)
const formRef = ref(null)

const queryForm = reactive({ keyword: '', category: null, status: null, page: 1, size: 20 })
const form = reactive({ supplierCode: '', supplierName: '', category: null, contactPerson: '', contactPhone: '', address: '', bankName: '', bankAccount: '', taxNo: '', rating: 0 })
const rules = {
  supplierCode: [{ required: true, message: '请输入供应商编码', trigger: 'blur' }],
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }]
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getSupplierList(queryForm)
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleCreate = () => {
  editingItem.value = null
  Object.assign(form, { supplierCode: '', supplierName: '', category: null, contactPerson: '', contactPhone: '', address: '', bankName: '', bankAccount: '', taxNo: '', rating: 0 })
  showDialog.value = true
}

const handleEdit = (row) => {
  editingItem.value = row
  Object.assign(form, { supplierCode: row.supplierCode, supplierName: row.supplierName, category: row.category, contactPerson: row.contactPerson, contactPhone: row.contactPhone, address: row.address, bankName: row.bankName, bankAccount: row.bankAccount, taxNo: row.taxNo, rating: row.rating || 0 })
  showDialog.value = true
}

const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '停用' : '启用'
  ElMessageBox.confirm(`确定要${action}供应商 ${row.supplierName} 吗？`, '提示', { type: 'warning' })
    .then(async () => { await updateSupplierStatus(row.id, newStatus); ElMessage.success(`${action}成功`); loadList() })
    .catch(() => {})
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (editingItem.value) { await updateSupplier(editingItem.value.id, form); ElMessage.success('更新成功') }
    else { await createSupplier(form); ElMessage.success('创建成功') }
    showDialog.value = false
    loadList()
  } finally { submitting.value = false }
}

const resetForm = () => { editingItem.value = null }

onMounted(loadList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
