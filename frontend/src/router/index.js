import { createRouter, createWebHistory } from 'vue-router'
import Overview from '../components/dashboard/Overview.vue'
import TreeArchive from '../components/forest/TreeArchive.vue'
import ForestClassify from '../components/forest/ForestClassify.vue'
import ResourceMonitor from '../components/forest/ResourceMonitor.vue'
import ForestRights from '../components/management/ForestRights.vue'
import CuttingPermit from '../components/management/CuttingPermit.vue'
import Statistics from '../components/statistics/Statistics.vue'
import Settings from '../components/settings/Settings.vue'

const routes = [
  {
    path: '/',
    redirect: '/overview'
  },
  {
    path: '/overview',
    name: 'Overview',
    component: Overview,
    meta: { title: '系统概览' }
  },
  {
    path: '/tree-archive',
    name: 'TreeArchive',
    component: TreeArchive,
    meta: { title: '林木档案管理' }
  },
  {
    path: '/forest-classify',
    name: 'ForestClassify',
    component: ForestClassify,
    meta: { title: '林地分类管理' }
  },
  {
    path: '/resource-monitor',
    name: 'ResourceMonitor',
    component: ResourceMonitor,
    meta: { title: '资源动态监测' }
  },
  {
    path: '/forest-rights',
    name: 'ForestRights',
    component: ForestRights,
    meta: { title: '林权证书管理' }
  },
  {
    path: '/cutting-permit',
    name: 'CuttingPermit',
    component: CuttingPermit,
    meta: { title: '采伐许可管理' }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: Statistics,
    meta: { title: '统计报表' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { title: '系统设置' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router