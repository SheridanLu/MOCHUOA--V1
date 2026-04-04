<template>
  <div class="material-page">
    <el-tabs v-model="activeTab" @tab-click="loadTab">
      <!-- Inbound Tab -->
      <el-tab-pane label="入库管理" name="inbound">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input v-model="inQuery.keyword" placeholder="搜索编号" clearable style="width:180px" /></el-form-item>
            <el-form-item>
              <el-select v-model="inQuery.status" placeholder="状态" clearable style="width:120px">
                <el-option label="草稿" :value="1" /><el-option label="待审批" :value="2" />
                <el-option label="财务已审" :value="3" /><el-option label="已入库" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary" @click="loadInbounds">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'material:create'" @click="handleCreateInbound">新建入库单</el-button>
        </div>
        <el-table :data="inbounds" v-loading="loading" stripe>
          <el-table-column prop="inboundNo" label="入库编号" width="160" />
          <el-table-column prop="projectName" label="项目" min-width="180" />
          <el-table-column prop="supplierName" label="供应商" width="150" />
          <el-table-column prop="totalAmount" label="金额" width="110" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 4 ? 'success' : row.status >= 2 ? 'warning' : 'info'" size="small">{{ row.statusName }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="inboundDate" label="日期" width="110" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleInboundDetail(row)">详情</el-button>
              <el-button link type="warning" v-if="row.status === 1" v-permission="'material:create'" @click="doSubmitInbound(row)">提交</el-button>
              <el-button link type="success" v-if="row.status === 2 || row.status === 3" v-permission="'material:approve'" @click="openApprove('inbound', row)">审批</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="inTotal" :page-size="inQuery.size" v-model:current-page="inQuery.page" @current-change="loadInbounds" />
      </el-tab-pane>

      <!-- Outbound Tab -->
      <el-tab-pane label="出库管理" name="outbound">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item><el-input v-model="outQuery.keyword" placeholder="搜索编号" clearable style="width:180px" /></el-form-item>
            <el-form-item>
              <el-select v-model="outQuery.status" placeholder="状态" clearable style="width:120px">
                <el-option label="草稿" :value="1" /><el-option label="待审批" :value="2" />
                <el-option label="已出库" :value="5" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary" @click="loadOutbounds">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'material:create'" @click="handleCreateOutbound">新建出库单</el-button>
        </div>
        <el-table :data="outbounds" v-loading="loading" stripe>
          <el-table-column prop="outboundNo" label="出库编号" width="160" />
          <el-table-column prop="projectName" label="项目" min-width="180" />
          <el-table-column prop="recipient" label="领用人" width="100" />
          <el-table-column prop="purpose" label="用途" width="150" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 5 ? 'success' : row.status >= 2 ? 'warning' : 'info'" size="small">{{ row.statusName }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleOutboundDetail(row)">详情</el-button>
              <el-button link type="warning" v-if="row.status === 1" v-permission="'material:create'" @click="doSubmitOutbound(row)">提交</el-button>
              <el-button link type="success" v-if="row.status >= 2 && row.status <= 4" v-permission="'material:approve'" @click="openApprove('outbound', row)">审批</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="outTotal" :page-size="outQuery.size" v-model:current-page="outQuery.page" @current-change="loadOutbounds" />
      </el-tab-pane>

      <!-- Return Tab -->
      <el-tab-pane label="退料管理" name="return">
        <div class="tab-header">
          <el-button type="primary" v-permission="'material:create'" @click="handleCreateReturn">新建退料</el-button>
        </div>
        <el-table :data="returns" v-loading="loading" stripe>
          <el-table-column prop="returnNo" label="退料编号" width="160" />
          <el-table-column prop="projectName" label="项目" min-width="180" />
          <el-table-column prop="returnTypeName" label="退料类型" width="120" />
          <el-table-column prop="targetProjectName" label="目标项目" width="150" />
          <el-table-column prop="reason" label="原因" width="200" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 3 ? 'success' : 'warning'" size="small">{{ row.statusName }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="success" v-if="row.status === 1" v-permission="'material:approve'" @click="openApprove('return', row)">审批</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Inventory Tab -->
      <el-tab-pane label="库存查询" name="inventory">
        <el-form :inline="true">
          <el-form-item><el-input-number v-model="invProjectId" placeholder="项目ID" :min="0" /></el-form-item>
          <el-form-item><el-input v-model="invKeyword" placeholder="物资名称/规格" clearable style="width:200px" /></el-form-item>
          <el-form-item><el-button type="primary" @click="loadInventory">查询</el-button></el-form-item>
        </el-form>
        <el-table :data="inventory" v-loading="loading" stripe>
          <el-table-column prop="projectName" label="项目" min-width="180" />
          <el-table-column prop="materialName" label="物资名称" width="160" />
          <el-table-column prop="spec" label="规格" width="120" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="quantity" label="库存数量" width="110" />
          <el-table-column prop="weightedAvgPrice" label="加权均价" width="110" />
          <el-table-column prop="totalAmount" label="总金额" width="110" />
          <el-table-column prop="warehouse" label="仓库" width="120" />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveDialog" title="审批" width="500px">
      <el-form label-width="80px">
        <el-form-item label="审批意见"><el-input v-model="approveComment" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApproveDialog = false">取消</el-button>
        <el-button type="danger" @click="doReject">驳回</el-button>
        <el-button type="success" @click="doApproveAction">通过</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="showDetailDialog" :title="detailTitle" width="700px">
      <el-table :data="detailItems" border size="small">
        <el-table-column prop="materialName" label="物资名称" /><el-table-column prop="spec" label="规格" />
        <el-table-column prop="unit" label="单位" width="80" /><el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="unitPrice" label="单价" width="100" /><el-table-column prop="amount" label="金额" width="100" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInbounds, getInboundById, createInbound, submitInbound, approveInbound, rejectInbound,
  getOutbounds, getOutboundById, createOutbound, submitOutbound, approveOutbound, rejectOutbound,
  getReturns, createReturn, approveReturn, getInventory } from '@/api/material'

const loading = ref(false)
const activeTab = ref('inbound')
const inbounds = ref([]), inTotal = ref(0), inQuery = reactive({ keyword: '', status: null, page: 1, size: 20 })
const outbounds = ref([]), outTotal = ref(0), outQuery = reactive({ keyword: '', status: null, page: 1, size: 20 })
const returns = ref([])
const inventory = ref([]), invProjectId = ref(null), invKeyword = ref('')
const showApproveDialog = ref(false), showDetailDialog = ref(false)
const approveComment = ref(''), approveType = ref(''), approvingId = ref(null)
const detailTitle = ref(''), detailItems = ref([])

const loadTab = () => {
  if (activeTab.value === 'inbound') loadInbounds()
  else if (activeTab.value === 'outbound') loadOutbounds()
  else if (activeTab.value === 'return') loadReturns()
  else loadInventory()
}

const loadInbounds = async () => { loading.value = true; try { const r = await getInbounds(inQuery); inbounds.value = r.data.records; inTotal.value = r.data.total } finally { loading.value = false } }
const loadOutbounds = async () => { loading.value = true; try { const r = await getOutbounds(outQuery); outbounds.value = r.data.records; outTotal.value = r.data.total } finally { loading.value = false } }
const loadReturns = async () => { loading.value = true; try { const r = await getReturns({}); returns.value = r.data.records } finally { loading.value = false } }
const loadInventory = async () => { loading.value = true; try { const r = await getInventory({ projectId: invProjectId.value, keyword: invKeyword.value }); inventory.value = r.data } finally { loading.value = false } }

const handleInboundDetail = async (row) => { const r = await getInboundById(row.id); detailTitle.value = `入库单 ${row.inboundNo}`; detailItems.value = r.data.items; showDetailDialog.value = true }
const handleOutboundDetail = async (row) => { const r = await getOutboundById(row.id); detailTitle.value = `出库单 ${row.outboundNo}`; detailItems.value = r.data.items; showDetailDialog.value = true }

const doSubmitInbound = (row) => { ElMessageBox.confirm('确定提交？').then(async () => { await submitInbound(row.id); ElMessage.success('已提交'); loadInbounds() }).catch(() => {}) }
const doSubmitOutbound = (row) => { ElMessageBox.confirm('确定提交？').then(async () => { await submitOutbound(row.id); ElMessage.success('已提交'); loadOutbounds() }).catch(() => {}) }

const openApprove = (type, row) => { approveType.value = type; approvingId.value = row.id; approveComment.value = ''; showApproveDialog.value = true }
const doApproveAction = async () => {
  const fn = approveType.value === 'inbound' ? approveInbound : approveType.value === 'outbound' ? approveOutbound : approveReturn
  await fn(approvingId.value, approveComment.value); ElMessage.success('审批通过'); showApproveDialog.value = false; loadTab()
}
const doReject = async () => {
  const fn = approveType.value === 'inbound' ? rejectInbound : rejectOutbound
  await fn(approvingId.value, approveComment.value); ElMessage.success('已驳回'); showApproveDialog.value = false; loadTab()
}

const handleCreateInbound = () => { ElMessage.info('请从合同审批通过后自动生成入库草稿，或通过API创建') }
const handleCreateOutbound = () => { ElMessage.info('出库创建功能需要从库存中选择物资') }
const handleCreateReturn = () => { ElMessage.info('退料创建功能需要从已入库物资中选择') }

onMounted(loadInbounds)
</script>

<style scoped>
.tab-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
</style>
