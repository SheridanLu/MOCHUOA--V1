<template>
  <div class="contract-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>合同管理</span>
          <div>
            <el-button type="primary" v-permission="'contract:create'" @click="handleCreate(1)">新建收入合同</el-button>
            <el-button type="success" v-permission="'contract:create'" @click="handleCreate(2)">新建支出合同</el-button>
          </div>
        </div>
      </template>
      <el-form :inline="true" class="search-form" @submit.prevent="loadList">
        <el-form-item>
          <el-input v-model="queryForm.keyword" placeholder="合同名称/编号" clearable @clear="loadList" style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.contractType" placeholder="类型" clearable @change="loadList" style="width:120px">
            <el-option label="收入合同" :value="1" /><el-option label="支出合同" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.status" placeholder="状态" clearable @change="loadList" style="width:120px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadList">搜索</el-button></el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="contractNo" label="合同编号" width="130" />
        <el-table-column prop="contractName" label="合同名称" min-width="200" />
        <el-table-column prop="contractTypeName" label="类型" width="100" />
        <el-table-column prop="projectName" label="所属项目" width="150" />
        <el-table-column prop="supplierName" label="供应商" width="120" />
        <el-table-column label="含税金额" width="120">
          <template #default="{ row }">{{ row.amountWithTax || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="warning" v-permission="'contract:create'" v-if="row.status === 1" @click="handleSubmit(row)">提交审批</el-button>
            <el-button link type="success" v-permission="'contract:approve'" v-if="[2,3,4].includes(row.status)" @click="handleApprove(row)">审批</el-button>
            <el-button link type="danger" v-permission="'contract:terminate'" v-if="[5,6].includes(row.status)" @click="handleTerminate(row)">终止</el-button>
            <el-button link type="primary" v-permission="'contract:create'" v-if="row.status >= 5 && row.status !== 8" @click="handleSupplement(row)">补充协议</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" class="pagination" :current-page="queryForm.page" :page-size="queryForm.size"
        :total="total" layout="total, prev, pager, next" @current-change="(p) => { queryForm.page = p; loadList() }" />
    </el-card>

    <!-- Create Dialog -->
    <el-dialog v-model="showFormDialog" :title="createType === 1 ? '新建收入合同' : '新建支出合同'" width="800px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="110px">
        <el-form-item label="合同名称" prop="contractName"><el-input v-model="form.contractName" /></el-form-item>
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" filterable placeholder="选择项目" style="width:100%">
            <el-option v-for="p in projects" :key="p.id" :label="`${p.projectNo} - ${p.projectName}`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商" v-if="createType === 2">
          <el-select v-model="form.supplierId" filterable clearable placeholder="选择供应商" style="width:100%">
            <el-option v-for="s in suppliers" :key="s.id" :label="s.supplierName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="含税金额"><el-input-number v-model="form.amountWithTax" :precision="2" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="税率(%)"><el-input-number v-model="form.taxRate" :precision="2" :min="0" :max="100" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="签订日期"><el-date-picker v-model="form.signDate" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="开始日期"><el-date-picker v-model="form.startDate" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="结束日期"><el-date-picker v-model="form.endDate" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>

        <template v-if="createType === 2">
          <el-divider>物资明细</el-divider>
          <el-button type="primary" size="small" @click="addItem" style="margin-bottom:12px">添加明细</el-button>
          <el-table :data="form.items" size="small">
            <el-table-column label="物资名称" min-width="120"><template #default="{ row }"><el-input v-model="row.materialName" size="small" /></template></el-table-column>
            <el-table-column label="规格" width="100"><template #default="{ row }"><el-input v-model="row.spec" size="small" /></template></el-table-column>
            <el-table-column label="单位" width="70"><template #default="{ row }"><el-input v-model="row.unit" size="small" /></template></el-table-column>
            <el-table-column label="数量" width="90"><template #default="{ row }"><el-input-number v-model="row.quantity" :precision="2" :min="0" size="small" controls-position="right" /></template></el-table-column>
            <el-table-column label="单价" width="100"><template #default="{ row }"><el-input-number v-model="row.unitPrice" :precision="2" :min="0" size="small" controls-position="right" /></template></el-table-column>
            <el-table-column label="操作" width="60"><template #default="{ $index }"><el-button link type="danger" size="small" @click="form.items.splice($index, 1)">删除</el-button></template></el-table-column>
          </el-table>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="showFormDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleFormSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="showDetailDialog" title="合同详情" width="800px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="合同编号">{{ detail.contractNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.contractTypeName }}</el-descriptions-item>
          <el-descriptions-item label="合同名称" :span="2">{{ detail.contractName }}</el-descriptions-item>
          <el-descriptions-item label="所属项目">{{ detail.projectName }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ detail.supplierName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="含税金额">{{ detail.amountWithTax }}</el-descriptions-item>
          <el-descriptions-item label="税率">{{ detail.taxRate }}%</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusTagType(detail.status)" size="small">{{ detail.statusName }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="签订日期">{{ detail.signDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="合同期限">{{ detail.startDate || '-' }} ~ {{ detail.endDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <template v-if="detail.items && detail.items.length">
          <el-divider>物资明细</el-divider>
          <el-table :data="detail.items" stripe size="small">
            <el-table-column prop="materialName" label="物资名称" /><el-table-column prop="spec" label="规格" width="100" />
            <el-table-column prop="unit" label="单位" width="60" /><el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="unitPrice" label="单价" width="90" /><el-table-column prop="amount" label="金额" width="100" />
          </el-table>
        </template>

        <template v-if="detail.supplements && detail.supplements.length">
          <el-divider>补充协议</el-divider>
          <el-table :data="detail.supplements" stripe size="small">
            <el-table-column prop="supplementNo" label="协议编号" width="130" />
            <el-table-column prop="reason" label="原因" min-width="150" />
            <el-table-column prop="amountChange" label="金额变动" width="100" />
            <el-table-column prop="newTotal" label="新总额" width="100" />
          </el-table>
        </template>
      </template>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveDialog" title="合同审批" width="500px">
      <el-form label-width="80px">
        <el-form-item label="审批意见"><el-input v-model="approveComment" type="textarea" :rows="3" placeholder="同意≥2字符，驳回≥5字符" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApproveDialog = false">取消</el-button>
        <el-button type="danger" @click="doReject">驳回</el-button>
        <el-button type="success" @click="doApprove">通过</el-button>
      </template>
    </el-dialog>

    <!-- Supplement Dialog -->
    <el-dialog v-model="showSupDialog" title="签订补充协议" width="500px">
      <el-form :model="supForm" ref="supFormRef" label-width="100px" :rules="{ reason: [{ required: true, message: '请输入原因' }] }">
        <el-form-item label="补充原因" prop="reason"><el-input v-model="supForm.reason" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="金额变动"><el-input-number v-model="supForm.amountChange" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="新总额"><el-input-number v-model="supForm.newTotal" :precision="2" :min="0" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSupDialog = false">取消</el-button>
        <el-button type="primary" @click="doSupplement">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getContractList, getContractById, createContract, submitContract, approveContract, rejectContract, terminateContract, createSupplement } from '@/api/contract'
import { getProjectList } from '@/api/project'
import { getEnabledSuppliers } from '@/api/supplier'

const statusOptions = [
  { value: 1, label: '草稿' }, { value: 2, label: '待审批' }, { value: 3, label: '财务已审' },
  { value: 4, label: '法务已审' }, { value: 5, label: '已审批' }, { value: 6, label: '执行中' },
  { value: 7, label: '已完成' }, { value: 8, label: '已终止' }
]
const statusTagType = (s) => ({ 1: 'info', 2: 'warning', 3: '', 4: '', 5: 'success', 6: 'primary', 7: 'success', 8: 'danger' }[s] || 'info')

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const projects = ref([])
const suppliers = ref([])
const formRef = ref(null)
const supFormRef = ref(null)
const queryForm = reactive({ keyword: '', contractType: null, status: null, page: 1, size: 20 })

const showFormDialog = ref(false)
const showDetailDialog = ref(false)
const showApproveDialog = ref(false)
const showSupDialog = ref(false)
const createType = ref(1)
const detail = ref(null)
const approveComment = ref('')
const approvingId = ref(null)
const supContractId = ref(null)

const form = reactive({ contractName: '', contractType: 1, projectId: null, supplierId: null, amountWithTax: null, taxRate: null, signDate: '', startDate: '', endDate: '', remark: '', items: [] })
const supForm = reactive({ reason: '', amountChange: null, newTotal: null })
const formRules = { contractName: [{ required: true, message: '请输入合同名称' }], projectId: [{ required: true, message: '请选择项目' }] }

const addItem = () => form.items.push({ materialName: '', spec: '', unit: '', quantity: null, unitPrice: null })

const loadList = async () => {
  loading.value = true
  try { const res = await getContractList(queryForm); list.value = res.data.records; total.value = res.data.total }
  finally { loading.value = false }
}

const handleCreate = (type) => {
  createType.value = type
  Object.assign(form, { contractName: '', contractType: type, projectId: null, supplierId: null, amountWithTax: null, taxRate: null, signDate: '', startDate: '', endDate: '', remark: '', items: [] })
  showFormDialog.value = true
}

const handleFormSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try { await createContract(form); ElMessage.success('创建成功'); showFormDialog.value = false; loadList() }
  finally { submitting.value = false }
}

const handleDetail = async (row) => { const res = await getContractById(row.id); detail.value = res.data; showDetailDialog.value = true }

const handleSubmit = (row) => {
  ElMessageBox.confirm(`确定提交合同 ${row.contractName} 审批吗？`).then(async () => { await submitContract(row.id); ElMessage.success('已提交审批'); loadList() }).catch(() => {})
}

const handleApprove = (row) => { approvingId.value = row.id; approveComment.value = ''; showApproveDialog.value = true }
const doApprove = async () => { await approveContract(approvingId.value, approveComment.value); ElMessage.success('审批通过'); showApproveDialog.value = false; loadList() }
const doReject = async () => { await rejectContract(approvingId.value, approveComment.value); ElMessage.success('已驳回'); showApproveDialog.value = false; loadList() }

const handleTerminate = (row) => {
  ElMessageBox.prompt('请输入终止原因', '终止合同', { inputPattern: /.{2,}/, inputErrorMessage: '请输入至少2个字符' })
    .then(async ({ value }) => { await terminateContract(row.id, value); ElMessage.success('已终止'); loadList() }).catch(() => {})
}

const handleSupplement = (row) => { supContractId.value = row.id; Object.assign(supForm, { reason: '', amountChange: null, newTotal: null }); showSupDialog.value = true }
const doSupplement = async () => {
  await supFormRef.value?.validate()
  await createSupplement(supContractId.value, supForm); ElMessage.success('补充协议创建成功'); showSupDialog.value = false; loadList()
}

onMounted(async () => {
  loadList()
  const [pRes, sRes] = await Promise.all([getProjectList({ size: 999 }), getEnabledSuppliers()])
  projects.value = pRes.data.records || []
  suppliers.value = sRes.data || []
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
