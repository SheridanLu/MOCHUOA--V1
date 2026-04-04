<template>
  <div class="purchase-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>采购管理</span>
          <el-button type="primary" v-permission="'purchase:create'" @click="handleCreate">新建采购清单</el-button>
        </div>
      </template>
      <el-form :inline="true" class="search-form">
        <el-form-item><el-input v-model="query.keyword" placeholder="搜索编号" clearable @keyup.enter="loadList" style="width:200px" /></el-form-item>
        <el-form-item>
          <el-select v-model="query.status" placeholder="状态" clearable @change="loadList" style="width:120px">
            <el-option label="草稿" :value="1" /><el-option label="待审批" :value="2" />
            <el-option label="财务已审" :value="3" /><el-option label="已审批" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadList">搜索</el-button></el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="listNo" label="采购编号" width="160" />
        <el-table-column prop="projectName" label="所属项目" min-width="200" />
        <el-table-column prop="totalAmount" label="总金额" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 4 ? 'success' : row.status >= 2 ? 'warning' : 'info'" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="warning" v-if="row.status === 1" v-permission="'purchase:create'" @click="handleSubmit(row)">提交</el-button>
            <el-button link type="success" v-if="row.status === 2 || row.status === 3" v-permission="'purchase:approve'" @click="handleApprove(row)">审批</el-button>
            <el-button link type="info" v-if="row.status === 4" v-permission="'purchase:create'" @click="handleChange(row)">变更</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="loadList" />
    </el-card>

    <!-- Create Dialog -->
    <el-dialog v-model="showCreateDialog" title="新建采购清单" width="800px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="项目ID"><el-input-number v-model="form.projectId" :min="1" /></el-form-item>
        <el-form-item label="物资明细">
          <el-button type="primary" size="small" @click="addItem">添加</el-button>
          <el-table :data="form.items" class="mt-2" border size="small">
            <el-table-column label="名称" min-width="150"><template #default="{ row }"><el-input v-model="row.materialName" size="small" /></template></el-table-column>
            <el-table-column label="规格" width="120"><template #default="{ row }"><el-input v-model="row.spec" size="small" /></template></el-table-column>
            <el-table-column label="单位" width="80"><template #default="{ row }"><el-input v-model="row.unit" size="small" /></template></el-table-column>
            <el-table-column label="数量" width="100"><template #default="{ row }"><el-input-number v-model="row.quantity" size="small" :min="0" /></template></el-table-column>
            <el-table-column label="估价" width="100"><template #default="{ row }"><el-input-number v-model="row.estimatedPrice" size="small" :min="0" :precision="2" /></template></el-table-column>
            <el-table-column label="操作" width="60"><template #default="{ $index }"><el-button link type="danger" size="small" @click="form.items.splice($index, 1)">删除</el-button></template></el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="showDetailDialog" title="采购清单详情" width="700px">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="编号">{{ detail.listNo }}</el-descriptions-item>
        <el-descriptions-item label="项目">{{ detail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusName }}</el-descriptions-item>
        <el-descriptions-item label="总金额">{{ detail.totalAmount }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detail?.items || []" class="mt-4" border size="small">
        <el-table-column prop="materialName" label="名称" /><el-table-column prop="spec" label="规格" />
        <el-table-column prop="unit" label="单位" width="80" /><el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="estimatedPrice" label="估价" width="100" />
      </el-table>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveDialog" title="审批" width="500px">
      <el-form label-width="80px">
        <el-form-item label="审批意见"><el-input v-model="approveComment" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApproveDialog = false">取消</el-button>
        <el-button type="danger" @click="doReject">驳回</el-button>
        <el-button type="success" @click="doApprove">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPurchaseList, getPurchaseById, createPurchase, submitPurchase, approvePurchase, rejectPurchase, requestPurchaseChange } from '@/api/purchase'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: null, page: 1, size: 20 })
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showApproveDialog = ref(false)
const detail = ref(null)
const approvingId = ref(null)
const approveComment = ref('')
const form = reactive({ projectId: null, items: [] })

const loadList = async () => {
  loading.value = true
  try {
    const res = await getPurchaseList(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const handleCreate = () => {
  form.projectId = null
  form.items = [{ materialName: '', spec: '', unit: '', quantity: null, estimatedPrice: null }]
  showCreateDialog.value = true
}

const addItem = () => form.items.push({ materialName: '', spec: '', unit: '', quantity: null, estimatedPrice: null })

const doCreate = async () => {
  submitting.value = true
  try {
    await createPurchase(form)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    loadList()
  } finally { submitting.value = false }
}

const handleDetail = async (row) => {
  const res = await getPurchaseById(row.id)
  detail.value = res.data
  showDetailDialog.value = true
}

const handleSubmit = (row) => {
  ElMessageBox.confirm(`确定提交 ${row.listNo} 审批？`).then(async () => {
    await submitPurchase(row.id); ElMessage.success('已提交'); loadList()
  }).catch(() => {})
}

const handleApprove = (row) => { approvingId.value = row.id; approveComment.value = ''; showApproveDialog.value = true }
const doApprove = async () => { await approvePurchase(approvingId.value, approveComment.value); ElMessage.success('审批通过'); showApproveDialog.value = false; loadList() }
const doReject = async () => { await rejectPurchase(approvingId.value, approveComment.value); ElMessage.success('已驳回'); showApproveDialog.value = false; loadList() }

const handleChange = (row) => {
  ElMessageBox.confirm(`确定对 ${row.listNo} 发起变更？`).then(async () => {
    await requestPurchaseChange(row.id); ElMessage.success('已发起变更'); loadList()
  }).catch(() => {})
}

onMounted(loadList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.mt-2 { margin-top: 8px; }
.mt-4 { margin-top: 16px; }
</style>
