<template>
  <el-breadcrumb separator="/" class="breadcrumb">
    <el-breadcrumb-item :to="{ path: '/' }">
      <el-icon><House /></el-icon>
      首页
    </el-breadcrumb-item>
    <el-breadcrumb-item
      v-for="item in breadcrumbItems"
      :key="item.path"
      :to="item.path ? { path: item.path } : null"
    >
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { House } from '@element-plus/icons-vue'

const route = useRoute()

// 路由标题映射
const routeTitleMap = {
  '/overview': '系统概览',
  '/tree-archive': '林木档案管理',
  '/forest-classify': '林地分类管理',
  '/resource-monitor': '资源动态监测',
  '/forest-rights': '林权证书管理',
  '/cutting-permit': '采伐许可管理',
  '/statistics': '统计报表',
  '/settings': '系统设置',
  '/help': '帮助文档'
}

const breadcrumbItems = computed(() => {
  const pathArray = route.path.split('/').filter(path => path)
  const items = []
  
  let currentPath = ''
  pathArray.forEach(path => {
    currentPath += `/${path}`
    const title = routeTitleMap[currentPath] || path
    items.push({
      path: currentPath,
      title
    })
  })
  
  return items
})
</script>

<style scoped>
.breadcrumb {
  margin-bottom: 16px;
}

.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #303133;
  font-weight: 500;
}
</style>