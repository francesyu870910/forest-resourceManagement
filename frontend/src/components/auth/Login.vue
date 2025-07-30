<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>森林资源管理系统</h2>
        <p>请登录您的账户</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
        
        <el-form-item class="forgot-password">
          <el-link type="primary" @click="showResetPassword">
            忘记密码？
          </el-link>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 密码重置对话框 -->
    <ResetPassword
      v-model:visible="resetPasswordVisible"
      @success="handleResetSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import ResetPassword from './ResetPassword.vue'
import { login } from '@/utils/auth'

const router = useRouter()
const updateUserInfo = inject('updateUserInfo', () => {})
const loginFormRef = ref()
const loading = ref(false)
const resetPasswordVisible = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    const result = await login(loginForm.username, loginForm.password)
    
    if (result.success) {
      ElMessage.success('登录成功')
      // 更新全局用户信息
      updateUserInfo()
      // 跳转到主页面
      router.push('/')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error('登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 显示密码重置对话框
const showResetPassword = () => {
  resetPasswordVisible.value = true
}

// 处理密码重置成功
const handleResetSuccess = () => {
  ElMessage.success('密码重置成功，请使用新密码登录')
  resetPasswordVisible.value = false
}

// 组件挂载时检查是否已登录
onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    // 如果已登录，直接跳转到主页面
    router.push('/')
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: 600;
}

.login-header p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.login-form {
  width: 100%;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.forgot-password {
  text-align: center;
  margin-bottom: 0;
}

.forgot-password :deep(.el-form-item__content) {
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    padding: 30px 20px;
    margin: 10px;
  }
  
  .login-header h2 {
    font-size: 20px;
  }
}
</style>