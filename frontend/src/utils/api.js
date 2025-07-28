import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    return config
  },
  error => {
    // 对请求错误做些什么
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 对响应数据做点什么
    return response.data
  },
  error => {
    // 对响应错误做点什么
    console.error('响应错误:', error)
    
    let message = '请求失败'
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权访问'
          break
        case 403:
          message = '禁止访问'
          break
        case 404:
          message = '请求资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    } else if (error.request) {
      message = '网络连接失败'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// API方法
export const treeArchiveApi = {
  // 获取林木列表
  getList: (params) => api.get('/trees', { params }),
  // 获取林木详情
  getById: (id) => api.get(`/trees/${id}`),
  // 获取统计信息
  getStatistics: () => api.get('/trees/statistics'),
  // 新增林木档案
  create: (data) => api.post('/trees', data),
  // 更新林木档案
  update: (id, data) => api.put(`/trees/${id}`, data),
  // 删除林木档案
  delete: (id) => api.delete(`/trees/${id}`)
}

export const forestLandApi = {
  // 获取林地列表
  getList: (params) => api.get('/forestlands', { params }),
  // 获取林地统计
  getStats: () => api.get('/forestlands/stats'),
  // 新增林地信息
  create: (data) => api.post('/forestlands', data),
  // 更新林地信息
  update: (id, data) => api.put(`/forestlands/${id}`, data),
  // 删除林地信息
  delete: (id) => api.delete(`/forestlands/${id}`)
}

export const monitoringApi = {
  // 获取生长量数据
  getGrowthData: (params) => api.get('/monitoring/growth', { params }),
  // 获取蓄积量数据
  getVolumeData: (params) => api.get('/monitoring/volume', { params }),
  // 获取变化趋势
  getTrends: (params) => api.get('/monitoring/trends', { params }),
  // 获取预警信息
  getAlerts: () => api.get('/monitoring/alerts'),
  // 获取统计数据
  getStatistics: () => api.get('/monitoring/statistics'),
  // 获取林地选项
  getForestLands: () => api.get('/monitoring/forest-lands')
}

export const forestRightsApi = {
  // 获取证书列表
  getList: (params) => api.get('/rights', { params }),
  // 获取即将到期证书
  getExpiring: () => api.get('/rights/expiring'),
  // 获取林地选项
  getForestLands: () => api.get('/rights/forest-lands'),
  // 新增证书
  create: (data) => api.post('/rights', data),
  // 更新证书
  update: (id, data) => api.put(`/rights/${id}`, data),
  // 更新证书状态
  updateStatus: (id, status) => api.put(`/rights/${id}/status`, { status }),
  // 删除证书
  delete: (id) => api.delete(`/rights/${id}`),
  // 获取统计信息
  getStatistics: () => api.get('/rights/statistics')
}

export const cuttingPermitApi = {
  // 获取许可列表
  getList: (params) => api.get('/permits', { params }),
  // 新增许可申请
  create: (data) => api.post('/permits', data),
  // 审批许可
  approve: (id, data) => api.put(`/permits/${id}/approve`, data),
  // 获取采伐统计
  getStatistics: (params) => api.get('/permits/statistics', { params }),
  // 获取待审批许可
  getPending: () => api.get('/permits/pending'),
  // 获取即将到期许可
  getExpiring: () => api.get('/permits/expiring'),
  // 获取林地选项
  getForestLands: () => api.get('/permits/forest-lands'),
  // 删除许可
  delete: (id) => api.delete(`/permits/${id}`)
}

export const overviewApi = {
  // 获取概览统计
  getStatistics: () => api.get('/overview/statistics'),
  // 获取趋势分析
  getTrends: (params) => api.get('/overview/trends', { params }),
  // 获取预警信息
  getAlerts: () => api.get('/overview/alerts'),
  // 获取最近活动
  getRecentActivities: () => api.get('/overview/recent-activities'),
  // 获取系统状态
  getSystemStatus: () => api.get('/overview/system-status')
}

export default api