import { ElMessage, ElMessageBox, ElNotification, ElLoading } from 'element-plus'

/**
 * 消息提示工具类
 */
export class MessageUtil {
  /**
   * 成功消息
   */
  static success(message, duration = 3000) {
    return ElMessage({
      type: 'success',
      message,
      duration,
      showClose: true
    })
  }

  /**
   * 错误消息
   */
  static error(message, duration = 3000) {
    return ElMessage({
      type: 'error',
      message,
      duration,
      showClose: true
    })
  }

  /**
   * 警告消息
   */
  static warning(message, duration = 3000) {
    return ElMessage({
      type: 'warning',
      message,
      duration,
      showClose: true
    })
  }

  /**
   * 信息消息
   */
  static info(message, duration = 3000) {
    return ElMessage({
      type: 'info',
      message,
      duration,
      showClose: true
    })
  }

  /**
   * 确认对话框
   */
  static confirm(message, title = '确认操作', options = {}) {
    const defaultOptions = {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      ...options
    }

    return ElMessageBox.confirm(message, title, defaultOptions)
  }

  /**
   * 提示对话框
   */
  static alert(message, title = '提示', options = {}) {
    const defaultOptions = {
      confirmButtonText: '确定',
      type: 'info',
      ...options
    }

    return ElMessageBox.alert(message, title, defaultOptions)
  }

  /**
   * 输入对话框
   */
  static prompt(message, title = '输入', options = {}) {
    const defaultOptions = {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: null,
      inputErrorMessage: '输入格式不正确',
      ...options
    }

    return ElMessageBox.prompt(message, title, defaultOptions)
  }

  /**
   * 通知消息
   */
  static notify(options = {}) {
    const defaultOptions = {
      title: '通知',
      message: '',
      type: 'info',
      duration: 4500,
      position: 'top-right',
      ...options
    }

    return ElNotification(defaultOptions)
  }

  /**
   * 成功通知
   */
  static notifySuccess(title, message, duration = 4500) {
    return this.notify({
      title,
      message,
      type: 'success',
      duration
    })
  }

  /**
   * 错误通知
   */
  static notifyError(title, message, duration = 4500) {
    return this.notify({
      title,
      message,
      type: 'error',
      duration
    })
  }

  /**
   * 警告通知
   */
  static notifyWarning(title, message, duration = 4500) {
    return this.notify({
      title,
      message,
      type: 'warning',
      duration
    })
  }

  /**
   * 信息通知
   */
  static notifyInfo(title, message, duration = 4500) {
    return this.notify({
      title,
      message,
      type: 'info',
      duration
    })
  }
}

/**
 * 加载状态管理工具类
 */
export class LoadingUtil {
  static loadingInstance = null

  /**
   * 显示全屏加载
   */
  static showFullScreen(text = '加载中...', options = {}) {
    const defaultOptions = {
      lock: true,
      text,
      background: 'rgba(0, 0, 0, 0.7)',
      ...options
    }

    this.loadingInstance = ElLoading.service(defaultOptions)
    return this.loadingInstance
  }

  /**
   * 显示元素加载
   */
  static showElement(target, text = '加载中...', options = {}) {
    const defaultOptions = {
      target,
      lock: true,
      text,
      background: 'rgba(255, 255, 255, 0.9)',
      ...options
    }

    return ElLoading.service(defaultOptions)
  }

  /**
   * 隐藏加载
   */
  static hide() {
    if (this.loadingInstance) {
      this.loadingInstance.close()
      this.loadingInstance = null
    }
  }

  /**
   * 异步操作包装器
   */
  static async wrap(asyncFn, loadingText = '处理中...', options = {}) {
    const loading = this.showFullScreen(loadingText, options)
    
    try {
      const result = await asyncFn()
      return result
    } finally {
      loading.close()
    }
  }
}

// 导出默认实例
export const message = MessageUtil
export const loading = LoadingUtil

// 导出便捷方法
export const showMessage = MessageUtil
export const showLoading = LoadingUtil