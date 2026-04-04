<template>
  <div class="template-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>合同模板管理</span>
          <el-button type="primary" v-permission="'template:create'" @click="handleCreate">新建模板</el-button>
        </div>
      </template>
      <el-form :inline="true" class="search-form">
        <el-form-item>
          <el-select v-model="filterType" placeholder="模板类型" clearable @change="loadList" style="width:140px">
            <el-option label="收入合同" :value="1" /><el-option label="支出合同" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="filterStatus" placeholder="状态" clearable @change="loadList" style="width:100px">
            <el-option label="草稿" :value="1" /><el-option label="待审批" :value="2" /><el-option label="已审批" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="templateName" label="模板名称" min-width="200" />
        <el-table-column prop="templateTypeName" label="类型" width="120" />
        <el-table-column prop="version" label="版本" width="70" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 3 ? 'success' : row.status === 2 ? 'warning' : 'info'" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handlePreview(row)">预览</el-button>
            <el-button link type="primary" v-permission="'template:edit'" v-if="row.status === 1" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" v-permission="'template:create'" v-if="row.status === 1" @click="handleSubmit(row)">提交审批</el-button>
            <el-button link type="success" v-permission="'template:approve'" v-if="row.status === 2" @click="handleApprove(row)">审批</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="showFormDialog" :title="editingId ? '编辑模板' : '新建模板'" width="700px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="模板名称" prop="templateName"><el-input v-model="form.templateName" /></el-form-item>
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="form.templateType" style="width:100%">
            <el-option label="收入合同模板" :value="1" /><el-option label="支出合同模板" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板内容">
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="使用 ${变量名} 作为占位符，如 ${甲方名称}、${合同金额}" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFormDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleFormSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Preview Dialog -->
    <el-dialog v-model="showPreviewDialog" title="模板预览" width="700px">
      <div v-if="previewContent" v-html="previewContent" class="template-preview" />
      <div v-else class="template-preview-empty">模板内容为空</div>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveDialog" title="模板审批" width="500px">
      <el-form label-width="80px">
        <el-form-item label="审批意见"><el-input v-model="approveComment" type="textarea" :rows="3" /></el-form-item>
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
import { getTemplateList, createTemplate, updateTemplate, submitTemplate, approveTemplate, rejectTemplate, renderTemplate } from '@/api/template'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const formRef = ref(null)
const filterType = ref(null)
const filterStatus = ref(null)

const showFormDialog = ref(false)
const showPreviewDialog = ref(false)
const showApproveDialog = ref(false)
const editingId = ref(null)
const previewContent = ref('')
const approveComment = ref('')
const approvingId = ref(null)

const form = reactive({ templateName: '', templateType: 1, content: '' })
const formRules = { templateName: [{ required: true, message: '请输入模板名称' }], templateType: [{ required: true, message: '请选择类型' }] }

const loadList = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterType.value) params.templateType = filterType.value
    if (filterStatus.value) params.status = filterStatus.value
    const res = await getTemplateList(params)
    list.value = res.data
  } finally { loading.value = false }
}

const handleCreate = () => {
  editingId.value = null
  Object.assign(form, { templateName: '', templateType: 1, content: '' })
  showFormDialog.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, { templateName: row.templateName, templateType: row.templateType, content: row.content || '' })
  showFormDialog.value = true
}

const handleFormSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (editingId.value) { await updateTemplate(editingId.value, form); ElMessage.success('更新成功') }
    else { await createTemplate(form); ElMessage.success('创建成功') }
    showFormDialog.value = false; loadList()
  } finally { submitting.value = false }
}

const handlePreview = async (row) => {
  const res = await renderTemplate(row.id)
  previewContent.value = res.data || row.content || ''
  showPreviewDialog.value = true
}

const handleSubmit = (row) => {
  ElMessageBox.confirm(`确定提交模板 ${row.templateName} 审批吗？`).then(async () => { await submitTemplate(row.id); ElMessage.success('已提交审批'); loadList() }).catch(() => {})
}

const handleApprove = (row) => { approvingId.value = row.id; approveComment.value = ''; showApproveDialog.value = true }
const doApprove = async () => { await approveTemplate(approvingId.value, approveComment.value); ElMessage.success('审批通过'); showApproveDialog.value = false; loadList() }
const doReject = async () => { await rejectTemplate(approvingId.value, approveComment.value); ElMessage.success('已驳回'); showApproveDialog.value = false; loadList() }

onMounted(loadList)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.template-preview { padding: 16px; border: 1px solid #e6e6e6; border-radius: 4px; min-height: 200px; line-height: 1.8; white-space: pre-wrap; }
.template-preview-empty { padding: 40px; text-align: center; color: #999; }
</style>
