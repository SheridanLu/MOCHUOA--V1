<template>
  <div class="contact-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通讯录</span>
          <el-input v-model="searchKeyword" placeholder="搜索姓名/电话/职位(≥2字符)" clearable @input="handleSearch" style="width:280px" />
        </div>
      </template>
      <el-container>
        <el-aside width="240px" class="dept-aside">
          <el-tree
            :data="deptTree"
            :props="{ label: 'deptName', children: 'children' }"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handleDeptClick"
          >
            <template #default="{ data }">
              <span>{{ data.deptName }}</span>
            </template>
          </el-tree>
        </el-aside>
        <el-main>
          <el-table :data="contacts" v-loading="loading" stripe>
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="position" label="职位" width="150" />
            <el-table-column prop="deptName" label="部门" width="150" />
            <el-table-column prop="phone" label="手机号" width="140" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
          </el-table>
        </el-main>
      </el-container>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getContactList, searchContacts } from '@/api/contact'
import { getDeptTree } from '@/api/department'

const deptTree = ref([])
const contacts = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const selectedDeptId = ref(null)
let searchTimer = null

const loadDeptTree = async () => {
  const res = await getDeptTree()
  deptTree.value = res.data
}

const loadContacts = async (deptId, keyword) => {
  loading.value = true
  try {
    const params = {}
    if (deptId) params.deptId = deptId
    if (keyword) params.keyword = keyword
    const res = await getContactList(params)
    contacts.value = res.data
  } finally { loading.value = false }
}

const handleDeptClick = (data) => {
  selectedDeptId.value = data.id
  searchKeyword.value = ''
  loadContacts(data.id)
}

const handleSearch = (val) => {
  clearTimeout(searchTimer)
  if (!val || val.length < 2) {
    if (!val) loadContacts(selectedDeptId.value)
    return
  }
  searchTimer = setTimeout(async () => {
    loading.value = true
    try {
      const res = await searchContacts(val)
      contacts.value = res.data
    } finally { loading.value = false }
  }, 300)
}

onMounted(async () => {
  await loadDeptTree()
  loadContacts()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.dept-aside { border-right: 1px solid #e6e6e6; padding-right: 16px; }
</style>
