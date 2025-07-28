<template>
  <div class="settings">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <!-- 基本设置 -->
        <el-card class="settings-card">
          <template #header>
            <span>基本设置</span>
          </template>
          <el-form :model="basicSettings" label-width="100px">
            <el-form-item label="系统名称">
              <el-input v-model="basicSettings.systemName" />
            </el-form-item>
            <el-form-item label="系统版本">
              <el-input v-model="basicSettings.version" readonly />
            </el-form-item>
            <el-form-item label="管理单位">
              <el-input v-model="basicSettings.organization" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="basicSettings.phone" />
            </el-form-item>
            <el-form-item label="系统描述">
              <el-input
                v-model="basicSettings.description"
                type="textarea"
                :rows="3"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 数据设置 -->
        <el-card class="settings-card">
          <template #header>
            <span>数据设置</span>
          </template>
          <el-form :model="dataSettings" label-width="100px">
            <el-form-item label="分页大小">
              <el-select v-model="dataSettings.pageSize" style="width: 100%">
                <el-option label="10条/页" :value="10" />
                <el-option label="20条/页" :value="20" />
                <el-option label="50条/页" :value="50" />
                <el-option label="100条/页" :value="100" />
              </el-select>
            </el-form-item>
            <el-form-item label="刷新间隔">
              <el-select v-model="dataSettings.refreshInterval" style="width: 100%">
                <el-option label="不自动刷新" :value="0" />
                <el-option label="30秒" :value="30" />
                <el-option label="1分钟" :value="60" />
                <el-option label="5分钟" :value="300" />
              </el-select>
            </el-form-item>
            <el-form-item label="数据缓存">
              <el-switch v-model="dataSettings.enableCache" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveDataSettings">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <!-- 通知设置 -->
        <el-card class="settings-card">
          <template #header>
            <span>通知设置</span>
          </template>
          <el-form :model="notificationSettings" label-width="100px">
            <el-form-item label="证书到期">
              <el-switch v-model="notificationSettings.rightsExpiry" />
              <span class="setting-desc">到期前30天提醒</span>
            </el-form-item>
            <el-form-item label="许可到期">
              <el-switch v-model="notificationSettings.permitExpiry" />
              <span class="setting-desc">到期前30天提醒</span>
            </el-form-item>
            <el-form-item label="资源预警">
              <el-switch v-model="notificationSettings.resourceAlert" />
              <span class="setting-desc">异常变化时预警</span>
            </el-form-item>
            <el-form-item label="邮件通知">
              <el-switch v-model="notificationSettings.emailNotification" />
            </el-form-item>
            <el-form-item label="短信通知">
              <el-switch v-model="notificationSettings.smsNotification" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveNotificationSettings">
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 系统信息 -->
        <el-card class="info-card">
          <template #header>
            <span>系统信息</span>
          </template>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">系统版本</span>
              <span class="info-value">v2.1.3</span>
            </div>
            <div class="info-item">
              <span class="info-label">构建时间</span>
              <span class="info-value">2024-12-15</span>
            </div>
            <div class="info-item">
              <span class="info-label">运行环境</span>
              <span class="info-value">生产环境</span>
            </div>
            <div class="info-item">
              <span class="info-label">数据存储</span>
              <span class="info-value">MySQL 8.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后更新</span>
              <span class="info-value">{{ lastUpdateTime }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 响应式数据
const lastUpdateTime = ref('')

// 设置数据
const basicSettings = reactive({
  systemName: '森林资源管理系统',
  version: 'v2.1.3',
  organization: '林业管理局',
  phone: '400-123-4567',
  description: '基于Web的森林资源数字化管理平台，提供林木档案、林地分类、资源监测、林权证书、采伐许可等全方位管理功能。'
})

const dataSettings = reactive({
  pageSize: 10,
  refreshInterval: 0,
  enableCache: true
})

const notificationSettings = reactive({
  rightsExpiry: true,
  permitExpiry: true,
  resourceAlert: true,
  emailNotification: false,
  smsNotification: false
})

// 生命周期
onMounted(() => {
  loadSettings()
  updateLastUpdateTime()
})

// 方法
const loadSettings = () => {
  // 从localStorage加载设置
  const savedBasicSettings = localStorage.getItem('basicSettings')
  if (savedBasicSettings) {
    Object.assign(basicSettings, JSON.parse(savedBasicSettings))
  }
  
  const savedDataSettings = localStorage.getItem('dataSettings')
  if (savedDataSettings) {
    Object.assign(dataSettings, JSON.parse(savedDataSettings))
  }
  
  const savedNotificationSettings = localStorage.getItem('notificationSettings')
  if (savedNotificationSettings) {
    Object.assign(notificationSettings, JSON.parse(savedNotificationSettings))
  }
}

const saveBasicSettings = () => {
  localStorage.setItem('basicSettings', JSON.stringify(basicSettings))
  ElMessage.success('基本设置保存成功')
  updateLastUpdateTime()
}

const saveDataSettings = () => {
  localStorage.setItem('dataSettings', JSON.stringify(dataSettings))
  ElMessage.success('数据设置保存成功')
  updateLastUpdateTime()
}

const saveNotificationSettings = () => {
  localStorage.setItem('notificationSettings', JSON.stringify(notificationSettings))
  ElMessage.success('通知设置保存成功')
  updateLastUpdateTime()
}



const updateLastUpdateTime = () => {
  lastUpdateTime.value = new Date().toLocaleString('zh-CN')
}
</script>

<style scoped>
.settings {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.settings-card {
  margin-bottom: 20px;
  height: fit-content;
}

.info-card {
  margin-bottom: 20px;
  height: fit-content;
}

.setting-desc {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.info-list {
  padding: 10px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 8px 0;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.info-label {
  color: #606266;
  font-weight: 500;
}

.info-value {
  color: #303133;
  font-weight: 600;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .el-col {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .settings {
    padding: 15px;
  }
  
  .el-form {
    .el-form-item__label {
      font-size: 13px;
    }
  }
}

</style>