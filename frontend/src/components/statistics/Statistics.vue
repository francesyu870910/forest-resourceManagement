<template>
  <div class="statistics">
    <div class="page-header">
      <h2>统计报表</h2>
      <div class="header-actions">
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- 综合统计卡片 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409eff;">
              <el-icon size="24"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewStats.totalTrees || 0 }}</div>
              <div class="stat-title">林木总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67c23a;">
              <el-icon size="24"><MapLocation /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewStats.totalForestLands || 0 }}</div>
              <div class="stat-title">林地数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #e6a23c;">
              <el-icon size="24"><Notebook /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewStats.totalRights || 0 }}</div>
              <div class="stat-title">林权证书</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #f56c6c;">
              <el-icon size="24"><EditPen /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewStats.totalPermits || 0 }}</div>
              <div class="stat-title">采伐许可</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>林地分类分布</span>
          </template>
          <div class="chart-container">
            <v-chart
              v-if="forestLandChartOption"
              :option="forestLandChartOption"
              style="height: 300px"
            />
            <div v-else class="chart-placeholder">
              <el-icon size="48"><PieChart /></el-icon>
              <p>林地分类分布图</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>林木健康状态分布</span>
          </template>
          <div class="chart-container">
            <v-chart
              v-if="treeHealthChartOption"
              :option="treeHealthChartOption"
              style="height: 300px"
            />
            <div v-else class="chart-placeholder">
              <el-icon size="48"><TrendCharts /></el-icon>
              <p>健康状态分布图</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-row :gutter="20" class="tables-section">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>林权证书状态统计</span>
          </template>
          <el-table :data="rightsStatsData" size="small">
            <el-table-column prop="status" label="状态" />
            <el-table-column prop="count" label="数量" />
            <el-table-column prop="percentage" label="占比" />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>采伐许可状态统计</span>
          </template>
          <el-table :data="permitsStatsData" size="small">
            <el-table-column prop="status" label="状态" />
            <el-table-column prop="count" label="数量" />
            <el-table-column prop="percentage" label="占比" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, Download, Document, MapLocation, Notebook, EditPen, PieChart, TrendCharts 
} from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart as EChartsPie, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import { treeArchiveApi, forestLandApi, forestRightsApi, cuttingPermitApi } from '../../utils/api'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  EChartsPie,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent
])

// 响应式数据
const overviewStats = ref({})
const forestLandStats = ref({})
const treeHealthStats = ref({})
const rightsStats = ref({})
const permitsStats = ref({})

// 图表配置
const forestLandChartOption = ref(null)
const treeHealthChartOption = ref(null)

// 计算属性
const rightsStatsData = computed(() => {
  if (!rightsStats.value.statusStats) return []
  
  const total = Object.values(rightsStats.value.statusStats).reduce((sum, count) => sum + count, 0)
  
  return Object.entries(rightsStats.value.statusStats).map(([status, count]) => ({
    status,
    count,
    percentage: total > 0 ? ((count / total) * 100).toFixed(1) + '%' : '0%'
  }))
})

const permitsStatsData = computed(() => {
  if (!permitsStats.value.statusStats) return []
  
  const total = Object.values(permitsStats.value.statusStats).reduce((sum, count) => sum + count, 0)
  
  return Object.entries(permitsStats.value.statusStats).map(([status, count]) => ({
    status,
    count,
    percentage: total > 0 ? ((count / total) * 100).toFixed(1) + '%' : '0%'
  }))
})

// 生命周期
onMounted(() => {
  loadAllStats()
})

// 方法
const loadAllStats = async () => {
  await Promise.all([
    loadOverviewStats(),
    loadForestLandStats(),
    loadTreeHealthStats(),
    loadRightsStats(),
    loadPermitsStats()
  ])
  
  generateCharts()
}

const loadOverviewStats = async () => {
  try {
    const [treeStats, forestStats, rightsStatsRes, permitsStatsRes] = await Promise.all([
      treeArchiveApi.getList({ page: 1, size: 1 }),
      forestLandApi.getList({ page: 1, size: 1 }),
      forestRightsApi.getList({ page: 1, size: 1 }),
      cuttingPermitApi.getList({ page: 1, size: 1 })
    ])
    
    overviewStats.value = {
      totalTrees: treeStats.total,
      totalForestLands: forestStats.total,
      totalRights: rightsStatsRes.total,
      totalPermits: permitsStatsRes.total
    }
  } catch (error) {
    ElMessage.error('加载概览统计失败')
  }
}

const loadForestLandStats = async () => {
  try {
    const response = await forestLandApi.getStats()
    forestLandStats.value = response
  } catch (error) {
    ElMessage.error('加载林地统计失败')
  }
}

const loadTreeHealthStats = async () => {
  try {
    const response = await treeArchiveApi.getStatistics()
    treeHealthStats.value = response
  } catch (error) {
    ElMessage.error('加载林木统计失败')
  }
}

const loadRightsStats = async () => {
  try {
    const response = await forestRightsApi.getStatistics()
    rightsStats.value = response
  } catch (error) {
    ElMessage.error('加载林权统计失败')
  }
}

const loadPermitsStats = async () => {
  try {
    const response = await cuttingPermitApi.getStatistics()
    permitsStats.value = response
  } catch (error) {
    ElMessage.error('加载许可统计失败')
  }
}

const generateCharts = () => {
  // 林地分类分布饼图
  if (forestLandStats.value.classificationCounts) {
    const data = Object.entries(forestLandStats.value.classificationCounts).map(([name, value]) => ({
      name,
      value
    }))
    
    forestLandChartOption.value = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '林地分类',
          type: 'pie',
          radius: '50%',
          data,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
  }
  
  // 林木健康状态分布柱状图
  if (treeHealthStats.value.healthStats) {
    const categories = Object.keys(treeHealthStats.value.healthStats)
    const values = Object.values(treeHealthStats.value.healthStats)
    
    treeHealthChartOption.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: categories
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '数量',
          type: 'bar',
          data: values,
          itemStyle: {
            color: function(params) {
              const colors = ['#5cb87a', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
              return colors[params.dataIndex % colors.length]
            }
          }
        }
      ]
    }
  }
}

const refreshData = () => {
  loadAllStats()
  ElMessage.success('数据刷新成功')
}

const exportReport = () => {
  try {
    // 准备导出数据
    const reportData = {
      exportTime: new Date().toLocaleString('zh-CN'),
      overviewStats: overviewStats.value,
      forestLandStats: forestLandStats.value,
      treeHealthStats: treeHealthStats.value,
      rightsStats: rightsStats.value,
      permitsStats: permitsStats.value,
      rightsStatsData: rightsStatsData.value,
      permitsStatsData: permitsStatsData.value
    }
    
    // 生成报表内容
    let reportContent = `森林资源管理系统统计报表\n`
    reportContent += `导出时间：${reportData.exportTime}\n\n`
    
    // 概览统计
    reportContent += `=== 概览统计 ===\n`
    reportContent += `林木总数：${reportData.overviewStats.totalTrees || 0}\n`
    reportContent += `林地数量：${reportData.overviewStats.totalForestLands || 0}\n`
    reportContent += `林权证书：${reportData.overviewStats.totalRights || 0}\n`
    reportContent += `采伐许可：${reportData.overviewStats.totalPermits || 0}\n\n`
    
    // 林地分类统计
    if (reportData.forestLandStats.classificationCounts) {
      reportContent += `=== 林地分类统计 ===\n`
      Object.entries(reportData.forestLandStats.classificationCounts).forEach(([type, count]) => {
        reportContent += `${type}：${count}\n`
      })
      reportContent += `\n`
    }
    
    // 林木健康状态统计
    if (reportData.treeHealthStats.healthStats) {
      reportContent += `=== 林木健康状态统计 ===\n`
      Object.entries(reportData.treeHealthStats.healthStats).forEach(([status, count]) => {
        reportContent += `${status}：${count}\n`
      })
      reportContent += `\n`
    }
    
    // 林权证书状态统计
    if (reportData.rightsStatsData.length > 0) {
      reportContent += `=== 林权证书状态统计 ===\n`
      reportData.rightsStatsData.forEach(item => {
        reportContent += `${item.status}：${item.count}（${item.percentage}）\n`
      })
      reportContent += `\n`
    }
    
    // 采伐许可状态统计
    if (reportData.permitsStatsData.length > 0) {
      reportContent += `=== 采伐许可状态统计 ===\n`
      reportData.permitsStatsData.forEach(item => {
        reportContent += `${item.status}：${item.count}（${item.percentage}）\n`
      })
      reportContent += `\n`
    }
    
    // 创建并下载文件
    const blob = new Blob([reportContent], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `森林资源管理系统统计报表_${new Date().toISOString().slice(0, 10)}.txt`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    ElMessage.success('报表导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('报表导出失败')
  }
}


</script>

<style scoped>
.statistics {
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

.stats-overview {
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

.charts-section {
  margin-bottom: 20px;
}

.tables-section {
  margin-bottom: 20px;
}

.chart-container {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: #909399;
}

.chart-placeholder p {
  margin-top: 10px;
  font-size: 14px;
}
</style>