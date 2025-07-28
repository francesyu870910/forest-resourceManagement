<template>
  <div class="tree-archive">
    <div class="page-header">
      <h2>林木资源档案管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          新增林木档案
        </el-button>
        <el-button @click="exportData">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="树种">
          <el-input
            v-model="searchForm.treeSpecies"
            placeholder="请输入树种"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="健康状态">
          <el-select
            v-model="searchForm.healthStatus"
            placeholder="请选择健康状态"
            clearable
            style="width: 150px"
          >
            <el-option label="健康" value="健康" />
            <el-option label="良好" value="良好" />
            <el-option label="一般" value="一般" />
            <el-option label="较差" value="较差" />
            <el-option label="病虫害" value="病虫害" />
          </el-select>
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
        @selection-change="handleSelectionChange"
        @edit="handleEdit"
        @delete="handleDelete"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      >
        <template #healthStatus="{ row }">
          <el-tag
            :type="getHealthStatusType(row.healthStatus)"
            size="small"
          >
            {{ row.healthStatus }}
          </el-tag>
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
        <el-form-item label="树种" prop="treeSpecies">
          <el-input
            v-model="formData.treeSpecies"
            placeholder="请输入树种名称"
          />
        </el-form-item>
        
        <el-form-item label="胸径(cm)" prop="diameter">
          <el-input-number
            v-model="formData.diameter"
            :min="5"
            :max="100"
            :precision="1"
            placeholder="请输入胸径"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="树高(m)" prop="height">
          <el-input-number
            v-model="formData.height"
            :min="1"
            :max="50"
            :precision="1"
            placeholder="请输入树高"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="健康状态" prop="healthStatus">
          <el-select
            v-model="formData.healthStatus"
            placeholder="请选择健康状态"
            style="width: 100%"
          >
            <el-option label="健康" value="健康" />
            <el-option label="良好" value="良好" />
            <el-option label="一般" value="一般" />
            <el-option label="较差" value="较差" />
            <el-option label="病虫害" value="病虫害" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="位置信息" prop="location">
          <el-input
            v-model="formData.location"
            placeholder="请输入位置信息"
            type="textarea"
            :rows="2"
          />
        </el-form-item>
      </template>
    </FormDialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Download } from '@element-plus/icons-vue'
import DataTable from '../common/DataTable.vue'
import FormDialog from '../common/FormDialog.vue'
import { treeArchiveApi } from '../../utils/api'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])

// 搜索表单
const searchForm = reactive({
  treeSpecies: '',
  healthStatus: '',
  location: ''
})

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentEditId = ref(null)

const dialogTitle = computed(() => {
  return isEdit.value ? '编辑林木档案' : '新增林木档案'
})

// 表单数据
const formData = reactive({
  treeSpecies: '',
  diameter: null,
  height: null,
  healthStatus: '',
  location: ''
})

// 表单验证规则
const formRules = {
  treeSpecies: [
    { required: true, message: '请输入树种名称', trigger: 'blur' },
    { min: 2, max: 20, message: '树种名称长度在2-20个字符', trigger: 'blur' }
  ],
  diameter: [
    { required: true, message: '请输入胸径', trigger: 'blur' },
    { type: 'number', min: 5, max: 100, message: '胸径必须在5-100cm之间', trigger: 'blur' }
  ],
  height: [
    { required: true, message: '请输入树高', trigger: 'blur' },
    { type: 'number', min: 1, max: 50, message: '树高必须在1-50m之间', trigger: 'blur' }
  ],
  healthStatus: [
    { required: true, message: '请选择健康状态', trigger: 'change' }
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
  { prop: 'treeSpecies', label: '树种', minWidth: 120 },
  { prop: 'diameter', label: '胸径(cm)', width: 100 },
  { prop: 'height', label: '树高(m)', width: 100 },
  { prop: 'healthStatus', label: '健康状态', width: 120, slot: 'healthStatus' },
  { prop: 'location', label: '位置', minWidth: 150 },
  { prop: 'createTime', label: '创建时间', width: 180, formatter: formatDateTime }
]

// 生命周期
onMounted(() => {
  loadData()
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
      ...searchForm
    }
    
    const response = await treeArchiveApi.getList(params)
    tableData.value = response.data
    total.value = response.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
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
    formData[key] = row[key] || ''
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除林木档案"${row.treeSpecies}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await treeArchiveApi.delete(row.id)
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
      `确定要删除选中的 ${selectedRows.value.length} 条林木档案吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    await treeArchiveApi.delete(`batch`, { data: ids })
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
    if (isEdit.value) {
      await treeArchiveApi.update(currentEditId.value, data)
      ElMessage.success('更新成功')
    } else {
      await treeArchiveApi.create(data)
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
    formData[key] = key === 'diameter' || key === 'height' ? null : ''
  })
}

const getHealthStatusType = (status) => {
  const typeMap = {
    '健康': 'success',
    '良好': 'success',
    '一般': 'warning',
    '较差': 'danger',
    '病虫害': 'danger'
  }
  return typeMap[status] || 'info'
}

const exportData = () => {
  try {
    // 准备导出数据
    const exportTime = new Date().toLocaleString('zh-CN')
    let csvContent = '林木资源档案数据导出\n'
    csvContent += `导出时间：${exportTime}\n\n`
    
    // CSV表头
    csvContent += 'ID,树种,胸径(cm),树高(m),健康状态,种植日期,位置信息,备注\n'
    
    // 数据行
    tableData.value.forEach(item => {
      const row = [
        item.id || '',
        item.treeSpecies || '',
        item.diameter || '',
        item.height || '',
        item.healthStatus || '',
        item.plantingDate ? new Date(item.plantingDate).toLocaleDateString('zh-CN') : '',
        (item.location || '').replace(/,/g, '，'), // 替换逗号避免CSV格式问题
        (item.remarks || '').replace(/,/g, '，')
      ]
      csvContent += row.join(',') + '\n'
    })
    
    // 添加统计信息
    csvContent += '\n统计信息：\n'
    csvContent += `总记录数：${total.value}\n`
    
    // 按健康状态统计
    const healthStats = {}
    tableData.value.forEach(item => {
      const status = item.healthStatus || '未知'
      healthStats[status] = (healthStats[status] || 0) + 1
    })
    
    csvContent += '健康状态分布：\n'
    Object.entries(healthStats).forEach(([status, count]) => {
      csvContent += `${status}：${count}\n`
    })
    
    // 创建并下载文件
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `林木资源档案_${new Date().toISOString().slice(0, 10)}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    ElMessage.success('数据导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('数据导出失败')
  }
}
</script>

<style scoped>
.tree-archive {
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
</style>