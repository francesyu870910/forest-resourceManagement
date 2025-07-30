<template>
  <div id="app">
    <!-- 登录页面布局 -->
    <div v-if="isAuthPage" class="auth-layout">
      <router-view />
    </div>
    
    <!-- 主应用布局 -->
    <el-container v-else class="layout-container">
      <el-header class="header">
        <NavMenu />
      </el-header>
      
      <el-main class="main-content">
        <Breadcrumb />
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { computed, provide, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import NavMenu from './components/common/NavMenu.vue'
import Breadcrumb from './components/common/Breadcrumb.vue'
import { isLoggedIn, getUserInfo } from './utils/auth'

export default {
  name: 'App',
  components: {
    NavMenu,
    Breadcrumb
  },
  setup() {
    const route = useRoute()
    const userInfo = ref(null)
    const loginStatus = ref(false)
    
    // 判断是否为认证相关页面
    const isAuthPage = computed(() => {
      return ['Login', 'ResetPassword'].includes(route.name)
    })
    
    // 更新用户信息
    const updateUserInfo = () => {
      loginStatus.value = isLoggedIn()
      userInfo.value = getUserInfo()
    }
    
    // 提供全局状态给子组件
    provide('userInfo', userInfo)
    provide('loginStatus', loginStatus)
    provide('updateUserInfo', updateUserInfo)
    
    onMounted(() => {
      updateUserInfo()
    })
    
    return {
      isAuthPage,
      userInfo,
      loginStatus
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  height: 100vh;
}

.layout-container {
  height: 100vh;
}

.header {
  padding: 0;
  height: 60px;
}

.main-content {
  background-color: #f8f9fa;
  padding: 20px;
}

.auth-layout {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-container {
    flex-direction: column;
  }
  
  .header {
    padding: 0 10px;
  }
  
  .main-content {
    padding: 10px;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 5px;
  }
}
</style>