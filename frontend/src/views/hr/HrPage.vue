<template>
  <div class="hr-page">
    <el-tabs v-model="activeTab" @tab-click="loadTab">
      <!-- Employee Tab -->
      <el-tab-pane label="员工管理" name="employee">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input v-model="empQuery.keyword" placeholder="姓名/工号/手机" clearable style="width:200px" /></el-form-item>
            <el-form-item>
              <el-select v-model="empQuery.status" placeholder="状态" clearable style="width:100px">
                <el-option label="在职" :value="1" /><el-option label="离职" :value="2" /><el-option label="试用期" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary" @click="loadEmployees">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'hr:create'" @click="showOnboardDialog = true">入职登记</el-button>
        </div>
        <el-table :data="employees" v-loading="loading" stripe>
          <el-table-column prop="employeeNo" label="工号" width="130" />
          <el-table-column prop="realName" label="姓名" width="100" />
          <el-table-column prop="genderName" label="性别" width="60" />
          <el-table-column prop="deptName" label="部门" width="120" />
          <el-table-column prop="position" label="职位" width="120" />
          <el-table-column prop="phone" label="手机" width="130" />
          <el-table-column prop="entryDate" label="入职日期" width="120" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : row.status === 3 ? 'warning' : 'danger'" size="small">{{ row.statusName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="danger" v-if="row.status !== 2" v-permission="'hr:create'" @click="handleOffboard(row)">离职</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="empTotal" :page-size="empQuery.size" v-model:current-page="empQuery.page" @current-change="loadEmployees" />
      </el-tab-pane>

      <!-- Payroll Tab -->
      <el-tab-pane label="薪资管理" name="payroll">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input v-model="payPeriod" placeholder="月份(如2026-03)" style="width:150px" /></el-form-item>
            <el-form-item>
              <el-select v-model="payStatus" placeholder="状态" clearable style="width:120px">
                <el-option label="待调整" :value="1" /><el-option label="待审批" :value="2" />
                <el-option label="已审批" :value="4" /><el-option label="已发放" :value="5" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary" @click="loadPayrolls">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'hr:create'" @click="handleGeneratePayroll">生成月度工资</el-button>
        </div>
        <el-table :data="payrolls" v-loading="loading" stripe>
          <el-table-column prop="employeeName" label="员工" width="100" />
          <el-table-column prop="period" label="月份" width="100" />
          <el-table-column prop="baseSalary" label="基本工资" width="100" />
          <el-table-column prop="bonus" label="奖金" width="90" />
          <el-table-column prop="deduction" label="扣除" width="90" />
          <el-table-column prop="socialInsurance" label="社保" width="90" />
          <el-table-column prop="tax" label="个税" width="90" />
          <el-table-column prop="netSalary" label="实发" width="100" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }"><el-tag :type="row.status === 5 ? 'success' : row.status >= 2 ? 'warning' : 'info'" size="small">{{ row.statusName }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" v-if="row.status === 1" v-permission="'finance:approve'" @click="handleAdjust(row)">调整</el-button>
              <el-button link type="success" v-if="row.status === 2 || row.status === 3" v-permission="'finance:approve'" @click="handlePayApprove(row)">审批</el-button>
              <el-button link type="warning" v-if="row.status === 4" v-permission="'finance:approve'" @click="handleMarkPaid(row)">标记已发</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="payTotal" :page-size="20" v-model:current-page="payPage" @current-change="loadPayrolls" />
      </el-tab-pane>

      <!-- Reimbursement Tab -->
      <el-tab-pane label="报销管理" name="reimbursement">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item>
              <el-select v-model="reimbStatus" placeholder="状态" clearable style="width:120px">
                <el-option label="主管审批" :value="2" /><el-option label="财务审批" :value="3" />
                <el-option label="已完成" :value="5" /><el-option label="已驳回" :value="6" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary" @click="loadReimbursements">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'hr:create'" @click="showReimbDialog = true">新建报销</el-button>
        </div>
        <el-table :data="reimbursements" v-loading="loading" stripe>
          <el-table-column prop="reimburseNo" label="报销编号" width="160" />
          <el-table-column prop="employeeName" label="员工" width="100" />
          <el-table-column prop="category" label="类别" width="100" />
          <el-table-column prop="amount" label="金额" width="100" />
          <el-table-column prop="projectName" label="项目" width="150" />
          <el-table-column prop="description" label="说明" min-width="200" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 5 ? 'success' : row.status === 6 ? 'danger' : 'warning'" size="small">{{ row.statusName }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="success" v-if="row.status >= 2 && row.status <= 4" v-permission="'hr:approve'" @click="handleReimbApprove(row)">审批</el-button>
              <el-button link type="danger" v-if="row.status >= 2 && row.status <= 4" v-permission="'hr:approve'" @click="handleReimbReject(row)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="reimbTotal" :page-size="20" v-model:current-page="reimbPage" @current-change="loadReimbursements" />
      </el-tab-pane>

      <!-- Contract Tab -->
      <el-tab-pane label="劳动合同" name="contract">
        <div class="tab-header">
          <el-button type="warning" @click="loadExpiringContracts">查看即将到期(30天)</el-button>
          <el-button type="primary" v-permission="'hr:create'" @click="showContractDialog = true">新建合同</el-button>
        </div>
        <el-table :data="hrContracts" v-loading="loading" stripe>
          <el-table-column prop="employeeName" label="员工" width="100" />
          <el-table-column prop="contractTypeName" label="合同类型" width="120" />
          <el-table-column prop="startDate" label="开始日期" width="120" />
          <el-table-column prop="endDate" label="结束日期" width="120" />
          <el-table-column label="剩余天数" width="100">
            <template #default="{ row }"><el-tag :type="row.daysToExpire <= 30 ? 'danger' : 'success'" size="small" v-if="row.daysToExpire != null">{{ row.daysToExpire }}天</el-tag></template>
          </el-table-column>
          <el-table-column prop="statusName" label="状态" width="80" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" v-if="row.status === 1" v-permission="'hr:create'" @click="handleRenew(row)">续签</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Qualification Tab -->
      <el-tab-pane label="资质管理" name="qualification">
        <div class="tab-header">
          <el-button type="warning" @click="loadExpiringQuals">查看即将到期(30天)</el-button>
        </div>
        <el-table :data="qualifications" v-loading="loading" stripe>
          <el-table-column prop="employeeName" label="员工" width="100" />
          <el-table-column prop="qualName" label="资质名称" min-width="200" />
          <el-table-column prop="qualNo" label="证书编号" width="160" />
          <el-table-column prop="issueDate" label="颁发日期" width="120" />
          <el-table-column prop="expireDate" label="到期日期" width="120" />
          <el-table-column label="剩余天数" width="100">
            <template #default="{ row }"><el-tag :type="row.daysToExpire <= 30 ? 'danger' : 'success'" size="small" v-if="row.daysToExpire != null">{{ row.daysToExpire }}天</el-tag></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- Onboard Dialog -->
    <el-dialog v-model="showOnboardDialog" title="入职登记" width="600px">
      <el-form :model="onboardForm" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="onboardForm.realName" /></el-form-item>
        <el-form-item label="性别"><el-radio-group v-model="onboardForm.gender"><el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio></el-radio-group></el-form-item>
        <el-form-item label="手机"><el-input v-model="onboardForm.phone" /></el-form-item>
        <el-form-item label="身份证"><el-input v-model="onboardForm.idCard" /></el-form-item>
        <el-form-item label="部门ID"><el-input-number v-model="onboardForm.deptId" :min="1" /></el-form-item>
        <el-form-item label="职位"><el-input v-model="onboardForm.position" /></el-form-item>
        <el-form-item label="入职日期"><el-date-picker v-model="onboardForm.entryDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showOnboardDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doOnboard">确定</el-button>
      </template>
    </el-dialog>

    <!-- Reimbursement Dialog -->
    <el-dialog v-model="showReimbDialog" title="新建报销" width="600px">
      <el-form :model="reimbForm" label-width="80px">
        <el-form-item label="员工ID"><el-input-number v-model="reimbForm.employeeId" :min="1" /></el-form-item>
        <el-form-item label="类别">
          <el-select v-model="reimbForm.category">
            <el-option v-for="c in ['交通费','餐饮费','住宿费','办公用品','通讯费','其他']" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额"><el-input-number v-model="reimbForm.amount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="reimbForm.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReimbDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doCreateReimb">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmployees, onboardEmployee, offboardEmployee, getPayrolls, generatePayroll, adjustPayroll, approvePayroll, markPayrollPaid,
  getReimbursements, createReimbursement, approveReimbursement, rejectReimbursement,
  getHrContracts, getExpiringContracts, getQualifications, getExpiringQualifications } from '@/api/hr'

const loading = ref(false), submitting = ref(false)
const activeTab = ref('employee')

// Employee
const employees = ref([]), empTotal = ref(0), empQuery = reactive({ keyword: '', status: null, page: 1, size: 20 })
const showOnboardDialog = ref(false)
const onboardForm = reactive({ realName: '', gender: 1, phone: '', idCard: '', deptId: null, position: '', entryDate: '' })

// Payroll
const payrolls = ref([]), payTotal = ref(0), payPage = ref(1), payPeriod = ref(''), payStatus = ref(null)

// Reimbursement
const reimbursements = ref([]), reimbTotal = ref(0), reimbPage = ref(1), reimbStatus = ref(null)
const showReimbDialog = ref(false)
const reimbForm = reactive({ employeeId: null, category: '交通费', amount: null, description: '' })

// Contract & Qualification
const hrContracts = ref([]), qualifications = ref([])
const showContractDialog = ref(false)

const loadTab = () => {
  if (activeTab.value === 'employee') loadEmployees()
  else if (activeTab.value === 'payroll') loadPayrolls()
  else if (activeTab.value === 'reimbursement') loadReimbursements()
  else if (activeTab.value === 'contract') loadContracts()
  else loadQualifications()
}

const loadEmployees = async () => { loading.value = true; try { const r = await getEmployees(empQuery); employees.value = r.data.records; empTotal.value = r.data.total } finally { loading.value = false } }
const loadPayrolls = async () => { loading.value = true; try { const r = await getPayrolls({ period: payPeriod.value, status: payStatus.value, page: payPage.value }); payrolls.value = r.data.records; payTotal.value = r.data.total } finally { loading.value = false } }
const loadReimbursements = async () => { loading.value = true; try { const r = await getReimbursements({ status: reimbStatus.value, page: reimbPage.value }); reimbursements.value = r.data.records; reimbTotal.value = r.data.total } finally { loading.value = false } }
const loadContracts = async () => { loading.value = true; try { const r = await getHrContracts({}); hrContracts.value = r.data } finally { loading.value = false } }
const loadQualifications = async () => { loading.value = true; try { const r = await getQualifications({}); qualifications.value = r.data } finally { loading.value = false } }
const loadExpiringContracts = async () => { const r = await getExpiringContracts(30); hrContracts.value = r.data; ElMessage.info(`${r.data.length}个合同即将到期`) }
const loadExpiringQuals = async () => { const r = await getExpiringQualifications(30); qualifications.value = r.data; ElMessage.info(`${r.data.length}个资质即将到期`) }

const doOnboard = async () => {
  submitting.value = true
  try { await onboardEmployee(onboardForm); ElMessage.success('入职登记成功'); showOnboardDialog.value = false; loadEmployees() }
  finally { submitting.value = false }
}

const handleOffboard = (row) => {
  ElMessageBox.confirm(`确定将 ${row.realName} 办理离职？`, '提示', { type: 'warning' }).then(async () => {
    await offboardEmployee(row.id); ElMessage.success('离职办理成功'); loadEmployees()
  }).catch(() => {})
}

const handleGeneratePayroll = () => {
  ElMessageBox.prompt('请输入月份 (如 2026-04)', '生成月度工资').then(async ({ value }) => {
    await generatePayroll(value); ElMessage.success('工资已生成'); payPeriod.value = value; loadPayrolls()
  }).catch(() => {})
}

const handleAdjust = (row) => {
  ElMessageBox.prompt('输入奖金金额', '调整工资').then(async ({ value }) => {
    await adjustPayroll(row.id, { bonus: parseFloat(value) || 0 }); ElMessage.success('已调整'); loadPayrolls()
  }).catch(() => {})
}

const handlePayApprove = (row) => {
  ElMessageBox.prompt('审批意见', '工资审批').then(async ({ value }) => {
    await approvePayroll(row.id, value); ElMessage.success('审批通过'); loadPayrolls()
  }).catch(() => {})
}

const handleMarkPaid = (row) => {
  ElMessageBox.confirm('确定标记为已发放？').then(async () => {
    await markPayrollPaid(row.id); ElMessage.success('已标记'); loadPayrolls()
  }).catch(() => {})
}

const doCreateReimb = async () => {
  submitting.value = true
  try { await createReimbursement(reimbForm); ElMessage.success('报销提交成功'); showReimbDialog.value = false; loadReimbursements() }
  finally { submitting.value = false }
}

const handleReimbApprove = (row) => {
  ElMessageBox.prompt('审批意见', '报销审批').then(async ({ value }) => {
    await approveReimbursement(row.id, value); ElMessage.success('审批通过'); loadReimbursements()
  }).catch(() => {})
}

const handleReimbReject = (row) => {
  ElMessageBox.prompt('驳回原因(至少5字)', '驳回').then(async ({ value }) => {
    await rejectReimbursement(row.id, value); ElMessage.success('已驳回'); loadReimbursements()
  }).catch(() => {})
}

const handleRenew = (row) => { ElMessage.info('续签功能请通过API调用') }

onMounted(loadEmployees)
</script>

<style scoped>
.tab-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
</style>
