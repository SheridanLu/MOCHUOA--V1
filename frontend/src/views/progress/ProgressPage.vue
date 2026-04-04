<template>
  <div class="progress-page">
    <el-tabs v-model="activeTab" @tab-click="loadTab">
      <!-- Plans Tab -->
      <el-tab-pane label="进度计划" name="plan">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input-number v-model="planProjectId" placeholder="项目ID" :min="0" /></el-form-item>
            <el-form-item><el-button type="primary" @click="loadPlans">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'progress:create'" @click="showPlanForm = true">新建计划</el-button>
        </div>
        <el-table :data="plans" v-loading="loading" stripe>
          <el-table-column prop="planName" label="计划名称" min-width="200" />
          <el-table-column prop="projectName" label="项目" min-width="180" />
          <el-table-column prop="startDate" label="开始日期" width="120" />
          <el-table-column prop="endDate" label="结束日期" width="120" />
          <el-table-column prop="taskCount" label="任务数" width="80" />
          <el-table-column prop="overallProgress" label="总进度" width="120">
            <template #default="{ row }"><el-progress :percentage="row.overallProgress || 0" :stroke-width="14" text-inside /></template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 2 ? 'success' : row.status === 1 ? 'warning' : 'info'" size="small">{{ row.statusName || ['草稿','进行中','已完成'][row.status] }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="viewGantt(row)">甘特图</el-button>
              <el-button link type="success" v-if="row.status === 0" v-permission="'progress:approve'" @click="doApprovePlan(row)">审批</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tasks / Gantt Tab -->
      <el-tab-pane label="任务管理" name="task">
        <div class="tab-header" v-if="selectedPlan">
          <span style="font-size:16px;font-weight:bold">{{ selectedPlan.planName }} - 任务列表</span>
          <el-button type="primary" v-permission="'progress:create'" @click="showTaskForm = true">添加任务</el-button>
        </div>
        <el-alert v-else title="请先在「进度计划」选项卡中选择一个计划查看任务" type="info" :closable="false" />
        <el-table v-if="selectedPlan" :data="tasks" v-loading="loading" stripe row-key="id" :tree-props="{ children: 'children' }">
          <el-table-column prop="taskName" label="任务名称" min-width="200" />
          <el-table-column prop="assigneeName" label="负责人" width="100" />
          <el-table-column prop="startDate" label="开始" width="110" />
          <el-table-column prop="endDate" label="结束" width="110" />
          <el-table-column prop="progress" label="进度" width="120">
            <template #default="{ row }"><el-progress :percentage="row.progress" :stroke-width="14" text-inside /></template>
          </el-table-column>
          <el-table-column label="里程碑" width="80">
            <template #default="{ row }"><el-tag v-if="row.milestone" type="danger" size="small">里程碑</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" v-permission="'progress:create'" @click="openProgressUpdate(row)">更新进度</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Deviations Tab -->
      <el-tab-pane label="偏差分析" name="deviation">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input-number v-model="devProjectId" placeholder="项目ID" :min="0" /></el-form-item>
            <el-form-item><el-button type="primary" @click="loadDeviations">查询</el-button></el-form-item>
          </el-form>
          <el-button type="warning" v-permission="'progress:approve'" @click="doScanDeviations">扫描偏差</el-button>
        </div>
        <el-table :data="deviations" v-loading="loading" stripe>
          <el-table-column prop="taskName" label="任务" min-width="200" />
          <el-table-column prop="projectName" label="项目" width="180" />
          <el-table-column prop="deviationDays" label="偏差天数" width="110">
            <template #default="{ row }"><el-tag :type="row.deviationDays > 0 ? 'danger' : 'success'" size="small">{{ row.deviationDays > 0 ? '+' : '' }}{{ row.deviationDays }}天</el-tag></template>
          </el-table-column>
          <el-table-column prop="deviationTypeName" label="类型" width="100" />
          <el-table-column prop="description" label="说明" min-width="200" />
          <el-table-column prop="createdAt" label="检测时间" width="160" />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- Create Plan Dialog -->
    <el-dialog v-model="showPlanForm" title="新建进度计划" width="500px">
      <el-form :model="planForm" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="planForm.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="计划名称"><el-input v-model="planForm.planName" /></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="planForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="planForm.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPlanForm = false">取消</el-button>
        <el-button type="primary" @click="doCreatePlan">确定</el-button>
      </template>
    </el-dialog>

    <!-- Create Task Dialog -->
    <el-dialog v-model="showTaskForm" title="添加任务" width="550px">
      <el-form :model="taskForm" label-width="80px">
        <el-form-item label="任务名称"><el-input v-model="taskForm.taskName" /></el-form-item>
        <el-form-item label="父任务ID"><el-input-number v-model="taskForm.parentTaskId" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="负责人ID"><el-input-number v-model="taskForm.assigneeId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="taskForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="taskForm.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="里程碑"><el-switch v-model="taskForm.milestone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskForm = false">取消</el-button>
        <el-button type="primary" @click="doCreateTask">确定</el-button>
      </template>
    </el-dialog>

    <!-- Progress Update Dialog -->
    <el-dialog v-model="showProgressDialog" title="更新进度" width="400px">
      <el-form label-width="80px">
        <el-form-item label="进度"><el-slider v-model="progressVal" :max="100" show-input /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProgressDialog = false">取消</el-button>
        <el-button type="primary" @click="doUpdateProgress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPlans, createPlan, approvePlan, getGanttTasks, createTask, updateProgress, getDeviations, scanDeviations } from '@/api/progress'

const loading = ref(false)
const activeTab = ref('plan')
const plans = ref([]), planProjectId = ref(null)
const tasks = ref([]), selectedPlan = ref(null)
const deviations = ref([]), devProjectId = ref(null)
const showPlanForm = ref(false), showTaskForm = ref(false), showProgressDialog = ref(false)
const planForm = reactive({ projectId: null, planName: '', startDate: '', endDate: '' })
const taskForm = reactive({ taskName: '', parentTaskId: null, assigneeId: null, startDate: '', endDate: '', milestone: false })
const progressVal = ref(0), updatingTaskId = ref(null)

const loadTab = () => {
  if (activeTab.value === 'plan') loadPlans()
  else if (activeTab.value === 'deviation') loadDeviations()
}

const loadPlans = async () => { loading.value = true; try { const r = await getPlans({ projectId: planProjectId.value }); plans.value = r.data } finally { loading.value = false } }
const loadDeviations = async () => { loading.value = true; try { const r = await getDeviations({ projectId: devProjectId.value }); deviations.value = r.data } finally { loading.value = false } }

const viewGantt = async (plan) => { selectedPlan.value = plan; activeTab.value = 'task'; loading.value = true; try { const r = await getGanttTasks(plan.id); tasks.value = r.data } finally { loading.value = false } }

const doCreatePlan = async () => { await createPlan(planForm); ElMessage.success('创建成功'); showPlanForm.value = false; loadPlans() }
const doApprovePlan = async (row) => { await approvePlan(row.id, '同意'); ElMessage.success('审批通过'); loadPlans() }
const doCreateTask = async () => { taskForm.planId = selectedPlan.value.id; taskForm.projectId = selectedPlan.value.projectId; await createTask(taskForm); ElMessage.success('添加成功'); showTaskForm.value = false; viewGantt(selectedPlan.value) }

const openProgressUpdate = (row) => { updatingTaskId.value = row.id; progressVal.value = row.progress; showProgressDialog.value = true }
const doUpdateProgress = async () => { await updateProgress({ taskId: updatingTaskId.value, progress: progressVal.value }); ElMessage.success('更新成功'); showProgressDialog.value = false; viewGantt(selectedPlan.value) }

const doScanDeviations = async () => { if (!devProjectId.value) { ElMessage.warning('请输入项目ID'); return } await scanDeviations(devProjectId.value); ElMessage.success('扫描完成'); loadDeviations() }

onMounted(loadPlans)
</script>

<style scoped>
.tab-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
</style>
