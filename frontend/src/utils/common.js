/**
 * 通用工具函数
 */

/**
 * 日期格式化
 */
export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 日期时间格式化
 */
export function formatDateTime(date) {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

/**
 * 数字格式化
 */
export function formatNumber(num, decimals = 2) {
  if (num === null || num === undefined || isNaN(num)) return '0'
  
  return Number(num).toFixed(decimals)
}

/**
 * 文件大小格式化
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 防抖函数
 */
export function debounce(func, wait, immediate = false) {
  let timeout
  
  return function executedFunction(...args) {
    const later = () => {
      timeout = null
      if (!immediate) func.apply(this, args)
    }
    
    const callNow = immediate && !timeout
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    
    if (callNow) func.apply(this, args)
  }
}

/**
 * 节流函数
 */
export function throttle(func, limit) {
  let inThrottle
  
  return function executedFunction(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 深拷贝
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime())
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  if (typeof obj === 'object') {
    const clonedObj = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
}

/**
 * 生成唯一ID
 */
export function generateId(prefix = 'id') {
  return prefix + '_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now()
}

/**
 * 获取URL参数
 */
export function getUrlParams(url = window.location.href) {
  const params = {}
  const urlObj = new URL(url)
  
  for (const [key, value] of urlObj.searchParams) {
    params[key] = value
  }
  
  return params
}

/**
 * 设置URL参数
 */
export function setUrlParams(params, url = window.location.href) {
  const urlObj = new URL(url)
  
  Object.keys(params).forEach(key => {
    if (params[key] !== null && params[key] !== undefined) {
      urlObj.searchParams.set(key, params[key])
    } else {
      urlObj.searchParams.delete(key)
    }
  })
  
  return urlObj.toString()
}

/**
 * 下载文件
 */
export function downloadFile(data, filename, type = 'application/octet-stream') {
  const blob = new Blob([data], { type })
  const url = URL.createObjectURL(blob)
  
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  URL.revokeObjectURL(url)
}

/**
 * 导出JSON数据
 */
export function exportJson(data, filename = 'data.json') {
  const jsonStr = JSON.stringify(data, null, 2)
  downloadFile(jsonStr, filename, 'application/json')
}

/**
 * 导出CSV数据
 */
export function exportCsv(data, filename = 'data.csv', headers = []) {
  if (!Array.isArray(data) || data.length === 0) return
  
  let csvContent = ''
  
  // 添加表头
  if (headers.length > 0) {
    csvContent += headers.join(',') + '\n'
  } else if (typeof data[0] === 'object') {
    csvContent += Object.keys(data[0]).join(',') + '\n'
  }
  
  // 添加数据行
  data.forEach(row => {
    if (typeof row === 'object') {
      csvContent += Object.values(row).map(value => 
        typeof value === 'string' ? `"${value}"` : value
      ).join(',') + '\n'
    } else {
      csvContent += row + '\n'
    }
  })
  
  downloadFile(csvContent, filename, 'text/csv')
}

/**
 * 验证邮箱格式
 */
export function validateEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

/**
 * 验证手机号格式
 */
export function validatePhone(phone) {
  const re = /^1[3-9]\d{9}$/
  return re.test(phone)
}

/**
 * 验证身份证号格式
 */
export function validateIdCard(idCard) {
  const re = /^\d{17}[\dXx]$/
  return re.test(idCard)
}

/**
 * 获取文件扩展名
 */
export function getFileExtension(filename) {
  return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2)
}

/**
 * 判断是否为空值
 */
export function isEmpty(value) {
  return value === null || 
         value === undefined || 
         value === '' || 
         (Array.isArray(value) && value.length === 0) ||
         (typeof value === 'object' && Object.keys(value).length === 0)
}

/**
 * 数组去重
 */
export function uniqueArray(arr, key = null) {
  if (!Array.isArray(arr)) return []
  
  if (key) {
    const seen = new Set()
    return arr.filter(item => {
      const val = item[key]
      if (seen.has(val)) {
        return false
      }
      seen.add(val)
      return true
    })
  }
  
  return [...new Set(arr)]
}

/**
 * 对象数组排序
 */
export function sortArray(arr, key, order = 'asc') {
  if (!Array.isArray(arr)) return []
  
  return arr.sort((a, b) => {
    const aVal = a[key]
    const bVal = b[key]
    
    if (order === 'desc') {
      return bVal > aVal ? 1 : bVal < aVal ? -1 : 0
    } else {
      return aVal > bVal ? 1 : aVal < bVal ? -1 : 0
    }
  })
}

/**
 * 树形数据扁平化
 */
export function flattenTree(tree, childrenKey = 'children') {
  const result = []
  
  function traverse(nodes) {
    nodes.forEach(node => {
      result.push(node)
      if (node[childrenKey] && node[childrenKey].length > 0) {
        traverse(node[childrenKey])
      }
    })
  }
  
  traverse(tree)
  return result
}

/**
 * 扁平数据转树形
 */
export function arrayToTree(arr, idKey = 'id', parentKey = 'parentId', childrenKey = 'children') {
  const result = []
  const map = {}
  
  // 创建映射
  arr.forEach(item => {
    map[item[idKey]] = { ...item, [childrenKey]: [] }
  })
  
  // 构建树形结构
  arr.forEach(item => {
    const parent = map[item[parentKey]]
    if (parent) {
      parent[childrenKey].push(map[item[idKey]])
    } else {
      result.push(map[item[idKey]])
    }
  })
  
  return result
}