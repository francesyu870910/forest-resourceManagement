<template>
  <div class="forest-classify">
    <div class="page-header">
      <h2>林地分类管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          新增林地信息
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409eff;">
              <el-icon size="24"><MapLocation /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCount || 0 }}</div>
              <div class="stat-title">林地总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67c23a;">
              <el-icon size="24"><Crop /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalArea || 0 }}</div>
              <div class="stat-title">总面积(公顷)</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #e6a23c;">
              <el-icon size="24"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.avgArea || 0 }}</div>
              <div class="stat-title">平均面积(公顷)</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #f56c6c;">
              <el-icon size="24"><PieChart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">3</div>
              <div class="stat-title">分类类型</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类标签页 -->
    <el-card class="tabs-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all">
          <div class="tab-content">
            <div class="search-bar">
              <el-form :model="searchForm" inline>
                <el-form-item label="林地名称">
                  <el-input
                    v-model="searchForm.name"
                    placeholder="请输入林地名称"
                    clearable
                    style="width: 200px"
                  />
                </el-form-item>
                <el-form-item label="位置">
                  <el-input
                    v-model="searchForm.location"
                    placeholder="请输入位置"
                    clearable
                    style="width: 200px"
                  />
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
            </div>
            
            <DataTable
              :data="tableData"
              :columns="tableColumns"
              :loading="loading"
              :total="total"
              :current-page="currentPage"
              :page-size="pageSize"
              :show-selection="true"
              @selection-change="handleSelectionChange"
              @edit="handleEdit"
              @delete="handleDelete"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            >
              <template #classification="{ row }">
                <el-tag
                  :type="getClassificationType(row.classification)"
                  size="small"
                >
                  {{ row.classification }}
                </el-tag>
              </template>
              
              <template #area="{ row }">
                {{ row.area.toFixed(2) }} 公顷
              </template>
              
              <template #actions="{ row }">
                <el-button type="primary" size="small" @click="handleEdit(row)">
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">
                  删除
                </el-button>
              </template>
            </DataTable>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="用材林" name="用材林">
          <ClassificationTab 
            classification="用材林"
            :stats="stats.classificationCounts?.['用材林'] || 0"
            :area="stats.classificationAreas?.['用材林'] || 0"
            @edit="handleEdit"
            @delete="handleDelete"
          />
        </el-tab-pane>
        
        <el-tab-pane label="防护林" name="防护林">
          <ClassificationTab 
            classification="防护林"
            :stats="stats.classificationCounts?.['防护林'] || 0"
            :area="stats.classificationAreas?.['防护林'] || 0"
            @edit="handleEdit"
            @delete="handleDelete"
          />
        </el-tab-pane>
        
        <el-tab-pane label="经济林" name="经济林">
          <ClassificationTab 
            classification="经济林"
            :stats="stats.classificationCounts?.['经济林'] || 0"
            :area="stats.classificationAreas?.['经济林'] || 0"
            @edit="handleEdit"
            @delete="handleDelete"
          />
        </el-tab-pane>
      </el-tabs>
      
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
      @confirm="handleSubmit"
      @close="handleDialogClose"
    >
      <template #form="{ formData }">
        <el-form-item label="林地名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入林地名称"
          />
        </el-form-item>
        
        <el-form-item label="林地分类" prop="classification">
          <el-select
            v-model="formData.classification"
            placeholder="请选择林地分类"
            style="width: 100%"
          >
            <el-option label="用材林" value="用材林" />
            <el-option label="防护林" value="防护林" />
            <el-option label="经济林" value="经济林" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="面积(公顷)" prop="area">
          <el-input-number
            v-model="formData.area"
            :min="0.1"
            :max="10000"
            :precision="2"
            placeholder="请输入面积"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="位置信息" prop="location">
          <el-input
            v-model="formData.location"
            placeholder="请输入位置信息"
          />
        </el-form-item>
        
        <el-form-item label="坐标信息" prop="coordinates">
          <el-input
            v-model="formData.coordinates"
            placeholder="请输入坐标信息，格式：纬度,经度"
          />
        </el-form-item>
        
        <el-form-item label="描述信息" prop="description">
          <el-input
            v-model="formData.description"
            placeholder="请输入描述信息"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, MapLocation, Crop, TrendCharts, PieChart } from '@element-plus/icons-vue'
import DataTable from '../common/DataTable.vue'
import FormDialog from '../common/FormDialog.vue'
import ClassificationTab from './ClassificationTab.vue'
import { forestLandApi } from '../../utils/api'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])
const activeTab = ref('all')
const stats = ref({})

// 搜索表单
const searchForm = reactive({
  name: '',
  location: ''
})

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentEditId = ref(null)

const dialogTitle = computed(() => {
  return isEdit.value ? '编辑林地信息' : '新增林地信息'
})

// 表单数据
const formData = reactive({
  name: '',
  classification: '',
  area: null,
  location: '',
  coordinates: '',
  description: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入林地名称', trigger: 'blur' },
    { min: 2, max: 50, message: '林地名称长度在2-50个字符', trigger: 'blur' }
  ],
  classification: [
    { required: true, message: '请选择林地分类', trigger: 'change' }
  ],
  area: [
    { required: true, message: '请输入面积', trigger: 'blur' },
    { type: 'number', min: 0.1, max: 10000, message: '面积必须在0.1-10000公顷之间', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入位置信息', trigger: 'blur' }
  ]
}

// 格式化日期时间函数
const formatDateTime = (row, column, cellValue) => {
  if (!cellValue) return ''
  return new Date(cellValue).toLocaleString('zh-CN')
}

// 表格列配置
const tableColumns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '林地名称', minWidth: 150 },
  { prop: 'classification', label: '分类', width: 120, slot: 'classification' },
  { prop: 'area', label: '面积', width: 120, slot: 'area' },
  { prop: 'location', label: '位置', minWidth: 150 },
  { prop: 'createTime', label: '创建时间', width: 180, formatter: formatDateTime }
]

// 生命周期
onMounted(() => {
  loadData()
  loadStats()
})

onBeforeUnmount(() => {
  // 清理可能的异步操作
  loading.value = false
  submitLoading.value = false
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      classification: activeTab.value === 'all' ? '' : activeTab.value,
      ...searchForm
    }
    
    const response = await forestLandApi.getList(params)
    tableData.value = response.data
    total.value = response.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await forestLandApi.getStats()
    stats.value = response
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const handleTabChange = (tabName) => {
  activeTab.value = tabName
  currentPage.value = 1
  loadData()
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
    formData[key] = row[key] || ''
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除林地"${row.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await forestLandApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
    loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条林地信息吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    await forestLandApi.delete(`batch`, { data: ids })
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
    loadStats()
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
      await forestLandApi.update(currentEditId.value, data)
      ElMessage.success('更新成功')
    } else {
      await forestLandApi.create(data)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
    loadStats()
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
    formData[key] = key === 'area' ? null : ''
  })
}

const getClassificationType = (classification) => {
  const typeMap = {
    '用材林': 'success',
    '防护林': 'warning',
    '经济林': 'danger'
  }
  return typeMap[classification] || 'info'
}
</script>

<style scoped>
.forest-classify {
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

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
}

.tabs-card {
  margin-bottom: 20px;
}

.tab-content {
  margin-top: 20px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
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
</style>