<template>
  <div class="report-page">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 项目成本 -->
      <el-tab-pane label="项目成本" name="cost">
        <div class="tab-toolbar">
          <el-form inline>
            <el-form-item label="项目">
              <el-input v-model="costQuery.projectId" placeholder="项目ID（可选）" clearable style="width:140px" />
            </el-form-item>
            <el-form-item label="期间">
              <el-input v-model="costQuery.period" placeholder="如 2026-03" clearable style="width:130px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadCostReport">查询</el-button>
              <el-button type="success" :icon="Download" @click="exportReport('project_cost')">导出</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="costData" v-loading="costLoading" stripe border>
          <el-table-column prop="projectId" label="项目ID" width="80" />
          <el-table-column prop="projectName" label="项目名称" min-width="150" />
          <el-table-column prop="materialCost" label="材料费" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.materialCost) }}</template>
          </el-table-column>
          <el-table-column prop="laborCost" label="人工费" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.laborCost) }}</template>
          </el-table-column>
          <el-table-column prop="equipmentCost" label="设备费" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.equipmentCost) }}</template>
          </el-table-column>
          <el-table-column prop="managementCost" label="管理费" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.managementCost) }}</template>
          </el-table-column>
          <el-table-column prop="otherCost" label="其他" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.otherCost) }}</template>
          </el-table-column>
          <el-table-column prop="totalCost" label="总费用" width="130" align="right">
            <template #default="{ row }"><b>{{ formatMoney(row.totalCost) }}</b></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 收支对比 -->
      <el-tab-pane label="收支对比" name="income_expense">
        <div class="tab-toolbar">
          <el-form inline>
            <el-form-item label="项目">
              <el-input v-model="ieQuery.projectId" placeholder="项目ID（可选）" clearable style="width:140px" />
            </el-form-item>
            <el-form-item label="期间">
              <el-input v-model="ieQuery.period" placeholder="如 2026-03" clearable style="width:130px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadIncomeExpenseReport">查询</el-button>
              <el-button type="success" :icon="Download" @click="exportReport('income_expense')">导出</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="ieData" v-loading="ieLoading" stripe border>
          <el-table-column prop="projectId" label="项目ID" width="80" />
          <el-table-column prop="projectName" label="项目名称" min-width="150" />
          <el-table-column prop="totalIncome" label="总收入" width="130" align="right">
            <template #default="{ row }"><span style="color:#67C23A">{{ formatMoney(row.totalIncome) }}</span></template>
          </el-table-column>
          <el-table-column prop="totalExpense" label="总支出" width="130" align="right">
            <template #default="{ row }"><span style="color:#F56C6C">{{ formatMoney(row.totalExpense) }}</span></template>
          </el-table-column>
          <el-table-column prop="profit" label="利润" width="130" align="right">
            <template #default="{ row }">
              <span :style="{ color: row.profit >= 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
                {{ formatMoney(row.profit) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="profitRate" label="利润率(%)" width="110" align="right">
            <template #default="{ row }">{{ row.profitRate }}%</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 采购统计 -->
      <el-tab-pane label="采购统计" name="procurement">
        <div class="tab-toolbar">
          <el-form inline>
            <el-form-item label="项目">
              <el-input v-model="procQuery.projectId" placeholder="项目ID（可选）" clearable style="width:140px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadProcurementReport">查询</el-button>
              <el-button type="success" :icon="Download" @click="exportReport('procurement')">导出</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="procData" v-loading="procLoading" stripe border>
          <el-table-column prop="projectId" label="项目ID" width="80" />
          <el-table-column prop="projectName" label="项目名称" min-width="200" />
          <el-table-column prop="totalItems" label="采购项数" width="100" align="right" />
          <el-table-column prop="totalAmount" label="采购总额" width="140" align="right">
            <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
          </el-table-column>
          <el-table-column prop="completedRate" label="完成率(%)" width="110" align="right">
            <template #default="{ row }">{{ row.completedRate }}%</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 库存统计 -->
      <el-tab-pane label="库存统计" name="inventory">
        <div class="tab-toolbar">
          <el-form inline>
            <el-form-item label="项目">
              <el-input v-model="invQuery.projectId" placeholder="项目ID（可选）" clearable style="width:140px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadInventoryReport">查询</el-button>
              <el-button type="success" :icon="Download" @click="exportReport('inventory')">导出</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="invData" v-loading="invLoading" stripe border>
          <el-table-column prop="projectId" label="项目ID" width="80" />
          <el-table-column prop="projectName" label="项目名称" min-width="200" />
          <el-table-column prop="materialCount" label="物资种类" width="100" align="right" />
          <el-table-column prop="totalQuantity" label="总数量" width="120" align="right" />
          <el-table-column prop="totalValue" label="总价值" width="140" align="right">
            <template #default="{ row }">{{ formatMoney(row.totalValue) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 报表快照 -->
      <el-tab-pane label="报表快照" name="cached">
        <div class="tab-toolbar">
          <el-form inline>
            <el-form-item label="报表类型">
              <el-select v-model="cachedType" placeholder="全部" clearable style="width:160px">
                <el-option label="项目成本" value="project_cost" />
                <el-option label="收支对比" value="income_expense" />
                <el-option label="采购统计" value="procurement" />
                <el-option label="库存统计" value="inventory" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadCachedReports">查询</el-button>
            </el-form-item>
            <el-form-item label="预计算">
              <el-input v-model="preComputePeriod" placeholder="如 2026-03" style="width:130px" />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="handlePreCompute">生成快照</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="cachedList" v-loading="cachedLoading" stripe border>
          <el-table-column prop="reportType" label="报表类型" width="140">
            <template #default="{ row }">{{ reportTypeLabel(row.reportType) }}</template>
          </el-table-column>
          <el-table-column prop="reportName" label="报表名称" width="180" />
          <el-table-column prop="period" label="期间" width="120" />
          <el-table-column prop="generatedAt" label="生成时间" width="170" />
          <el-table-column label="数据条数" width="100" align="right">
            <template #default="{ row }">{{ Array.isArray(row.data) ? row.data.length : '-' }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import {
  getCostReport, getIncomeExpenseReport, getProcurementReport,
  getInventoryReport, getCachedReports, preComputeReports, exportReportExcel
} from '@/api/report'

const activeTab = ref('cost')

// Cost
const costQuery = reactive({ projectId: '', period: '' })
const costData = ref([])
const costLoading = ref(false)

const loadCostReport = async () => {
  costLoading.value = true
  try {
    const res = await getCostReport(costQuery)
    costData.value = res.data.data || []
  } catch { ElMessage.error('加载成本报表失败') }
  finally { costLoading.value = false }
}

// Income/Expense
const ieQuery = reactive({ projectId: '', period: '' })
const ieData = ref([])
const ieLoading = ref(false)

const loadIncomeExpenseReport = async () => {
  ieLoading.value = true
  try {
    const res = await getIncomeExpenseReport(ieQuery)
    ieData.value = res.data.data || []
  } catch { ElMessage.error('加载收支报表失败') }
  finally { ieLoading.value = false }
}

// Procurement
const procQuery = reactive({ projectId: '' })
const procData = ref([])
const procLoading = ref(false)

const loadProcurementReport = async () => {
  procLoading.value = true
  try {
    const res = await getProcurementReport(procQuery)
    procData.value = res.data.data || []
  } catch { ElMessage.error('加载采购报表失败') }
  finally { procLoading.value = false }
}

// Inventory
const invQuery = reactive({ projectId: '' })
const invData = ref([])
const invLoading = ref(false)

const loadInventoryReport = async () => {
  invLoading.value = true
  try {
    const res = await getInventoryReport(invQuery)
    invData.value = res.data.data || []
  } catch { ElMessage.error('加载库存报表失败') }
  finally { invLoading.value = false }
}

// Cached
const cachedType = ref('')
const cachedList = ref([])
const cachedLoading = ref(false)
const preComputePeriod = ref('')

const loadCachedReports = async () => {
  cachedLoading.value = true
  try {
    const res = await getCachedReports(cachedType.value)
    cachedList.value = res.data || []
  } catch { ElMessage.error('加载快照失败') }
  finally { cachedLoading.value = false }
}

const handlePreCompute = async () => {
  if (!preComputePeriod.value) {
    ElMessage.warning('请输入期间')
    return
  }
  try {
    await preComputeReports(preComputePeriod.value)
    ElMessage.success('快照生成成功')
    loadCachedReports()
  } catch { ElMessage.error('快照生成失败') }
}

const exportReport = async (reportType) => {
  try {
    const params = reportType === 'project_cost' ? costQuery
      : reportType === 'income_expense' ? ieQuery
      : reportType === 'procurement' ? procQuery
      : invQuery
    const res = await exportReportExcel(reportType, params)
    const blob = new Blob([res.data])
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${reportType}_${new Date().toISOString().slice(0,10)}.xls`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}

const formatMoney = (val) => {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const reportTypeLabel = (type) => {
  const map = { project_cost: '项目成本', income_expense: '收支对比', procurement: '采购统计', inventory: '库存统计' }
  return map[type] || type
}

onMounted(() => loadCostReport())
</script>

<style scoped lang="scss">
.report-page {
  padding: 0;
}
.tab-toolbar {
  margin-bottom: 16px;
}
</style>
