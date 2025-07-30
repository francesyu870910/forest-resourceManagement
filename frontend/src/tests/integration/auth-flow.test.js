import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'
import * as auth from '../../utils/auth.js'
import * as api from '../../utils/api.js'

// Mock axios
vi.mock('axios')
const mockedAxios = vi.mocked(axios)

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})

describe('Authentication Flow Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorageMock.getItem.mockReturnValue(null)
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('Complete Login Flow', () => {
    it('should handle successful admin login', async () => {
      // Mock successful login response
      const mockLoginResponse = {
        data: {
          success: true,
          data: {
            token: 'mock-admin-token',
            username: 'admin',
            role: 'ADMIN',
            expiresIn: 3600
          }
        }
      }

      mockedAxios.post.mockResolvedValueOnce(mockLoginResponse)

      // Test login
      const credentials = { username: 'admin', password: 'admin123' }
      const response = await api.login(credentials)

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/login', credentials)
      expect(response.success).toBe(true)
      expect(response.data.username).toBe('admin')
      expect(response.data.role).toBe('ADMIN')

      // Verify token storage
      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'mock-admin-token')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('userInfo', JSON.stringify({
        username: 'admin',
        role: 'ADMIN'
      }))
    })

    it('should handle successful user login', async () => {
      const mockLoginResponse = {
        data: {
          success: true,
          data: {
            token: 'mock-user-token',
            username: 'user',
            role: 'USER',
            expiresIn: 3600
          }
        }
      }

      mockedAxios.post.mockResolvedValueOnce(mockLoginResponse)

      const credentials = { username: 'user', password: 'user123' }
      const response = await api.login(credentials)

      expect(response.data.role).toBe('USER')
    })

    it('should handle demo users login', async () => {
      const demoUsers = [
        { username: '张林', role: 'USER' },
        { username: '李森', role: 'USER' },
        { username: '王树', role: 'ADMIN' },
        { username: '赵木', role: 'USER' },
        { username: '钱林业', role: 'USER' }
      ]

      for (const user of demoUsers) {
        const mockResponse = {
          data: {
            success: true,
            data: {
              token: `mock-${user.username}-token`,
              username: user.username,
              role: user.role,
              expiresIn: 3600
            }
          }
        }

        mockedAxios.post.mockResolvedValueOnce(mockResponse)

        const credentials = { username: user.username, password: 'user123' }
        const response = await api.login(credentials)

        expect(response.data.username).toBe(user.username)
        expect(response.data.role).toBe(user.role)
      }
    })

    it('should handle login failure', async () => {
      const mockErrorResponse = {
        response: {
          data: {
            success: false,
            message: '登录失败: 用户名或密码错误'
          }
        }
      }

      mockedAxios.post.mockRejectedValueOnce(mockErrorResponse)

      const credentials = { username: 'admin', password: 'wrongpassword' }
      
      try {
        await api.login(credentials)
        expect.fail('Should have thrown an error')
      } catch (error) {
        expect(error.response.data.message).toBe('登录失败: 用户名或密码错误')
      }
    })
  })

  describe('Password Reset Flow', () => {
    it('should handle successful password reset', async () => {
      const mockResetResponse = {
        data: {
          success: true,
          message: '密码重置成功'
        }
      }

      mockedAxios.post.mockResolvedValueOnce(mockResetResponse)

      const resetData = { username: 'user', newPassword: 'newpassword123' }
      const response = await api.resetPassword(resetData)

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/reset-password', resetData)
      expect(response.success).toBe(true)
      expect(response.message).toBe('密码重置成功')
    })

    it('should handle password reset for non-existent user', async () => {
      const mockErrorResponse = {
        response: {
          data: {
            success: false,
            message: '密码重置失败: 用户不存在'
          }
        }
      }

      mockedAxios.post.mockRejectedValueOnce(mockErrorResponse)

      const resetData = { username: 'nonexistent', newPassword: 'newpassword123' }
      
      try {
        await api.resetPassword(resetData)
        expect.fail('Should have thrown an error')
      } catch (error) {
        expect(error.response.data.message).toBe('密码重置失败: 用户不存在')
      }
    })
  })

  describe('User Management Flow', () => {
    beforeEach(() => {
      // Mock admin token
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'token') return 'mock-admin-token'
        if (key === 'userInfo') return JSON.stringify({ username: 'admin', role: 'ADMIN' })
        return null
      })
    })

    it('should get user list for admin', async () => {
      const mockUsersResponse = {
        data: {
          success: true,
          data: [
            { id: 1, username: 'admin', role: 'ADMIN', enabled: true },
            { id: 2, username: 'user', role: 'USER', enabled: true },
            { id: 3, username: '张林', role: 'USER', enabled: true },
            { id: 4, username: '李森', role: 'USER', enabled: true },
            { id: 5, username: '王树', role: 'ADMIN', enabled: true },
            { id: 6, username: '赵木', role: 'USER', enabled: true },
            { id: 7, username: '钱林业', role: 'USER', enabled: true }
          ]
        }
      }

      mockedAxios.get.mockResolvedValueOnce(mockUsersResponse)

      const response = await api.getUsers()

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/users', {
        headers: { Authorization: 'Bearer mock-admin-token' }
      })
      expect(response.success).toBe(true)
      expect(response.data).toHaveLength(7)
      expect(response.data.some(user => user.username === 'admin')).toBe(true)
      expect(response.data.some(user => user.username === '张林')).toBe(true)
    })

    it('should create new user', async () => {
      const newUser = { username: '测试用户', password: 'testpassword123', role: 'USER' }
      const mockCreateResponse = {
        data: {
          success: true,
          data: {
            id: 8,
            username: '测试用户',
            role: 'USER',
            enabled: true,
            createdAt: '2024-01-15T10:30:00'
          }
        }
      }

      mockedAxios.post.mockResolvedValueOnce(mockCreateResponse)

      const response = await api.createUser(newUser)

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/users', newUser, {
        headers: { Authorization: 'Bearer mock-admin-token' }
      })
      expect(response.success).toBe(true)
      expect(response.data.username).toBe('测试用户')
    })

    it('should update user', async () => {
      const updatedUser = { id: 8, username: '测试用户', role: 'ADMIN' }
      const mockUpdateResponse = {
        data: {
          success: true,
          data: {
            id: 8,
            username: '测试用户',
            role: 'ADMIN',
            enabled: true
          }
        }
      }

      mockedAxios.put.mockResolvedValueOnce(mockUpdateResponse)

      const response = await api.updateUser(8, updatedUser)

      expect(mockedAxios.put).toHaveBeenCalledWith('/api/users/8', updatedUser, {
        headers: { Authorization: 'Bearer mock-admin-token' }
      })
      expect(response.success).toBe(true)
      expect(response.data.role).toBe('ADMIN')
    })

    it('should delete user', async () => {
      const mockDeleteResponse = {
        data: {
          success: true,
          message: '用户删除成功'
        }
      }

      mockedAxios.delete.mockResolvedValueOnce(mockDeleteResponse)

      const response = await api.deleteUser(8)

      expect(mockedAxios.delete).toHaveBeenCalledWith('/api/users/8', {
        headers: { Authorization: 'Bearer mock-admin-token' }
      })
      expect(response.success).toBe(true)
    })

    it('should handle permission denied for regular user', async () => {
      // Mock regular user token
      localStorageMock.getItem.mockImplementation((key) => {
        if (key === 'token') return 'mock-user-token'
        if (key === 'userInfo') return JSON.stringify({ username: 'user', role: 'USER' })
        return null
      })

      const mockErrorResponse = {
        response: {
          status: 403,
          data: {
            success: false,
            message: '权限不足'
          }
        }
      }

      mockedAxios.get.mockRejectedValueOnce(mockErrorResponse)

      try {
        await api.getUsers()
        expect.fail('Should have thrown an error')
      } catch (error) {
        expect(error.response.status).toBe(403)
        expect(error.response.data.message).toBe('权限不足')
      }
    })
  })

  describe('Token Validation and Management', () => {
    it('should validate token', async () => {
      const mockValidateResponse = {
        data: {
          success: true,
          data: {
            valid: true,
            username: 'admin',
            role: 'ADMIN'
          }
        }
      }

      mockedAxios.post.mockResolvedValueOnce(mockValidateResponse)
      localStorageMock.getItem.mockReturnValue('mock-admin-token')

      const response = await api.validateToken()

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/validate', {}, {
        headers: { Authorization: 'Bearer mock-admin-token' }
      })
      expect(response.success).toBe(true)
      expect(response.data.valid).toBe(true)
    })

    it('should handle invalid token', async () => {
      const mockErrorResponse = {
        response: {
          status: 401,
          data: {
            success: false,
            message: 'Token无效'
          }
        }
      }

      mockedAxios.post.mockRejectedValueOnce(mockErrorResponse)
      localStorageMock.getItem.mockReturnValue('invalid-token')

      try {
        await api.validateToken()
        expect.fail('Should have thrown an error')
      } catch (error) {
        expect(error.response.status).toBe(401)
      }
    })

    it('should get current user info', async () => {
      const mockUserResponse = {
        data: {
          success: true,
          data: {
            username: 'admin',
            role: 'ADMIN',
            enabled: true,
            createdAt: '2024-01-01T00:00:00'
          }
        }
      }

      mockedAxios.get.mockResolvedValueOnce(mockUserResponse)
      localStorageMock.getItem.mockReturnValue('mock-admin-token')

      const response = await api.getCurrentUser()

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/auth/me', {
        headers: { Authorization: 'Bearer mock-admin-token' }
      })
      expect(response.success).toBe(true)
      expect(response.data.username).toBe('admin')
    })

    it('should handle logout', async () => {
      const mockLogoutResponse = {
        data: {
          success: true,
          message: '登出成功'
        }
      }

      mockedAxios.post.mockResolvedValueOnce(mockLogoutResponse)
      localStorageMock.getItem.mockReturnValue('mock-admin-token')

      const response = await api.logout()

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/auth/logout', {}, {
        headers: { Authorization: 'Bearer mock-admin-token' }
      })
      expect(response.success).toBe(true)
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('userInfo')
    })
  })

  describe('Error Handling and Validation', () => {
    it('should handle network errors', async () => {
      const networkError = new Error('Network Error')
      mockedAxios.post.mockRejectedValueOnce(networkError)

      try {
        await api.login({ username: 'admin', password: 'admin123' })
        expect.fail('Should have thrown an error')
      } catch (error) {
        expect(error.message).toBe('Network Error')
      }
    })

    it('should handle validation errors', async () => {
      const mockValidationError = {
        response: {
          status: 400,
          data: {
            success: false,
            message: '用户名不能为空'
          }
        }
      }

      mockedAxios.post.mockRejectedValueOnce(mockValidationError)

      try {
        await api.login({ username: '', password: 'password' })
        expect.fail('Should have thrown an error')
      } catch (error) {
        expect(error.response.status).toBe(400)
        expect(error.response.data.message).toBe('用户名不能为空')
      }
    })

    it('should handle server errors', async () => {
      const mockServerError = {
        response: {
          status: 500,
          data: {
            success: false,
            message: '服务器内部错误'
          }
        }
      }

      mockedAxios.post.mockRejectedValueOnce(mockServerError)

      try {
        await api.login({ username: 'admin', password: 'admin123' })
        expect.fail('Should have thrown an error')
      } catch (error) {
        expect(error.response.status).toBe(500)
      }
    })
  })

  describe('Demo Data Verification', () => {
    it('should verify all demo users exist', async () => {
      const expectedDemoUsers = [
        { username: 'admin', role: 'ADMIN' },
        { username: 'user', role: 'USER' },
        { username: '张林', role: 'USER' },
        { username: '李森', role: 'USER' },
        { username: '王树', role: 'ADMIN' },
        { username: '赵木', role: 'USER' },
        { username: '钱林业', role: 'USER' }
      ]

      const mockUsersResponse = {
        data: {
          success: true,
          data: expectedDemoUsers.map((user, index) => ({
            id: index + 1,
            ...user,
            enabled: true,
            createdAt: '2024-01-01T00:00:00'
          }))
        }
      }

      mockedAxios.get.mockResolvedValueOnce(mockUsersResponse)
      localStorageMock.getItem.mockReturnValue('mock-admin-token')

      const response = await api.getUsers()

      expect(response.success).toBe(true)
      expect(response.data).toHaveLength(expectedDemoUsers.length)

      for (const expectedUser of expectedDemoUsers) {
        const foundUser = response.data.find(user => user.username === expectedUser.username)
        expect(foundUser).toBeDefined()
        expect(foundUser.role).toBe(expectedUser.role)
        expect(foundUser.enabled).toBe(true)
      }
    })

    it('should verify demo users can login with correct passwords', async () => {
      const demoCredentials = [
        { username: 'admin', password: 'admin123' },
        { username: 'user', password: 'user123' },
        { username: '张林', password: 'user123' },
        { username: '李森', password: 'user123' },
        { username: '王树', password: 'user123' },
        { username: '赵木', password: 'user123' },
        { username: '钱林业', password: 'user123' }
      ]

      for (const credentials of demoCredentials) {
        const mockResponse = {
          data: {
            success: true,
            data: {
              token: `mock-${credentials.username}-token`,
              username: credentials.username,
              role: credentials.username === 'admin' || credentials.username === '王树' ? 'ADMIN' : 'USER',
              expiresIn: 3600
            }
          }
        }

        mockedAxios.post.mockResolvedValueOnce(mockResponse)

        const response = await api.login(credentials)
        expect(response.success).toBe(true)
        expect(response.data.username).toBe(credentials.username)
      }
    })
  })
})