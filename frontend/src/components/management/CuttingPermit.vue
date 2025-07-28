<template>
  <div class="cutting-permit">
    <div class="page-header">
      <h2>采伐许可管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          新增许可申请
        </el-button>
        <el-button @click="loadPendingPermits">
          <el-icon><Clock /></el-icon>
          待审批 ({{ pendingCount }})
        </el-button>
        <el-button @click="showStatistics">
          <el-icon><DataAnalysis /></el-icon>
          统计分析
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="许可证编号">
          <el-input
            v-model="searchForm.permitNo"
            placeholder="请输入许可证编号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="申请人">
          <el-input
            v-model="searchForm.applicantName"
            placeholder="请输入申请人姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待审批" value="待审批" />
            <el-option label="已批准" value="已批准" />
            <el-option label="已拒绝" value="已拒绝" />
            <el-option label="已过期" value="已过期" />
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
        :action-width="280"
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
        
        <template #validUntil="{ row }">
          <span v-if="row.validUntil" :class="{ 'expiring-date': isExpiringSoon(row.validUntil) }">
            {{ formatDate(row.validUntil) }}
          </span>
          <span v-else>-</span>
        </template>
        
        <template #actions="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button 
            v-if="row.status === '待审批'"
            type="success" 
            size="small" 
            @click="handleApprove(row)"
          >
            审批
          </el-button>
          <el-button type="info" size="small" @click="handleViewDetail(row)">
            详情
          </el-button>
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
            <el-form-item label="许可证编号" prop="permitNo">
              <el-input
                v-model="formData.permitNo"
                placeholder="请输入许可证编号"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请人姓名" prop="applicantName">
              <el-input
                v-model="formData.applicantName"
                placeholder="请输入申请人姓名"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="applicantIdCard">
              <el-input
                v-model="formData.applicantIdCard"
                placeholder="请输入身份证号"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="applicantPhone">
              <el-input
                v-model="formData.applicantPhone"
                placeholder="请输入联系电话"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="采伐地点" prop="forestLandId">
          <el-select
            v-model="formData.forestLandId"
            placeholder="请选择采伐地点"
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
            <el-form-item label="采伐面积(公顷)" prop="cuttingArea">
              <el-input-number
                v-model="formData.cuttingArea"
                :min="0.1"
                :max="10000"
                :precision="2"
                placeholder="请输入采伐面积"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采伐量(立方米)" prop="cuttingVolume">
              <el-input-number
                v-model="formData.cuttingVolume"
                :min="1"
                :max="10000"
                :precision="2"
                placeholder="请输入采伐量"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="申请理由" prop="reason">
          <el-input
            v-model="formData.reason"
            placeholder="请输入申请理由"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
      </template>
    </FormDialog>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      title="采伐许可审批"
      width="600px"
    >
      <div v-if="currentApprovalPermit">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="许可证编号">
            {{ currentApprovalPermit.permitNo }}
          </el-descriptions-item>
          <el-descriptions-item label="申请人">
            {{ currentApprovalPermit.applicantName }}
          </el-descriptions-item>
          <el-descriptions-item label="采伐地点">
            {{ currentApprovalPermit.forestLandName }}
          </el-descriptions-item>
          <el-descriptions-item label="申请日期">
            {{ formatDate(currentApprovalPermit.applicationDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="采伐面积">
            {{ currentApprovalPermit.cuttingArea }} 公顷
          </el-descriptions-item>
          <el-descriptions-item label="采伐量">
            {{ currentApprovalPermit.cuttingVolume }} 立方米
          </el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">
            {{ currentApprovalPermit.reason }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-form :model="approvalForm" :rules="approvalRules" ref="approvalFormRef" style="margin-top: 20px">
          <el-form-item label="审批人" prop="approver">
            <el-input
              v-model="approvalForm.approver"
              placeholder="请输入审批人姓名"
            />
          </el-form-item>
          
          <el-form-item label="审批意见" prop="opinion">
            <el-input
              v-model="approvalForm.opinion"
              placeholder="请输入审批意见"
              type="textarea"
              :rows="3"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approvalDialogVisible = false">取消</el-button>
          <el-button type="success" @click="handleApprovalSubmit('approve')" :loading="approvalLoading">
            批准
          </el-button>
          <el-button type="danger" @click="handleApprovalSubmit('reject')" :loading="approvalLoading">
            拒绝
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="采伐许可详情"
      width="700px"
    >
      <div v-if="currentDetailPermit">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="许可证编号">
            {{ currentDetailPermit.permitNo }}
          </el-descriptions-item>
          <el-descriptions-item label="申请人">
            {{ currentDetailPermit.applicantName }}
          </el-descriptions-item>
          <el-descriptions-item label="身份证号">
            {{ currentDetailPermit.applicantIdCard || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentDetailPermit.applicantPhone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="采伐地点">
            {{ currentDetailPermit.forestLandName }}
          </el-descriptions-item>
          <el-descriptions-item label="申请日期">
            {{ formatDate(currentDetailPermit.applicationDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="采伐面积">
            {{ currentDetailPermit.cuttingArea }} 公顷
          </el-descriptions-item>
          <el-descriptions-item label="采伐量">
            {{ currentDetailPermit.cuttingVolume }} 立方米
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDetailPermit.status)">
              {{ currentDetailPermit.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审批人">
            {{ currentDetailPermit.approver || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审批日期">
            {{ currentDetailPermit.approvalDate ? formatDate(currentDetailPermit.approvalDate) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="有效期至">
            {{ currentDetailPermit.validUntil ? formatDate(currentDetailPermit.validUntil) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">
            {{ currentDetailPermit.reason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetailPermit.approvalOpinion" label="审批意见" :span="2">
            {{ currentDetailPermit.approvalOpinion }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 统计分析对话框 -->
    <el-dialog
      v-model="statisticsDialogVisible"
      title="采伐统计分析"
      width="900px"
    >
      <div v-if="statistics">
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.totalCount }}</div>
              <div class="stat-label">总申请数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.totalCuttingArea }}</div>
              <div class="stat-label">总采伐面积(公顷)</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.totalCuttingVolume }}</div>
              <div class="stat-label">总采伐量(立方米)</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.expiringSoonCount }}</div>
              <div class="stat-label">即将到期</div>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="12">
            <h4>状态分布</h4>
            <el-table :data="statusStatsData" size="small">
              <el-table-column prop="status" label="状态" />
              <el-table-column prop="count" label="数量" />
              <el-table-column prop="percentage" label="占比" />
            </el-table>
          </el-col>
          <el-col :span="12">
            <h4>月度统计</h4>
            <div v-if="statistics.monthlyStats" style="height: 200px; overflow-y: auto;">
              <el-table :data="statistics.monthlyStats" size="small">
                <el-table-column prop="month" label="月份" width="80" />
                <el-table-column prop="count" label="申请数" width="80" />
                <el-table-column prop="area" label="面积" width="80" />
                <el-table-column prop="volume" label="采伐量" />
              </el-table>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Refresh, Clock, DataAnalysis 
} from '@element-plus/icons-vue'
import DataTable from '../common/DataTable.vue'
import FormDialog from '../common/FormDialog.vue'
import { cuttingPermitApi } from '../../utils/api'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const approvalLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])
const forestLandOptions = ref([])
const pendingCount = ref(0)
const statistics = ref(null)

// 对话框状态
const dialogVisible = ref(false)
const approvalDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const statisticsDialogVisible = ref(false)

// 当前操作的数据
const currentApprovalPermit = ref(null)
const currentDetailPermit = ref(null)
const isEdit = ref(false)
const currentEditId = ref(null)

// 搜索表单
const searchForm = reactive({
  permitNo: '',
  applicantName: '',
  status: ''
})

const dialogTitle = computed(() => {
  return isEdit.value ? '编辑采伐许可' : '新增采伐许可申请'
})

// 表单数据
const formData = reactive({
  permitNo: '',
  applicantName: '',
  applicantIdCard: '',
  applicantPhone: '',
  forestLandId: null,
  cuttingArea: null,
  cuttingVolume: null,
  reason: ''
})

// 审批表单
const approvalForm = reactive({
  approver: '',
  opinion: ''
})

const approvalFormRef = ref()

// 表单验证规则
const formRules = {
  permitNo: [
    { required: true, message: '请输入许可证编号', trigger: 'blur' },
    { min: 6, max: 20, message: '许可证编号长度在6-20个字符', trigger: 'blur' }
  ],
  applicantName: [
    { required: true, message: '请输入申请人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur' }
  ],
  applicantIdCard: [
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  applicantPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  forestLandId: [
    { required: true, message: '请选择采伐地点', trigger: 'change' }
  ],
  cuttingArea: [
    { required: true, message: '请输入采伐面积', trigger: 'blur' },
    { type: 'number', min: 0.1, max: 10000, message: '采伐面积必须在0.1-10000公顷之间', trigger: 'blur' }
  ],
  cuttingVolume: [
    { required: true, message: '请输入采伐量', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '采伐量必须在1-10000立方米之间', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入申请理由', trigger: 'blur' },
    { min: 5, max: 500, message: '申请理由长度在5-500个字符', trigger: 'blur' }
  ]
}

const approvalRules = {
  approver: [
    { required: true, message: '请输入审批人姓名', trigger: 'blur' }
  ],
  opinion: [
    { required: true, message: '请输入审批意见', trigger: 'blur' }
  ]
}

// 格式化日期函数
const formatDate = (row, column, cellValue) => {
  if (!cellValue) return ''
  return new Date(cellValue).toLocaleDateString('zh-CN')
}

// 表格列配置
const tableColumns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'permitNo', label: '许可证编号', width: 150 },
  { prop: 'applicantName', label: '申请人', width: 120 },
  { prop: 'forestLandName', label: '采伐地点', minWidth: 150 },
  { prop: 'cuttingArea', label: '采伐面积(公顷)', width: 120 },
  { prop: 'cuttingVolume', label: '采伐量(立方米)', width: 120 },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'applicationDate', label: '申请日期', width: 120, formatter: formatDate },
  { prop: 'validUntil', label: '有效期至', width: 120, slot: 'validUntil' }
]

// 统计数据计算
const statusStatsData = computed(() => {
  if (!statistics.value?.statusStats) return []
  
  const total = Object.values(statistics.value.statusStats).reduce((sum, count) => sum + count, 0)
  
  return Object.entries(statistics.value.statusStats).map(([status, count]) => ({
    status,
    count,
    percentage: total > 0 ? ((count / total) * 100).toFixed(1) + '%' : '0%'
  }))
})

// 生命周期
onMounted(() => {
  loadData()
  loadForestLandOptions()
  loadPendingCount()
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    // 只添加非空的搜索参数
    if (searchForm.permitNo && searchForm.permitNo.trim()) {
      params.permitNo = searchForm.permitNo.trim()
    }
    if (searchForm.applicantName && searchForm.applicantName.trim()) {
      params.applicantName = searchForm.applicantName.trim()
    }
    if (searchForm.status && searchForm.status.trim()) {
      params.status = searchForm.status.trim()
    }
    
    const response = await cuttingPermitApi.getList(params)
    tableData.value = response.data
    total.value = response.total
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadForestLandOptions = async () => {
  try {
    const response = await cuttingPermitApi.getForestLands()
    forestLandOptions.value = response
  } catch (error) {
    ElMessage.error('加载林地选项失败')
  }
}

const loadPendingCount = async () => {
  try {
    const response = await cuttingPermitApi.getPending()
    pendingCount.value = response.length
  } catch (error) {
    console.error('加载待审批数量失败')
  }
}

const loadPendingPermits = async () => {
  searchForm.status = '待审批'
  handleSearch()
}

const showStatistics = async () => {
  try {
    const response = await cuttingPermitApi.getStatistics({ groupBy: 'month' })
    statistics.value = response
    statisticsDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
  
  // 显示搜索条件（用于调试）
  const searchConditions = []
  if (searchForm.permitNo) searchConditions.push(`许可证编号: ${searchForm.permitNo}`)
  if (searchForm.applicantName) searchConditions.push(`申请人: ${searchForm.applicantName}`)
  if (searchForm.status) searchConditions.push(`状态: ${searchForm.status}`)
  
  if (searchConditions.length > 0) {
    console.log('搜索条件:', searchConditions.join(', '))
  }
}

const handleReset = () => {
  // 重置搜索表单
  searchForm.permitNo = ''
  searchForm.applicantName = ''
  searchForm.status = ''
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
    formData[key] = row[key] || ''
  })
  dialogVisible.value = true
}

const handleApprove = (row) => {
  currentApprovalPermit.value = row
  approvalForm.approver = ''
  approvalForm.opinion = ''
  approvalDialogVisible.value = true
}

const handleViewDetail = (row) => {
  currentDetailPermit.value = row
  detailDialogVisible.value = true
}

const handleApprovalSubmit = async (action) => {
  if (!approvalFormRef.value) return
  
  try {
    await approvalFormRef.value.validate()
    
    approvalLoading.value = true
    
    const data = {
      action,
      approver: approvalForm.approver,
      opinion: approvalForm.opinion
    }
    
    await cuttingPermitApi.approve(currentApprovalPermit.value.id, data)
    
    ElMessage.success(action === 'approve' ? '审批通过' : '审批拒绝')
    approvalDialogVisible.value = false
    loadData()
    loadPendingCount()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审批失败')
    }
  } finally {
    approvalLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除许可"${row.permitNo}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await cuttingPermitApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
    loadPendingCount()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条许可吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    await cuttingPermitApi.delete(`batch`, { data: ids })
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
    loadPendingCount()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleSubmit = async (data) => {
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await cuttingPermitApi.update(currentEditId.value, data)
      ElMessage.success('更新成功')
    } else {
      await cuttingPermitApi.create(data)
      ElMessage.success('申请提交成功')
    }
    
    dialogVisible.value = false
    loadData()
    loadPendingCount()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '申请提交失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  resetFormData()
}

const resetFormData = () => {
  Object.keys(formData).forEach(key => {
    if (key === 'forestLandId' || key === 'cuttingArea' || key === 'cuttingVolume') {
      formData[key] = null
    } else {
      formData[key] = ''
    }
  })
}

const getStatusType = (status) => {
  const typeMap = {
    '待审批': 'warning',
    '已批准': 'success',
    '已拒绝': 'danger',
    '已过期': 'info'
  }
  return typeMap[status] || 'info'
}

const isExpiringSoon = (validUntil) => {
  if (!validUntil) return false
  const expiry = new Date(validUntil)
  const now = new Date()
  const thirtyDaysLater = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000)
  return expiry <= thirtyDaysLater && expiry >= now
}
</script>

<style scoped>
.cutting-permit {
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

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 操作列按钮样式优化 */
:deep(.el-table__cell) {
  .el-button {
    margin-right: 4px;
    margin-bottom: 2px;
    padding: 5px 8px;
    font-size: 12px;
  }
  
  .el-button--small {
    padding: 5px 8px;
    font-size: 12px;
  }
}

/* 确保操作列按钮不换行 */
:deep(.el-table__cell:last-child) {
  white-space: nowrap;
  padding: 8px 12px;
}
</style>