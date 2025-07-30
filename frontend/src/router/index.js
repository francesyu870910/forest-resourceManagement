import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, isAdmin } from '../utils/auth.js'
import Overview from '../components/dashboard/Overview.vue'
import TreeArchive from '../components/forest/TreeArchive.vue'
import ForestClassify from '../components/forest/ForestClassify.vue'
import ResourceMonitor from '../components/forest/ResourceMonitor.vue'
import ForestRights from '../components/management/ForestRights.vue'
import CuttingPermit from '../components/management/CuttingPermit.vue'
import UserManagement from '../components/management/UserManagement.vue'
import Statistics from '../components/statistics/Statistics.vue'
import Settings from '../components/settings/Settings.vue'
import Login from '../components/auth/Login.vue'
import ResetPassword from '../components/auth/ResetPassword.vue'

const routes = [
  {
    path: '/',
    redirect: (to) => {
      // 如果用户已登录，重定向到概览页面，否则重定向到登录页面
      return isLoggedIn() ? '/overview' : '/login'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '用户登录', requiresGuest: true }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: ResetPassword,
    meta: { title: '密码重置', requiresGuest: true }
  },
  {
    path: '/overview',
    name: 'Overview',
    component: Overview,
    meta: { title: '系统概览', requiresAuth: true }
  },
  {
    path: '/tree-archive',
    name: 'TreeArchive',
    component: TreeArchive,
    meta: { title: '林木档案管理', requiresAuth: true }
  },
  {
    path: '/forest-classify',
    name: 'ForestClassify',
    component: ForestClassify,
    meta: { title: '林地分类管理', requiresAuth: true }
  },
  {
    path: '/resource-monitor',
    name: 'ResourceMonitor',
    component: ResourceMonitor,
    meta: { title: '资源动态监测', requiresAuth: true }
  },
  {
    path: '/forest-rights',
    name: 'ForestRights',
    component: ForestRights,
    meta: { title: '林权证书管理', requiresAuth: true }
  },
  {
    path: '/cutting-permit',
    name: 'CuttingPermit',
    component: CuttingPermit,
    meta: { title: '采伐许可管理', requiresAuth: true }
  },
  {
    path: '/users',
    name: 'UserManagement',
    component: UserManagement,
    meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: Statistics,
    meta: { title: '统计报表', requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { title: '系统设置', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const loggedIn = isLoggedIn()
  const userIsAdmin = isAdmin()

  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    if (!loggedIn) {
      // 未登录，重定向到登录页面
      next({
        name: 'Login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 如果路由需要管理员权限
    if (to.meta.requiresAdmin && !userIsAdmin) {
      // 权限不足，重定向到概览页面
      next({ name: 'Overview' })
      return
    }
  }

  // 如果路由需要访客状态（如登录页面）
  if (to.meta.requiresGuest && loggedIn) {
    // 已登录用户访问登录页面，重定向到概览页面
    next({ name: 'Overview' })
    return
  }

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 森林资源管理系统`
  }

  next()
})

export default router