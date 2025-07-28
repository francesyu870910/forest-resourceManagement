<template>
  <div class="overview">
    <div class="page-header">
      <h2>系统概览</h2>
      <div class="header-actions">
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409eff;">
              <el-icon size="24"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalTrees || 0 }}</div>
              <div class="stat-title">林木档案</div>
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
              <div class="stat-value">{{ statistics.totalForestLands || 0 }}</div>
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
              <div class="stat-value">{{ statistics.totalRights || 0 }}</div>
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
              <div class="stat-value">{{ statistics.totalPermits || 0 }}</div>
              <div class="stat-title">采伐许可</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预警信息 -->
    <el-card v-if="alerts.length > 0" class="alerts-card">
      <template #header>
        <div class="card-header">
          <span>预警信息</span>
          <el-badge :value="alerts.length" class="alert-badge" />
        </div>
      </template>
      <div class="alerts-list">
        <el-alert
          v-for="alert in alerts.slice(0, 3)"
          :key="alert.type + alert.message"
          :title="alert.title"
          :type="alert.level"
          :description="alert.message"
          show-icon
          :closable="false"
          class="alert-item"
        />
        <div v-if="alerts.length > 3" class="more-alerts">
          <el-link type="primary">查看更多预警信息 ({{ alerts.length - 3 }})</el-link>
        </div>
      </div>
    </el-card>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>林地分类分布</span>
          </template>
          <div class="chart-container">
            <v-chart
              v-if="forestClassificationChart"
              :option="forestClassificationChart"
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
            <span>林木健康状态</span>
          </template>
          <div class="chart-container">
            <v-chart
              v-if="treeHealthChart"
              :option="treeHealthChart"
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

    <!-- 趋势分析和最近活动 -->
    <el-row :gutter="20" class="bottom-section">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>资源变化趋势</span>
          </template>
          <div class="chart-container">
            <v-chart
              v-if="trendsChart"
              :option="trendsChart"
              style="height: 300px"
            />
            <div v-else class="chart-placeholder">
              <el-icon size="48"><DataAnalysis /></el-icon>
              <p>趋势分析图</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近活动</span>
          </template>
          <div class="activities-container">
            <el-timeline v-if="recentActivities.length > 0">
              <el-timeline-item
                v-for="activity in recentActivities.slice(0, 6)"
                :key="activity.type + activity.description"
                :timestamp="formatDateTime(activity.time)"
                placement="top"
              >
                <div class="activity-item">
                  <el-icon><component :is="activity.icon" /></el-icon>
                  <div class="activity-content">
                    <div class="activity-title">{{ activity.title }}</div>
                    <div class="activity-desc">{{ activity.description }}</div>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无最近活动" />
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 快速操作 -->
    <el-card class="quick-actions">
      <template #header>
        <span>快速操作</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="4" v-for="action in quickActions" :key="action.title">
          <div class="action-item" @click="handleQuickAction(action.route)">
            <el-icon size="32"><component :is="action.icon" /></el-icon>
            <p>{{ action.title }}</p>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  Document,
  MapLocation,
  TrendCharts,
  Notebook,
  EditPen,
  PieChart,
  DataAnalysis
} from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart as EChartsPie, LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { overviewApi } from '../../utils/api'
import { formatDateTime } from '../../utils/common'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  EChartsPie,
  LineChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const router = useRouter()

// 响应式数据
const statistics = ref({})
const alerts = ref([])
const recentActivities = ref([])
const trends = ref({})

// 图表配置
const forestClassificationChart = ref(null)
const treeHealthChart = ref(null)
const trendsChart = ref(null)

const quickActions = ref([
  {
    title: '新增林木',
    icon: 'Document',
    route: '/tree-archive'
  },
  {
    title: '林地管理',
    icon: 'MapLocation',
    route: '/forest-classify'
  },
  {
    title: '资源监测',
    icon: 'TrendCharts',
    route: '/resource-monitor'
  },
  {
    title: '证书管理',
    icon: 'Notebook',
    route: '/forest-rights'
  },
  {
    title: '许可审批',
    icon: 'EditPen',
    route: '/cutting-permit'
  },
  {
    title: '统计分析',
    icon: 'DataAnalysis',
    route: '/statistics'
  }
])

const handleQuickAction = (route) => {
  router.push(route)
}

onMounted(() => {
  loadAllData()
})

const loadAllData = async () => {
  await Promise.all([
    loadStatistics(),
    loadAlerts(),
    loadRecentActivities(),
    loadTrends()
  ])
  generateCharts()
}

const loadStatistics = async () => {
  try {
    const response = await overviewApi.getStatistics()
    statistics.value = response
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const loadAlerts = async () => {
  try {
    const response = await overviewApi.getAlerts()
    alerts.value = response
  } catch (error) {
    ElMessage.error('加载预警信息失败')
  }
}

const loadRecentActivities = async () => {
  try {
    const response = await overviewApi.getRecentActivities()
    recentActivities.value = response
  } catch (error) {
    ElMessage.error('加载最近活动失败')
  }
}

const loadTrends = async () => {
  try {
    const response = await overviewApi.getTrends({ year: '2024' })
    trends.value = response
  } catch (error) {
    ElMessage.error('加载趋势数据失败')
  }
}

const generateCharts = () => {
  // 林地分类分布饼图
  if (statistics.value.forestClassificationStats) {
    const data = Object.entries(statistics.value.forestClassificationStats).map(([name, value]) => ({
      name,
      value
    }))
    
    forestClassificationChart.value = {
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
  
  // 林木健康状态柱状图
  if (statistics.value.treeHealthStats) {
    const categories = Object.keys(statistics.value.treeHealthStats)
    const values = Object.values(statistics.value.treeHealthStats)
    
    treeHealthChart.value = {
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
  
  // 趋势分析图
  if (trends.value.monthlyTrends) {
    const months = trends.value.monthlyTrends.map(item => item.monthName)
    const growthData = trends.value.monthlyTrends.map(item => item.avgGrowth)
    const volumeData = trends.value.monthlyTrends.map(item => item.avgVolume)
    
    trendsChart.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        }
      },
      legend: {
        data: ['平均生长量', '平均蓄积量']
      },
      xAxis: {
        type: 'category',
        data: months
      },
      yAxis: [
        {
          type: 'value',
          name: '生长量 (cm/年)',
          position: 'left'
        },
        {
          type: 'value',
          name: '蓄积量 (立方米)',
          position: 'right'
        }
      ],
      series: [
        {
          name: '平均生长量',
          type: 'line',
          yAxisIndex: 0,
          data: growthData,
          smooth: true,
          itemStyle: {
            color: '#409eff'
          }
        },
        {
          name: '平均蓄积量',
          type: 'line',
          yAxisIndex: 1,
          data: volumeData,
          smooth: true,
          itemStyle: {
            color: '#67c23a'
          }
        }
      ]
    }
  }
}

const refreshData = () => {
  loadAllData()
  ElMessage.success('数据刷新成功')
}
</script>

<style scoped>
.overview {
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

.alerts-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-badge {
  margin-left: 10px;
}

.alerts-list {
  max-height: 200px;
  overflow-y: auto;
}

.alert-item {
  margin-bottom: 10px;
}

.more-alerts {
  text-align: center;
  padding: 10px;
}

.charts-section {
  margin-bottom: 20px;
}

.bottom-section {
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

.activities-container {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.activity-desc {
  font-size: 12px;
  color: #909399;
}

.quick-actions .action-item {
  text-align: center;
  padding: 20px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
}

.quick-actions .action-item:hover {
  background-color: #f5f7fa;
}

.quick-actions .action-item p {
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
}
</style>