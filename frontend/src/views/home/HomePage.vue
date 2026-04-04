<template>
  <div class="home-page">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="hover" class="home-card">
          <template #header>常用功能</template>
          <div class="shortcut-grid">
            <div class="shortcut-item" v-for="item in shortcuts" :key="item.code">
              <el-icon :size="24"><component :is="item.icon" /></el-icon>
              <span>{{ item.name }}</span>
            </div>
          </div>
        </el-card>
        <el-card shadow="hover" class="home-card" style="margin-top: 20px">
          <template #header>系统公告</template>
          <el-empty v-if="!announcements.length" description="暂无公告" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="home-card">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>待办事项</span>
              <el-badge :value="todoCount" :max="99">
                <el-button link type="primary" size="small">查看全部</el-button>
              </el-badge>
            </div>
          </template>
          <el-empty v-if="!todoList.length" description="暂无待办" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const shortcuts = ref([])
const todoList = ref([])
const todoCount = ref(0)
const announcements = ref([])

onMounted(() => {
  // TODO: 调用 GET /api/v1/home 获取首页聚合数据
})
</script>

<style scoped lang="scss">
.home-page {
  padding: 20px;
}
.home-card {
  border-radius: 8px;
}
.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  &:hover { background: #f5f7fa; }
  span { font-size: 13px; color: #606266; }
}
</style>
