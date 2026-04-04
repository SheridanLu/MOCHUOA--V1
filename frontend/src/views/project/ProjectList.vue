<template>
  <div class="project-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目管理</span>
          <div>
            <el-button type="primary" v-permission="'project:create'" @click="handleCreate(1)">新建实体项目</el-button>
            <el-button type="success" v-permission="'project:create'" @click="handleCreate(2)">新建虚拟项目</el-button>
          </div>
        </div>
      </template>
      <el-form :inline="true" class="search-form" @submit.prevent="loadList">
        <el-form-item>
          <el-input v-model="queryForm.keyword" placeholder="项目名称/编号" clearable @clear="loadList" style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.projectType" placeholder="类型" clearable @change="loadList" style="width:120px">
            <el-option label="实体项目" :value="1" /><el-option label="虚拟项目" :value="2" />
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
        <el-table-column prop="projectNo" label="项目编号" width="130" />
        <el-table-column prop="projectName" label="项目名称" min-width="200" />
        <el-table-column prop="projectTypeName" label="类型" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额(万)" width="120">
          <template #default="{ row }">{{ row.projectType === 2 ? (row.investLimit || '-') : (row.bidAmount || '-') }}</template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" v-permission="'project:edit'" v-if="row.status === 1" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" v-permission="'project:create'" v-if="row.status === 1 && row.projectType === 1" @click="handleSubmit(row)">提交审批</el-button>
            <el-button link type="success" v-permission="'project:approve'" v-if="row.status === 2" @click="handleApprove(row)">审批</el-button>
            <el-button link type="warning" v-permission="'project:edit'" v-if="[3,4].includes(row.status)" @click="handlePause(row)">暂停</el-button>
            <el-button link type="success" v-permission="'project:edit'" v-if="row.status === 5" @click="handleResume(row)">恢复</el-button>
            <el-button link type="info" v-permission="'project:edit'" v-if="row.projectType === 2 && ![7,9].includes(row.status)" @click="handleTerminate(row)">中止</el-button>
            <el-button link type="primary" v-permission="'project:create'" v-if="row.projectType === 2 && ![7,9].includes(row.status)" @click="handleConvert(row)">转实体</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" class="pagination" :current-page="queryForm.page" :page-size="queryForm.size"
        :total="total" layout="total, prev, pager, next" @current-change="(p) => { queryForm.page = p; loadList() }" />
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="showFormDialog" :title="formTitle" width="700px" @close="resetForm">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="110px">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-tree-select v-model="form.deptId" :data="deptTree" :props="{ label: 'deptName', value: 'id', children: 'children' }" check-strictly clearable />
        </el-form-item>

        <template v-if="form.projectType === 1">
          <el-divider>金额信息</el-divider>
          <el-form-item label="含税金额">
            <el-input-number v-model="form.bidAmount" :precision="2" :min="0" style="width:200px" />
          </el-form-item>
          <el-form-item label="税率(%)">
            <el-input-number v-model="form.taxRate" :precision="2" :min="0" :max="100" style="width:200px" />
          </el-form-item>
          <el-form-item label="税额">
            <el-input-number v-model="form.taxAmount" :precision="2" :min="0" style="width:200px" />
          </el-form-item>
          <el-form-item label="不含税金额">
            <el-input-number v-model="form.amountWithoutTax" :precision="2" :min="0" style="width:200px" />
          </el-form-item>
          <el-form-item label="招标公告URL">
            <el-input v-model="form.bidNoticeUrl" />
          </el-form-item>
        </template>

        <template v-if="form.projectType === 2">
          <el-form-item label="投入限额">
            <el-input-number v-model="form.investLimit" :precision="2" :min="0" style="width:200px" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="showFormDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleFormSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="showDetailDialog" title="项目详情" width="750px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="项目编号">{{ detail.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="项目类型">{{ detail.projectTypeName }}</el-descriptions-item>
        <el-descriptions-item label="项目名称" :span="2">{{ detail.projectName }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusTagType(detail.status)" size="small">{{ detail.statusName }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="负责人">{{ detail.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detail.deptName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
        <template v-if="detail.projectType === 1">
          <el-descriptions-item label="含税金额">{{ detail.bidAmount }}</el-descriptions-item>
          <el-descriptions-item label="税率">{{ detail.taxRate }}%</el-descriptions-item>
          <el-descriptions-item label="税额">{{ detail.taxAmount }}</el-descriptions-item>
          <el-descriptions-item label="不含税金额">{{ detail.amountWithoutTax }}</el-descriptions-item>
        </template>
        <template v-if="detail.projectType === 2">
          <el-descriptions-item label="投入限额">{{ detail.investLimit }}</el-descriptions-item>
        </template>
        <el-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <template v-if="detail && detail.paymentBatches && detail.paymentBatches.length">
        <el-divider>付款批次</el-divider>
        <el-table :data="detail.paymentBatches" stripe size="small">
          <el-table-column prop="batchNo" label="批次" width="60" />
          <el-table-column prop="description" label="说明" min-width="150" />
          <el-table-column prop="ratio" label="比例(%)" width="90" />
          <el-table-column prop="amount" label="金额" width="120" />
          <el-table-column prop="plannedDate" label="计划日期" width="120" />
        </el-table>
      </template>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveDialog" title="项目审批" width="500px">
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="approveComment" type="textarea" :rows="3" placeholder="同意请输入至少2个字符，驳回请输入至少5个字符" />
        </el-form-item>
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
import { getProjectList, getProjectById, createProject, updateProject, submitProject, approveProject, rejectProject, pauseProject, resumeProject, terminateProject, convertProject } from '@/api/project'
import { getDeptTree } from '@/api/department'

const statusOptions = [
  { value: 1, label: '草稿' }, { value: 2, label: '待审批' }, { value: 3, label: '已审批' },
  { value: 4, label: '进行中' }, { value: 5, label: '已暂停' }, { value: 6, label: '已关闭' },
  { value: 7, label: '已中止' }, { value: 8, label: '跟踪中' }, { value: 9, label: '已转实体' }
]
const statusTagType = (s) => ({ 1: 'info', 2: 'warning', 3: 'success', 4: 'primary', 5: 'warning', 6: 'info', 7: 'danger', 8: 'primary', 9: 'success' }[s] || 'info')

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const deptTree = ref([])
const formRef = ref(null)
const queryForm = reactive({ keyword: '', projectType: null, status: null, page: 1, size: 20 })
const form = reactive({ projectType: 1, projectName: '', description: '', deptId: null, bidAmount: null, taxRate: null, taxAmount: null, amountWithoutTax: null, investLimit: null, bidNoticeUrl: '' })
const formRules = { projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }] }

const showFormDialog = ref(false)
const showDetailDialog = ref(false)
const showApproveDialog = ref(false)
const editingId = ref(null)
const formTitle = ref('')
const detail = ref(null)
const approveComment = ref('')
const approvingId = ref(null)

const loadList = async () => {
  loading.value = true
  try { const res = await getProjectList(queryForm); list.value = res.data.records; total.value = res.data.total }
  finally { loading.value = false }
}

const handleCreate = (type) => {
  editingId.value = null
  formTitle.value = type === 1 ? '新建实体项目' : '新建虚拟项目'
  Object.assign(form, { projectType: type, projectName: '', description: '', deptId: null, bidAmount: null, taxRate: null, taxAmount: null, amountWithoutTax: null, investLimit: null, bidNoticeUrl: '' })
  showFormDialog.value = true
}

const handleEdit = async (row) => {
  editingId.value = row.id
  formTitle.value = '编辑项目'
  const res = await getProjectById(row.id)
  const d = res.data
  Object.assign(form, { projectType: d.projectType, projectName: d.projectName, description: d.description, deptId: d.deptId, bidAmount: d.bidAmount, taxRate: d.taxRate, taxAmount: d.taxAmount, amountWithoutTax: d.amountWithoutTax, investLimit: d.investLimit, bidNoticeUrl: d.bidNoticeUrl })
  showFormDialog.value = true
}

const handleFormSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (editingId.value) { await updateProject(editingId.value, form); ElMessage.success('更新成功') }
    else { await createProject(form); ElMessage.success('创建成功') }
    showFormDialog.value = false; loadList()
  } finally { submitting.value = false }
}

const handleDetail = async (row) => {
  const res = await getProjectById(row.id)
  detail.value = res.data
  showDetailDialog.value = true
}

const handleSubmit = (row) => {
  ElMessageBox.confirm(`确定提交项目 ${row.projectName} 审批吗？`, '提示', { type: 'warning' })
    .then(async () => { await submitProject(row.id); ElMessage.success('已提交审批'); loadList() }).catch(() => {})
}

const handleApprove = (row) => { approvingId.value = row.id; approveComment.value = ''; showApproveDialog.value = true }
const doApprove = async () => { await approveProject(approvingId.value, approveComment.value); ElMessage.success('审批通过'); showApproveDialog.value = false; loadList() }
const doReject = async () => { await rejectProject(approvingId.value, approveComment.value); ElMessage.success('已驳回'); showApproveDialog.value = false; loadList() }

const handlePause = (row) => { ElMessageBox.confirm(`确定暂停项目 ${row.projectName}？`).then(async () => { await pauseProject(row.id); ElMessage.success('已暂停'); loadList() }).catch(() => {}) }
const handleResume = (row) => { ElMessageBox.confirm(`确定恢复项目 ${row.projectName}？`).then(async () => { await resumeProject(row.id); ElMessage.success('已恢复'); loadList() }).catch(() => {}) }
const handleTerminate = (row) => {
  ElMessageBox.prompt('请输入中止原因', '中止虚拟项目', { inputPattern: /.{2,}/, inputErrorMessage: '请输入至少2个字符' })
    .then(async ({ value }) => { await terminateProject(row.id, value); ElMessage.success('已中止'); loadList() }).catch(() => {})
}
const handleConvert = (row) => {
  ElMessageBox.confirm(`确定将虚拟项目 ${row.projectName} 转为实体项目？`).then(async () => { await convertProject(row.id); ElMessage.success('已转为实体项目'); loadList() }).catch(() => {})
}

const resetForm = () => { editingId.value = null }

onMounted(async () => {
  loadList()
  const res = await getDeptTree(); deptTree.value = res.data
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
