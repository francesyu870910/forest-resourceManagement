<template>
  <div class="forest-rights">
    <div class="page-header">
      <h2>林权证书管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          新增证书
        </el-button>
        <el-button @click="loadExpiringRights">
          <el-icon><Warning /></el-icon>
          到期提醒
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="证书编号">
          <el-input
            v-model="searchForm.certificateNo"
            placeholder="请输入证书编号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="权利人">
          <el-input
            v-model="searchForm.ownerName"
            placeholder="请输入权利人姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="证书状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="有效" value="有效" />
            <el-option label="即将到期" value="即将到期" />
            <el-option label="过期" value="过期" />
            <el-option label="注销" value="注销" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <DataTable
        :data="tableData"
        :columns="tableColumns"
        :loading="loading"
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        :show-selection="true"
        :action-width="200"
        @selection-change="handleSelectionChange"
        @edit="handleEdit"
        @delete="handleDelete"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      >
        <template #status="{ row }">
          <el-tag
            :type="getStatusType(row.status)"
            size="small"
          >
            {{ row.status }}
          </el-tag>
        </template>
        
        <template #issueDate="{ row }">
          {{ formatDate(row.issueDate) }}
        </template>
        
        <template #expiryDate="{ row }">
          <span :class="{ 'expiring-date': isExpiringSoon(row.expiryDate) }">
            {{ formatDate(row.expiryDate) }}
          </span>
          <el-icon v-if="isExpiringSoon(row.expiryDate)" class="warning-icon">
            <Warning />
          </el-icon>
        </template>
        
        <template #actions="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-dropdown @command="(command) => handleStatusChange(row, command)">
            <el-button size="small">
              状态 <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="有效">设为有效</el-dropdown-item>
                <el-dropdown-item command="注销">注销证书</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="danger" size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </DataTable>
      
      <!-- 批量操作 -->
      <div v-if="selectedRows.length > 0" class="batch-actions">
        <el-alert
          :title="`已选择 ${selectedRows.length} 条记录`"
          type="info"
          show-icon
          :closable="false"
        />
        <el-button type="danger" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <FormDialog
      v-model:visible="dialogVisible"
      :title="dialogTitle"
      :form-data="formData"
      :rules="formRules"
      :loading="submitLoading"
      width="800px"
      @confirm="handleSubmit"
      @close="handleDialogClose"
    >
      <template #form="{ formData }">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="证书编号" prop="certificateNo">
              <el-input
                v-model="formData.certificateNo"
                placeholder="请输入证书编号"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权利人姓名" prop="ownerName">
              <el-input
                v-model="formData.ownerName"
                placeholder="请输入权利人姓名"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="ownerIdCard">
              <el-input
                v-model="formData.ownerIdCard"
                placeholder="请输入身份证号"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="ownerPhone">
              <el-input
                v-model="formData.ownerPhone"
                placeholder="请输入联系电话"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="关联林地" prop="forestLandId">
          <el-select
            v-model="formData.forestLandId"
            placeholder="请选择关联林地"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="land in forestLandOptions"
              :key="land.id"
              :label="`${land.name} (${land.classification}, ${land.area}公顷)`"
              :value="land.id"
            />
          </el-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发证日期" prop="issueDate">
              <el-date-picker
                v-model="formData.issueDate"
                type="date"
                placeholder="请选择发证日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到期日期" prop="expiryDate">
              <el-date-picker
                v-model="formData.expiryDate"
                type="date"
                placeholder="请选择到期日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="发证机关" prop="issueOrgan">
          <el-input
            v-model="formData.issueOrgan"
            placeholder="请输入发证机关"
          />
        </el-form-item>
        
        <el-form-item label="备注信息" prop="remarks">
          <el-input
            v-model="formData.remarks"
            placeholder="请输入备注信息"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </template>
    </FormDialog>

    <!-- 到期提醒对话框 -->
    <el-dialog
      v-model="expiringDialogVisible"
      title="证书到期提醒"
      width="800px"
    >
      <div v-if="expiringRights.length > 0">
        <el-alert
          :title="`共有 ${expiringRights.length} 个证书即将到期或已过期`"
          type="warning"
          show-icon
          :closable="false"
          style="margin-bottom: 20px"
        />
        <el-table :data="expiringRights" stripe>
          <el-table-column prop="certificateNo" label="证书编号" width="150" />
          <el-table-column prop="ownerName" label="权利人" width="120" />
          <el-table-column prop="forestLandName" label="林地名称" min-width="150" />
          <el-table-column prop="expiryDate" label="到期日期" width="120">
            <template #default="{ row }">
              <span :class="{ 'expired-date': isExpired(row.expiryDate) }">
                {{ formatDate(row.expiryDate) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else>
        <el-empty description="暂无即将到期的证书" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Refresh, Warning, ArrowDown 
} from '@element-plus/icons-vue'
import DataTable from '../common/DataTable.vue'
import FormDialog from '../common/FormDialog.vue'
import { forestRightsApi } from '../../utils/api'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])
const forestLandOptions = ref([])
const expiringRights = ref([])
const expiringDialogVisible = ref(false)

// 搜索表单
const searchForm = reactive({
  certificateNo: '',
  ownerName: '',
  status: ''
})

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentEditId = ref(null)

const dialogTitle = computed(() => {
  return isEdit.value ? '编辑林权证书' : '新增林权证书'
})

// 表单数据
const formData = reactive({
  certificateNo: '',
  ownerName: '',
  ownerIdCard: '',
  ownerPhone: '',
  forestLandId: null,
  issueDate: null,
  expiryDate: null,
  issueOrgan: '',
  remarks: ''
})

// 表单验证规则
const formRules = {
  certificateNo: [
    { required: true, message: '请输入证书编号', trigger: 'blur' },
    { min: 6, max: 20, message: '证书编号长度在6-20个字符', trigger: 'blur' }
  ],
  ownerName: [
    { required: true, message: '请输入权利人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur' }
  ],
  ownerIdCard: [
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  ownerPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  forestLandId: [
    { required: true, message: '请选择关联林地', trigger: 'change' }
  ],
  issueDate: [
    { required: true, message: '请选择发证日期', trigger: 'change' }
  ],
  expiryDate: [
    { required: true, message: '请选择到期日期', trigger: 'change' }
  ]
}

// 格式化日期函数
const formatDate = (date) => {
  if (!date) return ''
  // 处理后端返回的日期格式 (YYYY-MM-DD)
  if (typeof date === 'string') {
    return date
  }
  return new Date(date).toLocaleDateString('zh-CN')
}

// 表格列配置
const tableColumns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'certificateNo', label: '证书编号', width: 150 },
  { prop: 'ownerName', label: '权利人', width: 120 },
  { prop: 'forestLandName', label: '林地名称', minWidth: 150 },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'issueDate', label: '发证日期', width: 120, slot: 'issueDate' },
  { prop: 'expiryDate', label: '到期日期', width: 140, slot: 'expiryDate' },
  { prop: 'issueOrgan', label: '发证机关', width: 120 }
]

// 生命周期
onMounted(() => {
  loadData()
  loadForestLandOptions()
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    
    const response = await forestRightsApi.getList(params)
    tableData.value = response.data
    total.value = response.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadForestLandOptions = async () => {
  try {
    const response = await forestRightsApi.getForestLands()
    forestLandOptions.value = response
  } catch (error) {
    ElMessage.error('加载林地选项失败')
  }
}

const loadExpiringRights = async () => {
  try {
    const response = await forestRightsApi.getExpiring()
    expiringRights.value = response
    expiringDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载到期提醒失败')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  loadData()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const showAddDialog = () => {
  isEdit.value = false
  currentEditId.value = null
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentEditId.value = row.id
  Object.keys(formData).forEach(key => {
    if (key === 'issueDate' || key === 'expiryDate') {
      formData[key] = row[key] ? new Date(row[key]) : null
    } else {
      formData[key] = row[key] || ''
    }
  })
  dialogVisible.value = true
}

const handleStatusChange = async (row, status) => {
  try {
    await forestRightsApi.updateStatus(row.id, status)
    ElMessage.success('状态更新成功')
    loadData()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除证书"${row.certificateNo}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await forestRightsApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条证书吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    await forestRightsApi.delete(`batch`, { data: ids })
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleSubmit = async (data) => {
  submitLoading.value = true
  try {
    // 格式化日期
    const submitData = { ...data }
    if (submitData.issueDate) {
      submitData.issueDate = formatDateForSubmit(submitData.issueDate)
    }
    if (submitData.expiryDate) {
      submitData.expiryDate = formatDateForSubmit(submitData.expiryDate)
    }
    
    if (isEdit.value) {
      await forestRightsApi.update(currentEditId.value, submitData)
      ElMessage.success('更新成功')
    } else {
      await forestRightsApi.create(submitData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  resetFormData()
}

const resetFormData = () => {
  Object.keys(formData).forEach(key => {
    if (key === 'forestLandId') {
      formData[key] = null
    } else if (key === 'issueDate' || key === 'expiryDate') {
      formData[key] = null
    } else {
      formData[key] = ''
    }
  })
}

const getStatusType = (status) => {
  const typeMap = {
    '有效': 'success',
    '即将到期': 'warning',
    '过期': 'danger',
    '注销': 'info'
  }
  return typeMap[status] || 'info'
}

const isExpiringSoon = (expiryDate) => {
  if (!expiryDate) return false
  const expiry = new Date(expiryDate)
  const now = new Date()
  const thirtyDaysLater = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000)
  return expiry <= thirtyDaysLater && expiry >= now
}

const isExpired = (expiryDate) => {
  if (!expiryDate) return false
  return new Date(expiryDate) < new Date()
}

const formatDateForSubmit = (date) => {
  if (!date) return null
  const d = new Date(date)
  return d.getFullYear() + '-' + 
         String(d.getMonth() + 1).padStart(2, '0') + '-' + 
         String(d.getDate()).padStart(2, '0')
}
</script>

<style scoped>
.forest-rights {
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
  color: #303133;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.batch-actions {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.batch-actions .el-alert {
  flex: 1;
}

.expiring-date {
  color: #e6a23c;
  font-weight: bold;
}

.expired-date {
  color: #f56c6c;
  font-weight: bold;
}

.warning-icon {
  color: #e6a23c;
  margin-left: 5px;
}

/* 操作列按钮样式优化 */
:deep(.el-table__cell) {
  .el-button {
    margin-right: 5px;
    margin-bottom: 2px;
  }
  
  .el-dropdown {
    margin-right: 5px;
  }
}

/* 确保操作列按钮不换行 */
:deep(.el-table__cell:last-child) {
  white-space: nowrap;
}
</style>