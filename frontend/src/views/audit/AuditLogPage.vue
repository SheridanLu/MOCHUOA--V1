<template>
  <div class="audit-log-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>审计日志</span>
          <div>
            <el-button type="success" :icon="Download" @click="handleExportExcel">导出Excel</el-button>
            <el-button type="info" :icon="Download" @click="handleExportJson">导出JSON</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索区 -->
      <el-form :model="queryForm" inline class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="模糊搜索" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="queryForm.module" placeholder="全部" clearable style="width:120px">
            <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作">
          <el-select v-model="queryForm.action" placeholder="全部" clearable style="width:120px">
            <el-option v-for="a in actions" :key="a" :label="a" :value="a" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标类型">
          <el-input v-model="queryForm.targetType" placeholder="如 PROJECT" clearable style="width:130px" />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width:240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadLogs">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="logList" v-loading="loading" stripe border style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="action" label="操作" width="90">
          <template #default="{ row }">
            <el-tag :type="actionTagType(row.action)" size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标类型" width="110" />
        <el-table-column prop="targetId" label="目标ID" width="80" />
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="createdAt" label="时间" width="170" />
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadLogs"
        @current-change="loadLogs"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import { getAuditLogs, exportAuditExcel, exportAuditJson } from '@/api/audit'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const dateRange = ref(null)

const modules = ['PROJECT', 'CONTRACT', 'PURCHASE', 'MATERIAL', 'FINANCE', 'HR', 'SYSTEM', 'COMPLETION']
const actions = ['LOGIN', 'LOGOUT', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 'EXPORT']

const queryForm = reactive({
  username: '',
  module: '',
  action: '',
  targetType: '',
  startDate: '',
  endDate: '',
  page: 1,
  size: 20
})

const actionTagType = (action) => {
  const map = { LOGIN: 'success', LOGOUT: 'info', CREATE: 'primary', UPDATE: 'warning', DELETE: 'danger', APPROVE: 'success', REJECT: 'danger', EXPORT: '' }
  return map[action] || ''
}

const loadLogs = async () => {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryForm.startDate = dateRange.value[0]
      queryForm.endDate = dateRange.value[1]
    } else {
      queryForm.startDate = ''
      queryForm.endDate = ''
    }
    const res = await getAuditLogs(queryForm)
    logList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('加载审计日志失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.username = ''
  queryForm.module = ''
  queryForm.action = ''
  queryForm.targetType = ''
  queryForm.startDate = ''
  queryForm.endDate = ''
  queryForm.page = 1
  dateRange.value = null
  loadLogs()
}

const downloadBlob = (data, filename) => {
  const blob = new Blob([data])
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  window.URL.revokeObjectURL(url)
}

const handleExportExcel = async () => {
  try {
    const res = await exportAuditExcel(queryForm)
    downloadBlob(res.data, `audit_log_${new Date().toISOString().slice(0,10)}.csv`)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

const handleExportJson = async () => {
  try {
    const res = await exportAuditJson(queryForm)
    downloadBlob(res.data, `audit_log_${new Date().toISOString().slice(0,10)}.json`)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(() => loadLogs())
</script>

<style scoped lang="scss">
.audit-log-page {
  padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
