<template>
  <div class="showcase-page">
    <div class="tab-header">
      <el-form :inline="true">
        <el-form-item>
          <el-select v-model="query.status" placeholder="状态" clearable style="width:120px">
            <el-option label="草稿" :value="1" /><el-option label="待审批" :value="2" />
            <el-option label="已发布" :value="3" /><el-option label="已驳回" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.visibility" placeholder="可见性" clearable style="width:120px">
            <el-option label="公开" :value="1" /><el-option label="仅内部" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadShowcases">搜索</el-button></el-form-item>
      </el-form>
      <el-button type="primary" v-permission="'showcase:create'" @click="showForm = true">创建案例</el-button>
    </div>

    <!-- Card Grid -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in showcases" :key="item.id" style="margin-bottom:20px">
        <el-card :body-style="{ padding: '0' }" shadow="hover" @click="viewDetail(item)" style="cursor:pointer">
          <div class="card-cover" :style="{ backgroundImage: item.coverUrl ? `url(${item.coverUrl})` : 'none' }">
            <el-tag v-if="item.visibility === 2" type="warning" size="small" class="visibility-tag">仅内部</el-tag>
          </div>
          <div style="padding:14px">
            <h3 style="margin:0 0 8px;font-size:16px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ item.title }}</h3>
            <p style="margin:0;color:#999;font-size:13px;height:40px;overflow:hidden">{{ item.description }}</p>
            <div style="display:flex;justify-content:space-between;align-items:center;margin-top:8px">
              <el-tag :type="item.status === 3 ? 'success' : item.status === 4 ? 'danger' : item.status === 2 ? 'warning' : 'info'" size="small">{{ item.statusName || ['','草稿','待审批','已发布','已驳回'][item.status] }}</el-tag>
              <span style="color:#ccc;font-size:12px">{{ item.viewCount || 0 }} 次浏览</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="showcases.length === 0" description="暂无案例" />
    <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="loadShowcases" />

    <!-- Detail Dialog -->
    <el-dialog v-model="showDetailDialog" :title="detail?.title" width="700px">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="项目">{{ detail.projectName }}</el-descriptions-item>
          <el-descriptions-item label="可见性">{{ detail.visibilityName }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="detail.status === 3 ? 'success' : 'warning'" size="small">{{ detail.statusName }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="浏览次数">{{ detail.viewCount || 0 }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin:16px 0;line-height:1.8;white-space:pre-wrap">{{ detail.description }}</div>
        <el-row :gutter="12" v-if="detail.images && detail.images.length">
          <el-col :span="8" v-for="img in detail.images" :key="img.id" style="margin-bottom:12px">
            <el-image :src="img.imageUrl" fit="cover" style="width:100%;height:150px;border-radius:4px" :preview-src-list="detail.images.map(i => i.imageUrl)" />
            <div style="text-align:center;font-size:12px;color:#999;margin-top:4px">{{ img.caption }}</div>
          </el-col>
        </el-row>
        <div style="margin-top:16px;text-align:right">
          <el-button v-if="detail.status === 1" v-permission="'showcase:create'" type="warning" @click="doSubmit(detail)">提交审批</el-button>
          <el-button v-if="detail.status === 2" v-permission="'showcase:approve'" type="success" @click="openApprove(detail, false)">通过</el-button>
          <el-button v-if="detail.status === 2" v-permission="'showcase:approve'" type="danger" @click="openApprove(detail, true)">驳回</el-button>
          <el-button v-if="detail.status === 3" v-permission="'showcase:approve'" @click="doToggleVisibility(detail)">{{ detail.visibility === 1 ? '设为内部' : '设为公开' }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Create Form Dialog -->
    <el-dialog v-model="showForm" title="创建案例" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="项目ID"><el-input-number v-model="form.projectId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="form.coverUrl" /></el-form-item>
        <el-form-item label="视频URL"><el-input v-model="form.videoUrl" /></el-form-item>
        <el-form-item label="全景URL"><el-input v-model="form.panoramaUrl" /></el-form-item>
        <el-form-item label="可见性">
          <el-select v-model="form.visibility" style="width:100%">
            <el-option label="公开" :value="1" /><el-option label="仅内部" :value="2" />
          </el-select>
        </el-form-item>
        <el-divider>图片</el-divider>
        <el-form-item v-for="(img, idx) in form.images" :key="idx" :label="'图片'+(idx+1)">
          <div style="display:flex;gap:8px;width:100%">
            <el-input v-model="img.imageUrl" placeholder="图片URL" style="flex:2" />
            <el-input v-model="img.caption" placeholder="描述" style="flex:1" />
            <el-button type="danger" link @click="form.images.splice(idx, 1)">删除</el-button>
          </div>
        </el-form-item>
        <el-form-item><el-button type="primary" link @click="form.images.push({ imageUrl:'', caption:'', sortOrder: form.images.length })">+ 添加图片</el-button></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" @click="doCreate">创建</el-button>
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
import { getShowcases, getShowcaseDetail, createShowcase, submitShowcase, approveShowcase, rejectShowcase, setShowcaseVisibility } from '@/api/showcase'

const loading = ref(false)
const showcases = ref([]), total = ref(0)
const query = reactive({ status: null, visibility: null, page: 1, size: 12 })
const showForm = ref(false)
const form = reactive({ projectId: null, title: '', description: '', coverUrl: '', videoUrl: '', panoramaUrl: '', visibility: 1, images: [] })

const detail = ref(null), showDetailDialog = ref(false)
const showApproveDialog = ref(false), approveComment = ref(''), isReject = ref(false), approvingId = ref(null)

const loadShowcases = async () => { loading.value = true; try { const r = await getShowcases(query); showcases.value = r.data.records; total.value = r.data.total } finally { loading.value = false } }

const viewDetail = async (item) => { const r = await getShowcaseDetail(item.id); detail.value = r.data; showDetailDialog.value = true }
const doCreate = async () => { await createShowcase(form); ElMessage.success('案例已创建'); showForm.value = false; loadShowcases() }
const doSubmit = async (item) => { await submitShowcase(item.id); ElMessage.success('已提交审批'); showDetailDialog.value = false; loadShowcases() }
const doToggleVisibility = async (item) => { const newVis = item.visibility === 1 ? 2 : 1; await setShowcaseVisibility(item.id, newVis); ElMessage.success('已更新'); showDetailDialog.value = false; loadShowcases() }

const openApprove = (item, reject) => { approvingId.value = item.id; isReject.value = reject; approveComment.value = ''; showApproveDialog.value = true }
const doApproveAction = async () => {
  const fn = isReject.value ? rejectShowcase : approveShowcase
  await fn(approvingId.value, approveComment.value)
  ElMessage.success(isReject.value ? '已驳回' : '审批通过'); showApproveDialog.value = false; showDetailDialog.value = false; loadShowcases()
}

onMounted(loadShowcases)
</script>

<style scoped>
.tab-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
.card-cover { height: 160px; background-color: #f0f2f5; background-size: cover; background-position: center; position: relative; }
.visibility-tag { position: absolute; top: 8px; right: 8px; }
</style>
