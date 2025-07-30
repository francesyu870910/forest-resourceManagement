<template>
  <div class="nav-container">
    <!-- 移动端菜单按钮 -->
    <div v-if="isMobile" class="mobile-menu-header">
      <div class="menu-title">
        <el-icon><Folder /></el-icon>
        <span>森林资源管理系统</span>
      </div>
      <el-button @click="toggleMobileMenu" class="menu-toggle">
        <el-icon><Menu /></el-icon>
      </el-button>
    </div>
    
    <!-- 菜单 -->
    <el-menu
      :default-active="activeIndex"
      :class="['nav-menu', { 'mobile-menu': isMobile, 'mobile-menu-open': mobileMenuOpen }]"
      :mode="isMobile ? 'vertical' : 'horizontal'"
      @select="handleSelect"
      background-color="#545c64"
      text-color="#fff"
      active-text-color="#ffd04b"
      :collapse="false"
    >
      <!-- 桌面端标题 -->
      <div v-if="!isMobile" class="menu-title">
        <el-icon><Folder /></el-icon>
        <span>森林资源管理系统</span>
      </div>
      
      <el-menu-item 
        v-for="item in menuItems" 
        :key="item.index" 
        :index="item.index"
        @click="handleMenuClick"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.title }}</span>
      </el-menu-item>
      
      <!-- 用户信息和登出 -->
      <div class="user-info">
        <el-dropdown @command="handleUserCommand">
          <span class="user-dropdown">
            <el-icon><User /></el-icon>
            <span>{{ userInfo?.username || '用户' }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                角色: {{ userInfo?.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                登出
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-menu>
    
    <!-- 移动端遮罩 -->
    <div 
      v-if="isMobile && mobileMenuOpen" 
      class="mobile-menu-overlay"
      @click="closeMobileMenu"
    ></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Folder,
  DataAnalysis,
  Document,
  Location,
  TrendCharts,
  Notebook,
  Edit,
  PieChart,
  Setting,
  Menu,
  User,
  SwitchButton,
  ArrowDown
} from '@element-plus/icons-vue'
import { useResponsive } from '../../utils/responsive'
import { getUserInfo, isAdmin, logout } from '../../utils/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const { isMobile } = useResponsive()

const mobileMenuOpen = ref(false)

// 从App.vue注入的用户信息
const injectedUserInfo = inject('userInfo', ref(null))
const injectedLoginStatus = inject('loginStatus', ref(false))
const updateUserInfo = inject('updateUserInfo', () => {})

const userInfo = ref(getUserInfo())
const isUserAdmin = ref(isAdmin())

// 监听注入的用户信息变化
watch(injectedUserInfo, (newUserInfo) => {
  userInfo.value = newUserInfo || getUserInfo()
  isUserAdmin.value = isAdmin()
}, { immediate: true })

const menuItems = computed(() => {
  const items = [
    { index: '/overview', title: '系统概览', icon: DataAnalysis },
    { index: '/tree-archive', title: '林木档案', icon: Document },
    { index: '/forest-classify', title: '林地分类', icon: Location },
    { index: '/resource-monitor', title: '资源监测', icon: TrendCharts },
    { index: '/forest-rights', title: '林权证书', icon: Notebook },
    { index: '/cutting-permit', title: '采伐许可', icon: Edit },
    { index: '/statistics', title: '统计报表', icon: PieChart },
    { index: '/settings', title: '系统设置', icon: Setting }
  ]
  
  // 如果是管理员，添加用户管理菜单
  if (isUserAdmin.value) {
    items.splice(-1, 0, { index: '/users', title: '用户管理', icon: User })
  }
  
  return items
})

const activeIndex = computed(() => {
  return route.path
})

const handleSelect = (key) => {
  router.push(key)
  if (isMobile.value) {
    closeMobileMenu()
  }
}

const handleMenuClick = () => {
  if (isMobile.value) {
    closeMobileMenu()
  }
}

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
}

const handleUserCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('登出失败:', error)
        ElMessage.error('登出失败')
      }
    }
  }
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
})
</script>

<style scoped>
.nav-container {
  position: relative;
}

.nav-menu {
  border-bottom: none;
}

.menu-title {
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  margin-right: 30px;
}

.menu-title .el-icon {
  margin-right: 8px;
  font-size: 20px;
}

.el-menu-item {
  font-size: 14px;
}

.el-menu-item .el-icon {
  margin-right: 5px;
}

.user-info {
  margin-left: auto;
  padding: 0 20px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  color: #fff;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.user-dropdown .el-icon {
  margin-right: 5px;
}

.user-dropdown .el-icon--right {
  margin-left: 5px;
  margin-right: 0;
}

/* 移动端样式 */
.mobile-menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
  height: 60px;
  background-color: #545c64;
  color: #fff;
}

.mobile-menu-header .menu-title {
  margin-right: 0;
  padding: 0;
}

.menu-toggle {
  background: none;
  border: none;
  color: #fff;
}

.mobile-menu {
  position: fixed;
  top: 60px;
  left: -100%;
  width: 280px;
  height: calc(100vh - 60px);
  background-color: #545c64;
  transition: left 0.3s ease;
  z-index: 1000;
  overflow-y: auto;
}

.mobile-menu-open {
  left: 0;
}

.mobile-menu .el-menu-item {
  padding-left: 20px !important;
}

.mobile-menu .user-info {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  padding: 0 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 20px;
}

.mobile-menu-overlay {
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

/* 响应式断点 */
@media (max-width: 768px) {
  .menu-title span {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .menu-title span {
    font-size: 14px;
  }
  
  .mobile-menu {
    width: 100%;
    left: -100%;
  }
}
</style>