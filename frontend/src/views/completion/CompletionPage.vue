<template>
  <div class="completion-page">
    <el-tabs v-model="activeTab" @tab-click="loadTab">
      <!-- Completion Reports -->
      <el-tab-pane label="完工申请" name="report">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input-number v-model="reportQuery.projectId" placeholder="项目ID" :min="0" /></el-form-item>
            <el-form-item>
              <el-select v-model="reportQuery.status" placeholder="状态" clearable style="width:120px">
                <el-option label="待审批" :value="1" /><el-option label="已通过" :value="3" /><el-option label="已驳回" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary" @click="loadReports">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'completion:create'" @click="showReportForm = true">新建完工申请</el-button>
        </div>
        <el-table :data="reports" v-loading="loading" stripe>
          <el-table-column prop="reportNo" label="编号" width="160" />
          <el-table-column prop="projectName" label="项目" min-width="180" />
          <el-table-column prop="completionDate" label="完工日期" width="120" />
          <el-table-column prop="qualityRating" label="质量评级" width="100">
            <template #default="{ row }">{{ ['','优','良','合格','不合格'][row.qualityRating] || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 3 ? 'success' : row.status === 4 ? 'danger' : 'warning'" size="small">{{ row.statusName }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 1">
                <el-button link type="success" v-permission="'completion:approve'" @click="openApprove('report', row, false)">通过</el-button>
                <el-button link type="danger" v-permission="'completion:approve'" @click="openApprove('report', row, true)">驳回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="reportTotal" :page-size="reportQuery.size" v-model:current-page="reportQuery.page" @current-change="loadReports" />
      </el-tab-pane>

      <!-- Drawings -->
      <el-tab-pane label="竣工图纸" name="drawing">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input-number v-model="drawingProjectId" placeholder="项目ID" :min="1" /></el-form-item>
            <el-form-item><el-button type="primary" @click="loadDrawings">查询</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'completion:create'" @click="showDrawingForm = true">上传图纸</el-button>
        </div>
        <el-table :data="drawings" v-loading="loading" stripe>
          <el-table-column prop="drawingName" label="图纸名称" min-width="200" />
          <el-table-column prop="drawingType" label="类型" width="120" />
          <el-table-column prop="version" label="版本" width="80">
            <template #default="{ row }"><el-tag size="small">v{{ row.version }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="uploaderName" label="上传者" width="100" />
          <el-table-column prop="createdAt" label="上传时间" width="160" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" v-permission="'completion:create'" @click="doUploadVersion(row)">上传新版</el-button>
              <el-button link type="primary" @click="window.open(row.fileUrl)">下载</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Archive -->
      <el-tab-pane label="文档归档" name="archive">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input-number v-model="archiveProjectId" placeholder="项目ID" :min="1" /></el-form-item>
            <el-form-item><el-button type="primary" @click="loadArchive">加载归档</el-button></el-form-item>
          </el-form>
        </div>
        <template v-if="archive">
          <el-tabs v-model="archiveCategory" type="card">
            <el-tab-pane v-for="(items, cat) in archive.categories" :key="cat" :label="`${cat} (${items.length})`" :name="cat">
              <el-table :data="items" stripe size="small">
                <el-table-column prop="fileName" label="文件名" min-width="250" />
                <el-table-column prop="source" label="来源" width="120" />
                <el-table-column prop="uploadedAt" label="时间" width="160" />
                <el-table-column label="操作" width="100">
                  <template #default="{ row }">
                    <el-button link type="primary" v-if="row.fileUrl" @click="window.open(row.fileUrl)">下载</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </template>
        <el-empty v-else description="请输入项目ID后加载归档" />
      </el-tab-pane>

      <!-- Settlement -->
      <el-tab-pane label="工程结算" name="settlement">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input-number v-model="settlementProjectId" placeholder="项目ID" :min="1" /></el-form-item>
            <el-form-item><el-button type="primary" @click="loadSettlement">查询</el-button></el-form-item>
          </el-form>
          <el-button type="warning" v-permission="'completion:create'" @click="doGenerateSettlement">生成结算</el-button>
        </div>
        <el-descriptions v-if="settlement" :column="2" border>
          <el-descriptions-item label="结算编号">{{ settlement.settlementNo }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="settlement.status === 3 ? 'success' : 'warning'" size="small">{{ settlement.statusName }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="合同金额">{{ settlement.contractAmount }}</el-descriptions-item>
          <el-descriptions-item label="变更金额">{{ settlement.changeAmount }}</el-descriptions-item>
          <el-descriptions-item label="最终金额"><span style="font-size:18px;font-weight:bold;color:#409EFF">{{ settlement.finalAmount }}</span></el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="暂无结算数据" />
      </el-tab-pane>

      <!-- Labor Settlement -->
      <el-tab-pane label="劳务结算" name="laborSettlement">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input-number v-model="laborQuery.projectId" placeholder="项目ID" :min="0" /></el-form-item>
            <el-form-item><el-button type="primary" @click="loadLaborSettlements">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'completion:create'" @click="showLaborForm = true">新建劳务结算</el-button>
        </div>
        <el-table :data="laborSettlements" v-loading="loading" stripe>
          <el-table-column prop="settlementNo" label="编号" width="160" />
          <el-table-column prop="projectName" label="项目" min-width="160" />
          <el-table-column prop="teamName" label="班组" width="120" />
          <el-table-column prop="laborType" label="工种" width="100" />
          <el-table-column prop="totalDays" label="总工日" width="80" />
          <el-table-column prop="dailyRate" label="日单价" width="100" />
          <el-table-column prop="totalAmount" label="总额" width="110" />
          <el-table-column prop="deduction" label="扣款" width="100" />
          <el-table-column prop="finalAmount" label="结算额" width="110" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 3 ? 'success' : row.status === 4 ? 'danger' : 'warning'" size="small">{{ row.statusName }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 1">
                <el-button link type="success" v-permission="'completion:approve'" @click="openApprove('labor', row, false)">通过</el-button>
                <el-button link type="danger" v-permission="'completion:approve'" @click="openApprove('labor', row, true)">驳回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="laborTotal" :page-size="laborQuery.size" v-model:current-page="laborQuery.page" @current-change="loadLaborSettlements" />
      </el-tab-pane>
    </el-tabs>

    <!-- Report Form -->
    <el-dialog v-model="showReportForm" title="新建完工申请" width="550px">
      <el-form :model="reportForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="reportForm.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="完工日期"><el-date-picker v-model="reportForm.completionDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="质量评级">
          <el-select v-model="reportForm.qualityRating" style="width:100%">
            <el-option label="优" :value="1" /><el-option label="良" :value="2" /><el-option label="合格" :value="3" /><el-option label="不合格" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要"><el-input v-model="reportForm.summary" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="附件URL"><el-input v-model="reportForm.fileUrl" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReportForm = false">取消</el-button>
        <el-button type="primary" @click="doCreateReport">确定</el-button>
      </template>
    </el-dialog>

    <!-- Drawing Upload Form -->
    <el-dialog v-model="showDrawingForm" title="上传竣工图纸" width="600px">
      <el-form :model="drawingForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="drawingForm.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item v-for="(item, idx) in drawingForm.drawings" :key="idx" :label="'图纸'+(idx+1)">
          <div style="display:flex;gap:8px;width:100%">
            <el-input v-model="item.drawingName" placeholder="名称" style="flex:2" />
            <el-input v-model="item.drawingType" placeholder="类型" style="flex:1" />
            <el-input v-model="item.fileUrl" placeholder="文件URL" style="flex:2" />
            <el-button type="danger" link @click="drawingForm.drawings.splice(idx, 1)" v-if="drawingForm.drawings.length > 1">删除</el-button>
          </div>
        </el-form-item>
        <el-form-item><el-button type="primary" link @click="drawingForm.drawings.push({ drawingName:'', drawingType:'', fileUrl:'' })" :disabled="drawingForm.drawings.length >= 10">+ 添加图纸</el-button><span style="color:#999;margin-left:8px">最多10个</span></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDrawingForm = false">取消</el-button>
        <el-button type="primary" @click="doUploadDrawings">上传</el-button>
      </template>
    </el-dialog>

    <!-- Labor Settlement Form -->
    <el-dialog v-model="showLaborForm" title="新建劳务结算" width="550px">
      <el-form :model="laborForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="laborForm.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="班组名称"><el-input v-model="laborForm.teamName" /></el-form-item>
        <el-form-item label="工种"><el-input v-model="laborForm.laborType" /></el-form-item>
        <el-form-item label="总工日"><el-input-number v-model="laborForm.totalDays" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="日单价"><el-input-number v-model="laborForm.dailyRate" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="扣款"><el-input-number v-model="laborForm.deduction" :precision="2" :min="0" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLaborForm = false">取消</el-button>
        <el-button type="primary" @click="doCreateLabor">确定</el-button>
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
  getCompletionReports, createCompletionReport, approveReport, rejectReport,
  getDrawings, uploadDrawings, uploadDrawingVersion,
  getArchive, getSettlement, generateSettlement,
  getLaborSettlements, createLaborSettlement, approveLaborSettlement, rejectLaborSettlement
} from '@/api/completion'

const loading = ref(false)
const activeTab = ref('report')

const reports = ref([]), reportTotal = ref(0)
const reportQuery = reactive({ projectId: null, status: null, page: 1, size: 20 })
const showReportForm = ref(false)
const reportForm = reactive({ projectId: null, completionDate: '', qualityRating: null, summary: '', fileUrl: '' })

const drawings = ref([]), drawingProjectId = ref(null)
const showDrawingForm = ref(false)
const drawingForm = reactive({ projectId: null, drawings: [{ drawingName: '', drawingType: '', fileUrl: '' }] })

const archive = ref(null), archiveProjectId = ref(null), archiveCategory = ref('合同文件')
const settlement = ref(null), settlementProjectId = ref(null)

const laborSettlements = ref([]), laborTotal = ref(0)
const laborQuery = reactive({ projectId: null, page: 1, size: 20 })
const showLaborForm = ref(false)
const laborForm = reactive({ projectId: null, teamName: '', laborType: '', totalDays: 1, dailyRate: 0, deduction: 0 })

const showApproveDialog = ref(false), approveComment = ref(''), isReject = ref(false)
const approvingType = ref(''), approvingId = ref(null)

const loadTab = () => {
  if (activeTab.value === 'report') loadReports()
  else if (activeTab.value === 'drawing' && drawingProjectId.value) loadDrawings()
  else if (activeTab.value === 'laborSettlement') loadLaborSettlements()
}

const loadReports = async () => { loading.value = true; try { const r = await getCompletionReports(reportQuery); reports.value = r.data.records; reportTotal.value = r.data.total } finally { loading.value = false } }
const loadDrawings = async () => { if (!drawingProjectId.value) return; loading.value = true; try { const r = await getDrawings(drawingProjectId.value); drawings.value = r.data } finally { loading.value = false } }
const loadArchive = async () => { if (!archiveProjectId.value) return; loading.value = true; try { const r = await getArchive(archiveProjectId.value); archive.value = r.data } finally { loading.value = false } }
const loadSettlement = async () => { if (!settlementProjectId.value) return; loading.value = true; try { const r = await getSettlement(settlementProjectId.value); settlement.value = r.data } finally { loading.value = false } }
const loadLaborSettlements = async () => { loading.value = true; try { const r = await getLaborSettlements(laborQuery); laborSettlements.value = r.data.records; laborTotal.value = r.data.total } finally { loading.value = false } }

const doCreateReport = async () => { await createCompletionReport(reportForm); ElMessage.success('完工申请已提交'); showReportForm.value = false; loadReports() }
const doUploadDrawings = async () => { await uploadDrawings(drawingForm); ElMessage.success('图纸已上传'); showDrawingForm.value = false; loadDrawings() }
const doUploadVersion = async (row) => { const url = prompt('请输入新版图纸URL'); if (url) { await uploadDrawingVersion(row.id, url); ElMessage.success('新版本已上传'); loadDrawings() } }
const doGenerateSettlement = async () => { if (!settlementProjectId.value) { ElMessage.warning('请输入项目ID'); return } await generateSettlement(settlementProjectId.value); ElMessage.success('结算已生成'); loadSettlement() }
const doCreateLabor = async () => { await createLaborSettlement(laborForm); ElMessage.success('劳务结算已创建'); showLaborForm.value = false; loadLaborSettlements() }

const openApprove = (type, row, reject) => { approvingType.value = type; approvingId.value = row.id; isReject.value = reject; approveComment.value = ''; showApproveDialog.value = true }
const doApproveAction = async () => {
  const fns = { report: [approveReport, rejectReport], labor: [approveLaborSettlement, rejectLaborSettlement] }
  await fns[approvingType.value][isReject.value ? 1 : 0](approvingId.value, approveComment.value)
  ElMessage.success(isReject.value ? '已驳回' : '审批通过'); showApproveDialog.value = false; loadTab()
}

onMounted(loadReports)
</script>

<style scoped>
.tab-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
</style>
