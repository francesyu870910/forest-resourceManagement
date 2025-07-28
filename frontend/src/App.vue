<template>
  <div id="app">
    <el-container class="layout-container">
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
import NavMenu from './components/common/NavMenu.vue'
import Breadcrumb from './components/common/Breadcrumb.vue'

export default {
  name: 'App',
  components: {
    NavMenu,
    Breadcrumb
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