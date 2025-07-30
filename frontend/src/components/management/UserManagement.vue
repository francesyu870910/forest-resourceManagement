<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button
        type="primary"
        icon="Plus"
        @click="showAddUser"
      >
        添加用户
      </el-button>
    </div>

    <!-- 用户统计信息 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number">{{ userStats.total }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number admin">{{ userStats.adminCount }}</div>
            <div class="stat-label">管理员</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number user">{{ userStats.userCount }}</div>
            <div class="stat-label">普通用户</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-number enabled">{{ userStats.enabledCount }}</div>
            <div class="stat-label">启用用户</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.role"
            placeholder="请选择角色"
            clearable
            @change="handleSearch"
          >
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.enabled"
            placeholder="请选择状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户列表 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="filteredUsers"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="180">
          <template #default="{ row }">
            {{ row.lastLoginAt ? formatDateTime(row.lastLoginAt) : '从未登录' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="showEditUser(row)"
            >
              编辑
            </el-button>
            <el-button
              :type="row.enabled ? 'warning' : 'success'"
              size="small"
              @click="toggleUserStatus(row)"
            >
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="showDeleteUser(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 用户表单对话框 -->
    <UserForm
      v-model:visible="userFormVisible"
      :user="currentUser"
      :is-edit="isEdit"
      @success="handleUserFormSuccess"
    />

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
      :close-on-click-modal="false"
    >
      <p>确定要删除用户 <strong>{{ userToDelete?.username }}</strong> 吗？</p>
      <p class="warning-text">此操作不可恢复！</p>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button
            type="danger"
            :loading="deleteLoading"
            @click="confirmDeleteUser"
          >
            {{ deleteLoading ? '删除中...' : '确认删除' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import UserForm from './UserForm.vue'
import { userApi } from '@/utils/userApi'
import { formatDateTime } from '@/utils/common'

// 响应式数据
const loading = ref(false)
const users = ref([])
const userFormVisible = ref(false)
const deleteDialogVisible = ref(false)
const deleteLoading = ref(false)
const currentUser = ref(null)
const userToDelete = ref(null)
const isEdit = ref(false)

// 搜索表单
const searchForm = reactive({
  username: '',
  role: '',
  enabled: null
})

// 计算属性
const filteredUsers = computed(() => {
  return users.value.filter(user => {
    const matchUsername = !searchForm.username || 
      user.username.toLowerCase().includes(searchForm.username.toLowerCase())
    const matchRole = !searchForm.role || user.role === searchForm.role
    const matchEnabled = searchForm.enabled === null || user.enabled === searchForm.enabled
    
    return matchUsername && matchRole && matchEnabled
  })
})

const adminCount = computed(() => {
  return users.value.filter(user => user.role === 'ADMIN').length
})

const userCount = computed(() => {
  return users.value.filter(user => user.role === 'USER').length
})

const enabledCount = computed(() => {
  return users.value.filter(user => user.enabled).length
})

// 用户统计信息
const userStats = computed(() => {
  return {
    total: users.value.length,
    adminCount: adminCount.value,
    userCount: userCount.value,
    enabledCount: enabledCount.value
  }
})

// 方法
const loadUsers = async () => {
  try {
    loading.value = true
    const result = await userApi.getAllUsers()
    
    if (result.success) {
      users.value = result.data || []
    } else {
      ElMessage.error(result.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 搜索逻辑已在计算属性中实现
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.role = ''
  searchForm.enabled = null
}

const showAddUser = () => {
  currentUser.value = null
  isEdit.value = false
  userFormVisible.value = true
}

const showEditUser = (user) => {
  currentUser.value = { ...user }
  isEdit.value = true
  userFormVisible.value = true
}

const showDeleteUser = (user) => {
  userToDelete.value = user
  deleteDialogVisible.value = true
}

const toggleUserStatus = async (user) => {
  try {
    const action = user.enabled ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}用户 ${user.username} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const result = await userApi.updateUser(user.id, {
      username: user.username,
      role: user.role,
      enabled: !user.enabled
    })

    if (result.success) {
      ElMessage.success(`${action}成功`)
      await loadUsers()
    } else {
      ElMessage.error(result.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换用户状态失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const confirmDeleteUser = async () => {
  try {
    deleteLoading.value = true
    
    const result = await userApi.deleteUser(userToDelete.value.id)
    
    if (result.success) {
      ElMessage.success('删除成功')
      deleteDialogVisible.value = false
      await loadUsers()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    console.error('删除用户失败:', error)
    ElMessage.error('删除失败')
  } finally {
    deleteLoading.value = false
  }
}

const handleUserFormSuccess = async () => {
  userFormVisible.value = false
  await loadUsers()
}

// 组件挂载时加载数据
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.search-bar {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.stats-container {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-number.admin {
  color: #f56c6c;
}

.stat-number.user {
  color: #67c23a;
}

.stat-number.enabled {
  color: #e6a23c;
}

.stat-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.table-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stats-bar {
  margin-top: 20px;
}

.stats-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stats-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 20px;
}

.stats-item {
  text-align: center;
}

.stats-label {
  font-size: 14px;
  opacity: 0.9;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  margin-left: 8px;
}

.warning-text {
  color: #f56c6c;
  font-size: 14px;
  margin: 10px 0;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-management {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .search-form {
    flex-direction: column;
  }
  
  .stats-card :deep(.el-card__body) {
    flex-direction: column;
    gap: 15px;
  }
  
  .stats-item {
    display: flex;
    justify-content: space-between;
    width: 100%;
  }
}
</style>