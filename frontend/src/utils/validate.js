/**
 * 表单验证工具
 */

/**
 * 通用验证规则
 */
export const commonRules = {
  // 必填验证
  required: (message = '此项为必填项') => ({
    required: true,
    message,
    trigger: 'blur'
  }),

  // 邮箱验证
  email: (message = '请输入正确的邮箱地址') => ({
    type: 'email',
    message,
    trigger: 'blur'
  }),

  // 手机号验证
  phone: (message = '请输入正确的手机号') => ({
    pattern: /^1[3-9]\d{9}$/,
    message,
    trigger: 'blur'
  }),

  // 身份证验证
  idCard: (message = '请输入正确的身份证号') => ({
    pattern: /^\d{17}[\dXx]$/,
    message,
    trigger: 'blur'
  }),

  // 长度验证
  length: (min, max, message) => ({
    min,
    max,
    message: message || `长度在 ${min} 到 ${max} 个字符`,
    trigger: 'blur'
  }),

  // 最小长度验证
  minLength: (min, message) => ({
    min,
    message: message || `最少输入 ${min} 个字符`,
    trigger: 'blur'
  }),

  // 最大长度验证
  maxLength: (max, message) => ({
    max,
    message: message || `最多输入 ${max} 个字符`,
    trigger: 'blur'
  }),

  // 数字验证
  number: (message = '请输入数字') => ({
    type: 'number',
    message,
    trigger: 'blur'
  }),

  // 数字范围验证
  numberRange: (min, max, message) => ({
    type: 'number',
    min,
    max,
    message: message || `请输入 ${min} 到 ${max} 之间的数字`,
    trigger: 'blur'
  }),

  // 正整数验证
  positiveInteger: (message = '请输入正整数') => ({
    pattern: /^[1-9]\d*$/,
    message,
    trigger: 'blur'
  }),

  // 非负数验证
  nonNegative: (message = '请输入非负数') => ({
    pattern: /^\d+(\.\d+)?$/,
    message,
    trigger: 'blur'
  }),

  // URL验证
  url: (message = '请输入正确的URL地址') => ({
    type: 'url',
    message,
    trigger: 'blur'
  }),

  // 中文验证
  chinese: (message = '请输入中文') => ({
    pattern: /^[\u4e00-\u9fa5]+$/,
    message,
    trigger: 'blur'
  }),

  // 英文验证
  english: (message = '请输入英文') => ({
    pattern: /^[a-zA-Z]+$/,
    message,
    trigger: 'blur'
  }),

  // 英文数字验证
  alphanumeric: (message = '请输入英文或数字') => ({
    pattern: /^[a-zA-Z0-9]+$/,
    message,
    trigger: 'blur'
  })
}

/**
 * 业务相关验证规则
 */
export const businessRules = {
  // 树种名称验证
  treeSpecies: [
    commonRules.required('请输入树种名称'),
    commonRules.length(2, 20, '树种名称长度在2-20个字符')
  ],

  // 胸径验证
  diameter: [
    commonRules.required('请输入胸径'),
    commonRules.numberRange(5, 100, '胸径必须在5-100cm之间')
  ],

  // 树高验证
  height: [
    commonRules.required('请输入树高'),
    commonRules.numberRange(1, 50, '树高必须在1-50m之间')
  ],

  // 健康状态验证
  healthStatus: [
    commonRules.required('请选择健康状态')
  ],

  // 林地名称验证
  forestLandName: [
    commonRules.required('请输入林地名称'),
    commonRules.length(2, 50, '林地名称长度在2-50个字符')
  ],

  // 林地分类验证
  forestClassification: [
    commonRules.required('请选择林地分类')
  ],

  // 面积验证
  area: [
    commonRules.required('请输入面积'),
    commonRules.numberRange(0.1, 10000, '面积必须在0.1-10000公顷之间')
  ],

  // 证书编号验证
  certificateNo: [
    commonRules.required('请输入证书编号'),
    commonRules.length(6, 20, '证书编号长度在6-20个字符')
  ],

  // 权利人姓名验证
  ownerName: [
    commonRules.required('请输入权利人姓名'),
    commonRules.length(2, 20, '姓名长度在2-20个字符'),
    {
      pattern: /^[\u4e00-\u9fa5a-zA-Z]+$/,
      message: '姓名只能包含中文或英文',
      trigger: 'blur'
    }
  ],

  // 身份证号验证
  ownerIdCard: [
    commonRules.idCard()
  ],

  // 联系电话验证
  ownerPhone: [
    commonRules.phone()
  ],

  // 许可证编号验证
  permitNo: [
    commonRules.required('请输入许可证编号'),
    commonRules.length(6, 20, '许可证编号长度在6-20个字符')
  ],

  // 申请人姓名验证
  applicantName: [
    commonRules.required('请输入申请人姓名'),
    commonRules.length(2, 20, '姓名长度在2-20个字符'),
    {
      pattern: /^[\u4e00-\u9fa5a-zA-Z]+$/,
      message: '姓名只能包含中文或英文',
      trigger: 'blur'
    }
  ],

  // 采伐面积验证
  cuttingArea: [
    commonRules.required('请输入采伐面积'),
    commonRules.numberRange(0.1, 10000, '采伐面积必须在0.1-10000公顷之间')
  ],

  // 采伐量验证
  cuttingVolume: [
    commonRules.required('请输入采伐量'),
    commonRules.numberRange(1, 10000, '采伐量必须在1-10000立方米之间')
  ],

  // 申请理由验证
  reason: [
    commonRules.required('请输入申请理由'),
    commonRules.length(5, 500, '申请理由长度在5-500个字符')
  ],

  // 审批人验证
  approver: [
    commonRules.required('请输入审批人姓名'),
    commonRules.length(2, 20, '审批人姓名长度在2-20个字符')
  ],

  // 审批意见验证
  approvalOpinion: [
    commonRules.required('请输入审批意见'),
    commonRules.minLength(5, '审批意见至少5个字符')
  ]
}

/**
 * 自定义验证器
 */
export const customValidators = {
  // 日期范围验证
  dateRange: (startDate, endDate, message = '结束日期不能早于开始日期') => {
    return (rule, value, callback) => {
      if (startDate && endDate && new Date(endDate) < new Date(startDate)) {
        callback(new Error(message))
      } else {
        callback()
      }
    }
  },

  // 确认密码验证
  confirmPassword: (password, message = '两次输入的密码不一致') => {
    return (rule, value, callback) => {
      if (value !== password) {
        callback(new Error(message))
      } else {
        callback()
      }
    }
  },

  // 唯一性验证
  unique: (list, key, currentId = null, message = '该值已存在') => {
    return (rule, value, callback) => {
      const exists = list.some(item => 
        item[key] === value && (currentId === null || item.id !== currentId)
      )
      if (exists) {
        callback(new Error(message))
      } else {
        callback()
      }
    }
  },

  // 文件大小验证
  fileSize: (maxSize, message) => {
    return (rule, value, callback) => {
      if (value && value.size > maxSize) {
        callback(new Error(message || `文件大小不能超过 ${maxSize / 1024 / 1024}MB`))
      } else {
        callback()
      }
    }
  },

  // 文件类型验证
  fileType: (allowedTypes, message) => {
    return (rule, value, callback) => {
      if (value && !allowedTypes.includes(value.type)) {
        callback(new Error(message || `只允许上传 ${allowedTypes.join(', ')} 类型的文件`))
      } else {
        callback()
      }
    }
  }
}

/**
 * 表单验证工具类
 */
export class FormValidator {
  /**
   * 验证单个字段
   */
  static validateField(value, rules) {
    for (const rule of rules) {
      if (rule.required && (value === null || value === undefined || value === '')) {
        return { valid: false, message: rule.message || '此项为必填项' }
      }

      if (rule.pattern && value && !rule.pattern.test(value)) {
        return { valid: false, message: rule.message || '格式不正确' }
      }

      if (rule.min && value && value.length < rule.min) {
        return { valid: false, message: rule.message || `最少输入 ${rule.min} 个字符` }
      }

      if (rule.max && value && value.length > rule.max) {
        return { valid: false, message: rule.message || `最多输入 ${rule.max} 个字符` }
      }

      if (rule.type === 'number' && value && isNaN(Number(value))) {
        return { valid: false, message: rule.message || '请输入数字' }
      }

      if (rule.type === 'number' && value) {
        const num = Number(value)
        if (rule.min !== undefined && num < rule.min) {
          return { valid: false, message: rule.message || `数值不能小于 ${rule.min}` }
        }
        if (rule.max !== undefined && num > rule.max) {
          return { valid: false, message: rule.message || `数值不能大于 ${rule.max}` }
        }
      }
    }

    return { valid: true, message: '' }
  }

  /**
   * 验证整个表单
   */
  static validateForm(formData, rulesConfig) {
    const errors = {}
    let isValid = true

    Object.keys(rulesConfig).forEach(field => {
      const rules = rulesConfig[field]
      const value = formData[field]
      const result = this.validateField(value, rules)

      if (!result.valid) {
        errors[field] = result.message
        isValid = false
      }
    })

    return { valid: isValid, errors }
  }
}

// 导出默认验证规则
export default {
  commonRules,
  businessRules,
  customValidators,
  FormValidator
}