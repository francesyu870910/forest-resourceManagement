import axios from 'axios'

// API基础URL
const API_BASE_URL = 'http://localhost:8080/api'

// 创建axios实例
const authApi = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
authApi.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
authApi.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token过期或无效，清除本地存储并跳转到登录页
      clearAuth()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

/**
 * 用户登录
 */
export const login = async (username, password) => {
  try {
    const response = await authApi.post('/auth/login', {
      username,
      password
    })
    
    if (response.success && response.data) {
      // 保存认证信息到本地存储
      setToken(response.data.token)
      setUserInfo({
        username: response.data.username,
        role: response.data.role
      })
      return response
    }
    
    return response
  } catch (error) {
    console.error('登录请求失败:', error)
    return {
      success: false,
      message: error.response?.data?.message || '登录失败'
    }
  }
}

/**
 * 用户登出
 */
export const logout = async () => {
  try {
    await authApi.post('/auth/logout')
  } catch (error) {
    console.error('登出请求失败:', error)
  } finally {
    // 无论请求是否成功，都清除本地存储
    clearAuth()
  }
}

/**
 * 重置密码
 */
export const resetPassword = async (username, newPassword) => {
  try {
    const response = await authApi.post('/auth/reset-password', {
      username,
      newPassword
    })
    return response
  } catch (error) {
    console.error('密码重置请求失败:', error)
    return {
      success: false,
      message: error.response?.data?.message || '密码重置失败'
    }
  }
}

/**
 * 验证Token
 */
export const validateToken = async () => {
  try {
    const response = await authApi.post('/auth/validate')
    return response
  } catch (error) {
    console.error('Token验证失败:', error)
    return {
      success: false,
      message: 'Token验证失败'
    }
  }
}

/**
 * 获取当前用户信息
 */
export const getCurrentUser = async () => {
  try {
    const response = await authApi.get('/auth/me')
    return response
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return {
      success: false,
      message: '获取用户信息失败'
    }
  }
}

/**
 * 保存Token到本地存储
 */
export const setToken = (token) => {
  localStorage.setItem('token', token)
}

/**
 * 从本地存储获取Token
 */
export const getToken = () => {
  return localStorage.getItem('token')
}

/**
 * 保存用户信息到本地存储
 */
export const setUserInfo = (userInfo) => {
  localStorage.setItem('userInfo', JSON.stringify(userInfo))
}

/**
 * 从本地存储获取用户信息
 */
export const getUserInfo = () => {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 检查用户是否已登录
 */
export const isLoggedIn = () => {
  const token = getToken()
  const userInfo = getUserInfo()
  return !!(token && userInfo)
}

/**
 * 检查用户是否为管理员
 */
export const isAdmin = () => {
  const userInfo = getUserInfo()
  return userInfo?.role === 'ADMIN'
}

/**
 * 清除认证信息
 */
export const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
}

/**
 * 获取认证头
 */
export const getAuthHeader = () => {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export default authApi