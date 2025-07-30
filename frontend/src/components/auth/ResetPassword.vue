<template>
  <el-dialog
    v-model="dialogVisible"
    title="重置密码"
    width="400px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="resetFormRef"
      :model="resetForm"
      :rules="resetRules"
      label-width="80px"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="resetForm.username"
          placeholder="请输入用户名"
          clearable
        />
      </el-form-item>
      
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="resetForm.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
          clearable
        />
      </el-form-item>
      
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="resetForm.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
          clearable
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :loading="loading"
          @click="handleResetPassword"
        >
          {{ loading ? '重置中...' : '确认重置' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { resetPassword } from '@/utils/auth'

// 定义props和emits
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'success'])

// 响应式数据
const resetFormRef = ref()
const loading = ref(false)

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 重置表单数据
const resetForm = reactive({
  username: '',
  newPassword: '',
  confirmPassword: ''
})

// 确认密码验证器
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== resetForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const resetRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 处理密码重置
const handleResetPassword = async () => {
  if (!resetFormRef.value) return
  
  try {
    const valid = await resetFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    const result = await resetPassword(resetForm.username, resetForm.newPassword)
    
    if (result.success) {
      ElMessage.success('密码重置成功')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(result.message || '密码重置失败')
    }
  } catch (error) {
    console.error('密码重置错误:', error)
    ElMessage.error('密码重置失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 处理对话框关闭
const handleClose = () => {
  dialogVisible.value = false
  // 重置表单
  if (resetFormRef.value) {
    resetFormRef.value.resetFields()
  }
  resetForm.username = ''
  resetForm.newPassword = ''
  resetForm.confirmPassword = ''
}

// 监听对话框显示状态，重置表单
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 对话框打开时重置表单
    if (resetFormRef.value) {
      resetFormRef.value.resetFields()
    }
  }
})
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
}
</style>