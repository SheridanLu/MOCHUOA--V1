<template>
  <div class="finance-page">
    <el-tabs v-model="activeTab" @tab-click="loadTab">
      <!-- Income Split Tab -->
      <el-tab-pane label="收入分解" name="incomeSplit">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input-number v-model="splitQuery.projectId" placeholder="项目ID" :min="0" /></el-form-item>
            <el-form-item>
              <el-select v-model="splitQuery.status" placeholder="状态" clearable style="width:120px">
                <el-option label="待审批" :value="1" /><el-option label="已通过" :value="2" /><el-option label="已驳回" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary" @click="loadSplits">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'finance:create'" @click="showSplitForm = true">新建分解</el-button>
        </div>
        <el-table :data="splits" v-loading="loading" stripe>
          <el-table-column prop="splitNo" label="编号" width="160" />
          <el-table-column prop="projectName" label="项目" min-width="180" />
          <el-table-column prop="contractName" label="合同" width="160" />
          <el-table-column prop="period" label="期间" width="100" />
          <el-table-column prop="amount" label="金额" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 2 ? 'success' : row.status === 3 ? 'danger' : 'warning'" size="small">{{ row.statusName || ['','待审批','已通过','已驳回'][row.status] }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 1">
                <el-button link type="success" v-permission="'finance:approve'" @click="openApprove('split', row, false)">通过</el-button>
                <el-button link type="danger" v-permission="'finance:approve'" @click="openApprove('split', row, true)">驳回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="splitTotal" :page-size="splitQuery.size" v-model:current-page="splitQuery.page" @current-change="loadSplits" />
      </el-tab-pane>

      <!-- Payment Tab -->
      <el-tab-pane label="付款管理" name="payment">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input-number v-model="payQuery.projectId" placeholder="项目ID" :min="0" /></el-form-item>
            <el-form-item><el-button type="primary" @click="loadPayments">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'finance:create'" @click="showPayForm = true">新建付款</el-button>
        </div>
        <el-table :data="payments" v-loading="loading" stripe>
          <el-table-column prop="paymentNo" label="付款编号" width="160" />
          <el-table-column prop="projectName" label="项目" min-width="160" />
          <el-table-column prop="supplierName" label="供应商" width="150" />
          <el-table-column prop="amount" label="金额" width="110" />
          <el-table-column prop="paidAmount" label="已付" width="110" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 3 ? 'success' : row.status === 4 ? 'danger' : 'warning'" size="small">{{ row.statusName || ['','待审批','已审批','已付款','已驳回'][row.status] }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 1">
                <el-button link type="success" v-permission="'finance:approve'" @click="openApprove('payment', row, false)">通过</el-button>
                <el-button link type="danger" v-permission="'finance:approve'" @click="openApprove('payment', row, true)">驳回</el-button>
              </template>
              <el-button link type="primary" v-if="row.status === 2" v-permission="'finance:approve'" @click="doMarkPaid(row)">标记已付</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="payTotal" :page-size="payQuery.size" v-model:current-page="payQuery.page" @current-change="loadPayments" />
      </el-tab-pane>

      <!-- Reconciliation Tab -->
      <el-tab-pane label="对账管理" name="reconciliation">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input v-model="reconQuery.period" placeholder="期间 如2024-03" clearable style="width:150px" /></el-form-item>
            <el-form-item><el-button type="primary" @click="loadReconciliations">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="warning" v-permission="'finance:create'" @click="doGenerateReconciliation">生成月度对账</el-button>
        </div>
        <el-table :data="reconciliations" v-loading="loading" stripe>
          <el-table-column prop="reconciliationNo" label="对账编号" width="160" />
          <el-table-column prop="projectName" label="项目" min-width="160" />
          <el-table-column prop="supplierName" label="供应商" width="150" />
          <el-table-column prop="period" label="期间" width="100" />
          <el-table-column prop="totalAmount" label="总金额" width="110" />
          <el-table-column prop="confirmedAmount" label="确认金额" width="110" />
          <el-table-column prop="difference" label="差异" width="100">
            <template #default="{ row }"><el-tag :type="row.difference === 0 ? 'success' : 'danger'" size="small">{{ row.difference }}</el-tag></template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 2 ? 'success' : 'warning'" size="small">{{ row.statusName || ['','待确认','已确认','已驳回'][row.status] }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 1">
                <el-button link type="success" v-permission="'finance:approve'" @click="openApprove('reconciliation', row, false)">确认</el-button>
                <el-button link type="danger" v-permission="'finance:approve'" @click="openApprove('reconciliation', row, true)">驳回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="reconTotal" :page-size="reconQuery.size" v-model:current-page="reconQuery.page" @current-change="loadReconciliations" />
      </el-tab-pane>

      <!-- Invoice Tab -->
      <el-tab-pane label="发票管理" name="invoice">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input-number v-model="invQuery.projectId" placeholder="项目ID" :min="0" /></el-form-item>
            <el-form-item><el-button type="primary" @click="loadInvoices">搜索</el-button></el-form-item>
            <el-form-item><el-button type="warning" @click="loadExpiring">即将到期</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'finance:create'" @click="showInvoiceForm = true">登记发票</el-button>
        </div>
        <el-table :data="invoices" v-loading="loading" stripe>
          <el-table-column prop="invoiceNo" label="发票号" width="180" />
          <el-table-column prop="projectName" label="项目" min-width="160" />
          <el-table-column prop="invoiceTypeName" label="类型" width="120" />
          <el-table-column prop="amount" label="金额" width="110" />
          <el-table-column prop="taxAmount" label="税额" width="100" />
          <el-table-column prop="invoiceDate" label="日期" width="110" />
          <el-table-column prop="daysToExpire" label="到期天数" width="100">
            <template #default="{ row }"><el-tag v-if="row.daysToExpire !== null && row.daysToExpire <= 30" type="danger" size="small">{{ row.daysToExpire }}天</el-tag><span v-else>{{ row.daysToExpire ?? '-' }}</span></template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="invTotal" :page-size="invQuery.size" v-model:current-page="invQuery.page" @current-change="loadInvoices" />
      </el-tab-pane>

      <!-- Cost Tab -->
      <el-tab-pane label="成本汇总" name="cost">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input-number v-model="costProjectId" placeholder="项目ID" :min="1" /></el-form-item>
            <el-form-item><el-input v-model="costPeriod" placeholder="期间 如2024-03" clearable style="width:150px" /></el-form-item>
            <el-form-item><el-button type="primary" @click="loadCosts">查询</el-button></el-form-item>
            <el-form-item><el-button type="warning" v-permission="'finance:create'" @click="doAggregate">重新汇总</el-button></el-form-item>
          </el-form>
        </div>
        <el-table :data="costs" v-loading="loading" stripe show-summary :summary-method="costSummary">
          <el-table-column prop="categoryName" label="成本类别" min-width="180" />
          <el-table-column prop="amount" label="金额" width="150" />
          <el-table-column prop="period" label="期间" width="120" />
          <el-table-column prop="description" label="说明" min-width="200" />
        </el-table>
      </el-tab-pane>

      <!-- Receipt Tab -->
      <el-tab-pane label="收款登记" name="receipt">
        <div class="tab-header">
          <span></span>
          <el-button type="primary" v-permission="'finance:create'" @click="showReceiptForm = true">登记收款</el-button>
        </div>
        <el-alert title="收款登记后会自动更新合同已收金额" type="info" :closable="false" />
      </el-tab-pane>
    </el-tabs>

    <!-- Income Split Form -->
    <el-dialog v-model="showSplitForm" title="新建收入分解" width="500px">
      <el-form :model="splitForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="splitForm.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="合同ID"><el-input-number v-model="splitForm.contractId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="期间"><el-input v-model="splitForm.period" placeholder="如2024-03" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="splitForm.amount" :precision="2" :min="0.01" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSplitForm = false">取消</el-button>
        <el-button type="primary" @click="doCreateSplit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Payment Form -->
    <el-dialog v-model="showPayForm" title="新建付款" width="550px">
      <el-form :model="payForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="payForm.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="合同ID"><el-input-number v-model="payForm.contractId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="供应商ID"><el-input-number v-model="payForm.supplierId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="payForm.amount" :precision="2" :min="0.01" style="width:100%" /></el-form-item>
        <el-form-item label="银行名称"><el-input v-model="payForm.bankName" /></el-form-item>
        <el-form-item label="银行账号"><el-input v-model="payForm.bankAccount" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="payForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPayForm = false">取消</el-button>
        <el-button type="primary" @click="doCreatePayment">确定</el-button>
      </template>
    </el-dialog>

    <!-- Invoice Form -->
    <el-dialog v-model="showInvoiceForm" title="登记发票" width="550px">
      <el-form :model="invoiceForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="invoiceForm.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="合同ID"><el-input-number v-model="invoiceForm.contractId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="发票号"><el-input v-model="invoiceForm.invoiceNo" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="invoiceForm.invoiceType" style="width:100%">
            <el-option label="增值税专用发票" :value="1" /><el-option label="增值税普通发票" :value="2" /><el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额"><el-input-number v-model="invoiceForm.amount" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="税额"><el-input-number v-model="invoiceForm.taxAmount" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="开票日期"><el-date-picker v-model="invoiceForm.invoiceDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showInvoiceForm = false">取消</el-button>
        <el-button type="primary" @click="doCreateInvoice">确定</el-button>
      </template>
    </el-dialog>

    <!-- Receipt Form -->
    <el-dialog v-model="showReceiptForm" title="登记收款" width="500px">
      <el-form :model="receiptForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="receiptForm.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="合同ID"><el-input-number v-model="receiptForm.contractId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="receiptForm.amount" :precision="2" :min="0.01" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="receiptForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReceiptForm = false">取消</el-button>
        <el-button type="primary" @click="doRegisterReceipt">确定</el-button>
      </template>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveDialog" :title="isReject ? '驳回' : '审批通过'" width="450px">
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
import {
  getIncomeSplits, createIncomeSplit, approveIncomeSplit, rejectIncomeSplit,
  getReconciliations, generateReconciliation, approveReconciliation, rejectReconciliation,
  getPayments, createPayment, approvePayment, rejectPayment, markPaymentPaid,
  getInvoices, createInvoice, getExpiringInvoices,
  getCosts, aggregateCosts, registerReceipt
} from '@/api/finance'

const loading = ref(false)
const activeTab = ref('incomeSplit')

// Income splits
const splits = ref([]), splitTotal = ref(0)
const splitQuery = reactive({ projectId: null, status: null, page: 1, size: 20 })
const showSplitForm = ref(false)
const splitForm = reactive({ projectId: null, contractId: null, period: '', amount: 0 })

// Payments
const payments = ref([]), payTotal = ref(0)
const payQuery = reactive({ projectId: null, page: 1, size: 20 })
const showPayForm = ref(false)
const payForm = reactive({ projectId: null, contractId: null, supplierId: null, amount: 0, bankName: '', bankAccount: '', remark: '' })

// Reconciliations
const reconciliations = ref([]), reconTotal = ref(0)
const reconQuery = reactive({ period: '', page: 1, size: 20 })

// Invoices
const invoices = ref([]), invTotal = ref(0)
const invQuery = reactive({ projectId: null, page: 1, size: 20 })
const showInvoiceForm = ref(false)
const invoiceForm = reactive({ projectId: null, contractId: null, invoiceNo: '', invoiceType: 1, amount: 0, taxAmount: 0, invoiceDate: '' })

// Costs
const costs = ref([]), costProjectId = ref(null), costPeriod = ref('')

// Receipt
const showReceiptForm = ref(false)
const receiptForm = reactive({ projectId: null, contractId: null, amount: 0, remark: '' })

// Approve
const showApproveDialog = ref(false), approveComment = ref(''), isReject = ref(false)
const approvingType = ref(''), approvingId = ref(null)

const loadTab = () => {
  if (activeTab.value === 'incomeSplit') loadSplits()
  else if (activeTab.value === 'payment') loadPayments()
  else if (activeTab.value === 'reconciliation') loadReconciliations()
  else if (activeTab.value === 'invoice') loadInvoices()
  else if (activeTab.value === 'cost' && costProjectId.value) loadCosts()
}

const loadSplits = async () => { loading.value = true; try { const r = await getIncomeSplits(splitQuery); splits.value = r.data.records; splitTotal.value = r.data.total } finally { loading.value = false } }
const loadPayments = async () => { loading.value = true; try { const r = await getPayments(payQuery); payments.value = r.data.records; payTotal.value = r.data.total } finally { loading.value = false } }
const loadReconciliations = async () => { loading.value = true; try { const r = await getReconciliations(reconQuery); reconciliations.value = r.data.records; reconTotal.value = r.data.total } finally { loading.value = false } }
const loadInvoices = async () => { loading.value = true; try { const r = await getInvoices(invQuery); invoices.value = r.data.records; invTotal.value = r.data.total } finally { loading.value = false } }
const loadCosts = async () => { loading.value = true; try { const r = await getCosts(costProjectId.value, costPeriod.value); costs.value = r.data } finally { loading.value = false } }
const loadExpiring = async () => { loading.value = true; try { const r = await getExpiringInvoices(30); invoices.value = r.data; invTotal.value = r.data.length } finally { loading.value = false } }

const doCreateSplit = async () => { await createIncomeSplit(splitForm); ElMessage.success('创建成功'); showSplitForm.value = false; loadSplits() }
const doCreatePayment = async () => { await createPayment(payForm); ElMessage.success('创建成功'); showPayForm.value = false; loadPayments() }
const doCreateInvoice = async () => { await createInvoice(invoiceForm); ElMessage.success('登记成功'); showInvoiceForm.value = false; loadInvoices() }
const doRegisterReceipt = async () => { await registerReceipt(receiptForm); ElMessage.success('收款登记成功'); showReceiptForm.value = false }
const doGenerateReconciliation = async () => {
  const period = reconQuery.period || new Date().toISOString().slice(0, 7)
  await generateReconciliation(period); ElMessage.success('生成成功'); loadReconciliations()
}
const doMarkPaid = async (row) => { await markPaymentPaid(row.id); ElMessage.success('已标记付款'); loadPayments() }
const doAggregate = async () => { if (!costProjectId.value) { ElMessage.warning('请输入项目ID'); return } await aggregateCosts(costProjectId.value, costPeriod.value || new Date().toISOString().slice(0, 7)); ElMessage.success('汇总完成'); loadCosts() }

const openApprove = (type, row, reject) => { approvingType.value = type; approvingId.value = row.id; isReject.value = reject; approveComment.value = ''; showApproveDialog.value = true }

const doApproveAction = async () => {
  const fns = {
    split: [approveIncomeSplit, rejectIncomeSplit],
    payment: [approvePayment, rejectPayment],
    reconciliation: [approveReconciliation, rejectReconciliation]
  }
  const fn = fns[approvingType.value][isReject.value ? 1 : 0]
  await fn(approvingId.value, approveComment.value)
  ElMessage.success(isReject.value ? '已驳回' : '审批通过')
  showApproveDialog.value = false; loadTab()
}

const costSummary = ({ columns, data }) => {
  return columns.map((col, i) => {
    if (i === 0) return '合计'
    if (col.property === 'amount') return data.reduce((s, r) => s + (parseFloat(r.amount) || 0), 0).toFixed(2)
    return ''
  })
}

onMounted(loadSplits)
</script>

<style scoped>
.tab-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
</style>
