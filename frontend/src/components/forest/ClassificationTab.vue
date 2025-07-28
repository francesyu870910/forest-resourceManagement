<template>
  <div class="classification-tab">
    <!-- 分类统计 -->
    <div class="classification-stats">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">数量</div>
            <div class="stat-value">{{ stats }} 个</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">总面积</div>
            <div class="stat-value">{{ area }} 公顷</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-label">平均面积</div>
            <div class="stat-value">{{ avgArea }} 公顷</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 林地列表 -->
    <div class="forest-list">
      <el-row :gutter="20">
        <el-col 
          v-for="forestLand in forestLandList" 
          :key="forestLand.id"
          :span="8"
          class="forest-item-col"
        >
          <el-card class="forest-item" shadow="hover">
            <div class="forest-header">
              <h4>{{ forestLand.name }}</h4>
              <div class="forest-actions">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="$emit('edit', forestLand)"
                >
                  编辑
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="$emit('delete', forestLand)"
                >
                  删除
                </el-button>
              </div>
            </div>
            
            <div class="forest-info">
              <div class="info-item">
                <span class="info-label">面积：</span>
                <span class="info-value">{{ forestLand.area.toFixed(2) }} 公顷</span>
              </div>
              <div class="info-item">
                <span class="info-label">位置：</span>
                <span class="info-value">{{ forestLand.location }}</span>
              </div>
              <div v-if="forestLand.coordinates" class="info-item">
                <span class="info-label">坐标：</span>
                <span class="info-value">{{ forestLand.coordinates }}</span>
              </div>
              <div v-if="forestLand.description" class="info-item">
                <span class="info-label">描述：</span>
                <span class="info-value">{{ forestLand.description }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">创建时间：</span>
                <span class="info-value">{{ formatDateTime(forestLand.createTime) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 空状态 -->
      <div v-if="forestLandList.length === 0" class="empty-state">
        <el-empty description="暂无该分类的林地数据" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { forestLandApi } from '../../utils/api'

const props = defineProps({
  classification: {
    type: String,
    required: true
  },
  stats: {
    type: Number,
    default: 0
  },
  area: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['edit', 'delete'])

const forestLandList = ref([])
const loading = ref(false)

const avgArea = computed(() => {
  if (props.stats === 0) return 0
  return (props.area / props.stats).toFixed(2)
})

const loadForestLandsByClassification = async () => {
  loading.value = true
  try {
    const response = await forestLandApi.getList({
      classification: props.classification,
      page: 1,
      size: 100 // 加载更多数据用于展示
    })
    forestLandList.value = response.data
  } catch (error) {
    ElMessage.error('加载林地数据失败')
  } finally {
    loading.value = false
  }
}

// 监听分类变化，重新加载数据
watch(() => props.classification, () => {
  loadForestLandsByClassification()
}, { immediate: true })

onMounted(() => {
  loadForestLandsByClassification()
})

onBeforeUnmount(() => {
  // 清理可能的异步操作
  loading.value = false
})

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.classification-tab {
  padding: 20px 0;
}

.classification-stats {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.forest-list {
  min-height: 400px;
}

.forest-item-col {
  margin-bottom: 20px;
}

.forest-item {
  height: 100%;
  transition: all 0.3s;
}

.forest-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.forest-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.forest-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.forest-actions {
  display: flex;
  gap: 8px;
}

.forest-info {
  space-y: 8px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-label {
  color: #909399;
  min-width: 70px;
  flex-shrink: 0;
}

.info-value {
  color: #606266;
  flex: 1;
  word-break: break-all;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}
</style>