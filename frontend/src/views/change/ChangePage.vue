<template>
  <div class="change-page">
    <el-tabs v-model="activeTab" @tab-click="loadTab">
      <!-- Site Visa Tab -->
      <el-tab-pane label="现场签证" name="siteVisa">
        <div class="tab-header">
          <span></span>
          <el-button type="primary" v-permission="'change:create'" @click="showSiteVisaForm = true">新建签证</el-button>
        </div>
        <el-table :data="ledgerData" v-loading="loading" stripe>
          <el-table-column prop="changeNo" label="编号" width="160" />
          <el-table-column prop="projectName" label="项目" min-width="180" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="amountChange" label="金额变化" width="120" />
          <el-table-column label="类型" width="100">
            <template #default="{ row }"><el-tag size="small">{{ row.changeType }}</el-tag></template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 2 ? 'success' : row.status === 3 ? 'danger' : 'warning'" size="small">{{ row.statusName || ['','待审批','已通过','已驳回'][row.status] }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="160" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 1 && row.changeType === '现场签证'">
                <el-button link type="success" v-permission="'change:approve'" @click="openApprove('siteVisa', row)">通过</el-button>
                <el-button link type="danger" v-permission="'change:approve'" @click="openReject('siteVisa', row)">驳回</el-button>
              </template>
              <template v-if="row.status === 1 && row.changeType === '业主变更'">
                <el-button link type="success" v-permission="'change:approve'" @click="openApprove('ownerChange', row)">通过</el-button>
                <el-button link type="danger" v-permission="'change:approve'" @click="openReject('ownerChange', row)">驳回</el-button>
              </template>
              <template v-if="row.status === 1 && row.changeType === '用工签证'">
                <el-button link type="success" v-permission="'change:approve'" @click="openApprove('laborVisa', row)">通过</el-button>
                <el-button link type="danger" v-permission="'change:approve'" @click="openReject('laborVisa', row)">驳回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="ledgerTotal" :page-size="query.size" v-model:current-page="query.page" @current-change="loadLedger" />
      </el-tab-pane>

      <!-- Owner Change Tab -->
      <el-tab-pane label="业主变更" name="ownerChange">
        <div class="tab-header">
          <span></span>
          <el-button type="primary" v-permission="'change:create'" @click="showOwnerForm = true">新建变更</el-button>
        </div>
        <el-alert title="业主变更数据统一在「现场签证」选项卡的变更台账中查看" type="info" :closable="false" />
      </el-tab-pane>

      <!-- Labor Visa Tab -->
      <el-tab-pane label="用工签证" name="laborVisa">
        <div class="tab-header">
          <span></span>
          <el-button type="primary" v-permission="'change:create'" @click="showLaborForm = true">新建用工签证</el-button>
        </div>
        <el-alert title="用工签证数据统一在「现场签证」选项卡的变更台账中查看" type="info" :closable="false" />
      </el-tab-pane>
    </el-tabs>

    <!-- Site Visa Form -->
    <el-dialog v-model="showSiteVisaForm" title="新建现场签证" width="550px">
      <el-form :model="siteVisaForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="siteVisaForm.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="siteVisaForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="金额变化"><el-input-number v-model="siteVisaForm.amountChange" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="附件URL"><el-input v-model="siteVisaForm.fileUrl" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSiteVisaForm = false">取消</el-button>
        <el-button type="primary" @click="doCreateSiteVisa">确定</el-button>
      </template>
    </el-dialog>

    <!-- Owner Change Form -->
    <el-dialog v-model="showOwnerForm" title="新建业主变更" width="550px">
      <el-form :model="ownerForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="ownerForm.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="ownerForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="金额变化"><el-input-number v-model="ownerForm.amountChange" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="附件URL"><el-input v-model="ownerForm.fileUrl" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showOwnerForm = false">取消</el-button>
        <el-button type="primary" @click="doCreateOwnerChange">确定</el-button>
      </template>
    </el-dialog>

    <!-- Labor Visa Form -->
    <el-dialog v-model="showLaborForm" title="新建用工签证" width="550px">
      <el-form :model="laborForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="laborForm.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="laborForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="用工人数"><el-input-number v-model="laborForm.laborCount" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="laborForm.amount" :precision="2" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLaborForm = false">取消</el-button>
        <el-button type="primary" @click="doCreateLaborVisa">确定</el-button>
      </template>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveDialog" :title="approveDialogTitle" width="450px">
      <el-form label-width="80px">
        <el-form-item label="审批意见"><el-input v-model="approveComment" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApproveDialog = false">取消</el-button>
        <el-button :type="isReject ? 'danger' : 'success'" @click="doApproveAction">{{ isReject ? '确认驳回' : '确认通过' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createSiteVisa, approveSiteVisa, rejectSiteVisa, createOwnerChange, approveOwnerChange, rejectOwnerChange, createLaborVisa, approveLaborVisa, rejectLaborVisa, getChangeLedger } from '@/api/change'

const loading = ref(false)
const activeTab = ref('siteVisa')
const ledgerData = ref([]), ledgerTotal = ref(0)
const query = reactive({ projectId: null, status: null, page: 1, size: 20 })

const showSiteVisaForm = ref(false), showOwnerForm = ref(false), showLaborForm = ref(false)
const siteVisaForm = reactive({ projectId: null, description: '', amountChange: 0, fileUrl: '' })
const ownerForm = reactive({ projectId: null, description: '', amountChange: 0, fileUrl: '' })
const laborForm = reactive({ projectId: null, description: '', laborCount: 1, amount: 0 })

const showApproveDialog = ref(false), approveComment = ref(''), approveDialogTitle = ref('')
const isReject = ref(false), approvingType = ref(''), approvingId = ref(null)

const loadTab = () => { if (activeTab.value === 'siteVisa') loadLedger() }

const loadLedger = async () => { loading.value = true; try { const r = await getChangeLedger(query); ledgerData.value = r.data.records; ledgerTotal.value = r.data.total } finally { loading.value = false } }

const doCreateSiteVisa = async () => { await createSiteVisa(siteVisaForm); ElMessage.success('创建成功'); showSiteVisaForm.value = false; loadLedger() }
const doCreateOwnerChange = async () => { await createOwnerChange(ownerForm); ElMessage.success('创建成功'); showOwnerForm.value = false; loadLedger() }
const doCreateLaborVisa = async () => { await createLaborVisa(laborForm); ElMessage.success('创建成功'); showLaborForm.value = false; loadLedger() }

const openApprove = (type, row) => { approvingType.value = type; approvingId.value = row.id; isReject.value = false; approveDialogTitle.value = '审批通过'; approveComment.value = ''; showApproveDialog.value = true }
const openReject = (type, row) => { approvingType.value = type; approvingId.value = row.id; isReject.value = true; approveDialogTitle.value = '驳回'; approveComment.value = ''; showApproveDialog.value = true }

const doApproveAction = async () => {
  const fns = { siteVisa: [approveSiteVisa, rejectSiteVisa], ownerChange: [approveOwnerChange, rejectOwnerChange], laborVisa: [approveLaborVisa, rejectLaborVisa] }
  const fn = fns[approvingType.value][isReject.value ? 1 : 0]
  await fn(approvingId.value, approveComment.value)
  ElMessage.success(isReject.value ? '已驳回' : '审批通过')
  showApproveDialog.value = false; loadLedger()
}

onMounted(loadLedger)
</script>

<style scoped>
.tab-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
</style>
