import axios from 'axios'
import { getAuthHeader } from './auth'

// API基础URL
const API_BASE_URL = 'http://localhost:8080/api'

// 创建axios实例
const userApiInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
userApiInstance.interceptors.request.use(
  (config) => {
    const authHeader = getAuthHeader()
    if (authHeader.Authorization) {
      config.headers.Authorization = authHeader.Authorization
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
userApiInstance.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token过期或无效，跳转到登录页
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

/**
 * 用户管理API
 */
export const userApi = {
  /**
   * 获取所有用户
   */
  getAllUsers: async () => {
    try {
      const response = await userApiInstance.get('/users')
      return response
    } catch (error) {
      console.error('获取用户列表失败:', error)
      return {
        success: false,
        message: error.response?.data?.message || '获取用户列表失败'
      }
    }
  },

  /**
   * 根据ID获取用户
   */
  getUserById: async (id) => {
    try {
      const response = await userApiInstance.get(`/users/${id}`)
      return response
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return {
        success: false,
        message: error.response?.data?.message || '获取用户信息失败'
      }
    }
  },

  /**
   * 创建新用户
   */
  createUser: async (userData) => {
    try {
      const response = await userApiInstance.post('/users', userData)
      return response
    } catch (error) {
      console.error('创建用户失败:', error)
      return {
        success: false,
        message: error.response?.data?.message || '创建用户失败'
      }
    }
  },

  /**
   * 更新用户信息
   */
  updateUser: async (id, userData) => {
    try {
      const response = await userApiInstance.put(`/users/${id}`, userData)
      return response
    } catch (error) {
      console.error('更新用户失败:', error)
      return {
        success: false,
        message: error.response?.data?.message || '更新用户失败'
      }
    }
  },

  /**
   * 删除用户
   */
  deleteUser: async (id) => {
    try {
      const response = await userApiInstance.delete(`/users/${id}`)
      return response
    } catch (error) {
      console.error('删除用户失败:', error)
      return {
        success: false,
        message: error.response?.data?.message || '删除用户失败'
      }
    }
  },

  /**
   * 重置用户密码
   */
  resetUserPassword: async (username, newPassword) => {
    try {
      const response = await userApiInstance.post('/auth/reset-password', {
        username,
        newPassword
      })
      return response
    } catch (error) {
      console.error('重置密码失败:', error)
      return {
        success: false,
        message: error.response?.data?.message || '重置密码失败'
      }
    }
  },

  /**
   * 启用/禁用用户
   */
  toggleUserStatus: async (id, enabled) => {
    try {
      const response = await userApiInstance.put(`/users/${id}`, { enabled })
      return response
    } catch (error) {
      console.error('更新用户状态失败:', error)
      return {
        success: false,
        message: error.response?.data?.message || '更新用户状态失败'
      }
    }
  }
}

export default userApi