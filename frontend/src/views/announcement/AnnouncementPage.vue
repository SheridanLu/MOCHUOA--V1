<template>
  <div class="announcement-page">
    <el-tabs v-model="activeTab" @tab-click="loadTab">
      <!-- Announcements -->
      <el-tab-pane label="公告管理" name="announcement">
        <div class="tab-header">
          <el-form :inline="true">
            <el-form-item>
              <el-select v-model="annQuery.type" placeholder="类型" clearable style="width:120px">
                <el-option label="通知" :value="1" /><el-option label="公告" :value="2" /><el-option label="制度文件" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-select v-model="annQuery.status" placeholder="状态" clearable style="width:120px">
                <el-option label="草稿" :value="1" /><el-option label="已发布" :value="2" /><el-option label="已下线" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item><el-button type="primary" @click="loadAnnouncements">搜索</el-button></el-form-item>
          </el-form>
          <el-button type="primary" v-permission="'announcement:create'" @click="showAnnForm = true">发布公告</el-button>
        </div>
        <el-table :data="announcements" v-loading="loading" stripe>
          <el-table-column prop="title" label="标题" min-width="250" />
          <el-table-column prop="typeName" label="类型" width="100">
            <template #default="{ row }"><el-tag size="small">{{ ['','通知','公告','制度文件'][row.type] }}</el-tag></template>
          </el-table-column>
          <el-table-column label="置顶" width="80">
            <template #default="{ row }"><el-tag v-if="row.pinned" type="danger" size="small">置顶</el-tag></template>
          </el-table-column>
          <el-table-column prop="publishAt" label="发布时间" width="160" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag :type="row.status === 2 ? 'success' : row.status === 3 ? 'info' : 'warning'" size="small">{{ row.statusName || ['','草稿','已发布','已下线'][row.status] }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="creatorName" label="创建人" width="100" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="viewContent(row)">查看</el-button>
              <el-button link type="success" v-if="row.status === 1" v-permission="'announcement:manage'" @click="doPublish(row)">发布</el-button>
              <el-button link type="warning" v-if="row.status === 2" v-permission="'announcement:manage'" @click="doOffline(row)">下线</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="annTotal" :page-size="annQuery.size" v-model:current-page="annQuery.page" @current-change="loadAnnouncements" />
      </el-tab-pane>

      <!-- Notifications -->
      <el-tab-pane name="notification">
        <template #label>
          消息通知
          <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" style="margin-left:4px" />
        </template>
        <div class="tab-header">
          <span></span>
          <el-button type="primary" link @click="doMarkAllRead">全部已读</el-button>
        </div>
        <el-table :data="notifications" v-loading="loading" stripe>
          <el-table-column label="" width="40">
            <template #default="{ row }"><span v-if="!row.readFlag" style="color:#F56C6C;font-size:20px">·</span></template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="250" />
          <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
          <el-table-column prop="module" label="模块" width="100" />
          <el-table-column prop="createdAt" label="时间" width="160" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="primary" v-if="!row.readFlag" @click="doMarkRead(row)">标记已读</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" background layout="total, prev, pager, next" :total="notifTotal" :page-size="notifQuery.size" v-model:current-page="notifQuery.page" @current-change="loadNotifications" />
      </el-tab-pane>
    </el-tabs>

    <!-- Create Announcement Dialog -->
    <el-dialog v-model="showAnnForm" title="发布公告" width="600px">
      <el-form :model="annForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="annForm.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="annForm.type" style="width:100%">
            <el-option label="通知" :value="1" /><el-option label="公告" :value="2" /><el-option label="制度文件" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="annForm.content" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="置顶"><el-switch v-model="annForm.pinned" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="定时发布"><el-date-picker v-model="annForm.publishAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="留空立即发布" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAnnForm = false">取消</el-button>
        <el-button type="primary" @click="doCreateAnnouncement">确定</el-button>
      </template>
    </el-dialog>

    <!-- View Content Dialog -->
    <el-dialog v-model="showContentDialog" :title="viewingTitle" width="600px">
      <div style="white-space:pre-wrap;line-height:1.8">{{ viewingContent }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAnnouncements, createAnnouncement, publishAnnouncement, offlineAnnouncement, getMyNotifications, markNotificationRead, markAllRead, getUnreadCount } from '@/api/announcement'

const loading = ref(false)
const activeTab = ref('announcement')
const unreadCount = ref(0)

const announcements = ref([]), annTotal = ref(0)
const annQuery = reactive({ type: null, status: null, page: 1, size: 20 })
const showAnnForm = ref(false)
const annForm = reactive({ title: '', type: 1, content: '', pinned: 0, publishAt: null })

const notifications = ref([]), notifTotal = ref(0)
const notifQuery = reactive({ page: 1, size: 20 })

const showContentDialog = ref(false), viewingTitle = ref(''), viewingContent = ref('')

const loadTab = () => { if (activeTab.value === 'announcement') loadAnnouncements(); else loadNotifications() }

const loadAnnouncements = async () => { loading.value = true; try { const r = await getAnnouncements(annQuery); announcements.value = r.data.records; annTotal.value = r.data.total } finally { loading.value = false } }
const loadNotifications = async () => { loading.value = true; try { const r = await getMyNotifications(notifQuery); notifications.value = r.data.records; notifTotal.value = r.data.total } finally { loading.value = false } }
const loadUnreadCount = async () => { try { const r = await getUnreadCount(); unreadCount.value = r.data } catch(e) {} }

const doCreateAnnouncement = async () => { await createAnnouncement(annForm); ElMessage.success('公告已创建'); showAnnForm.value = false; loadAnnouncements() }
const doPublish = async (row) => { await publishAnnouncement(row.id); ElMessage.success('已发布'); loadAnnouncements() }
const doOffline = async (row) => { await offlineAnnouncement(row.id); ElMessage.success('已下线'); loadAnnouncements() }
const doMarkRead = async (row) => { await markNotificationRead(row.id); row.readFlag = 1; unreadCount.value = Math.max(0, unreadCount.value - 1) }
const doMarkAllRead = async () => { await markAllRead(); ElMessage.success('全部已读'); loadNotifications(); unreadCount.value = 0 }
const viewContent = (row) => { viewingTitle.value = row.title; viewingContent.value = row.content; showContentDialog.value = true }

onMounted(() => { loadAnnouncements(); loadUnreadCount() })
</script>

<style scoped>
.tab-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.mt-4 { margin-top: 16px; }
</style>
