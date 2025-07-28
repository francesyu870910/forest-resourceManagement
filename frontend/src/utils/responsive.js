/**
 * 响应式设计工具
 */

import { ref, onMounted, onUnmounted } from 'vue'

// 断点定义
export const breakpoints = {
  xs: 480,   // 超小屏幕
  sm: 768,   // 小屏幕
  md: 992,   // 中等屏幕
  lg: 1200,  // 大屏幕
  xl: 1920   // 超大屏幕
}

/**
 * 获取当前屏幕尺寸类型
 */
export function getScreenSize(width = window.innerWidth) {
  if (width < breakpoints.xs) return 'xs'
  if (width < breakpoints.sm) return 'sm'
  if (width < breakpoints.md) return 'md'
  if (width < breakpoints.lg) return 'lg'
  return 'xl'
}

/**
 * 判断是否为移动设备
 */
export function isMobile(width = window.innerWidth) {
  return width < breakpoints.md
}

/**
 * 判断是否为平板设备
 */
export function isTablet(width = window.innerWidth) {
  return width >= breakpoints.sm && width < breakpoints.lg
}

/**
 * 判断是否为桌面设备
 */
export function isDesktop(width = window.innerWidth) {
  return width >= breakpoints.lg
}

/**
 * 响应式Hook
 */
export function useResponsive() {
  const screenWidth = ref(window.innerWidth)
  const screenHeight = ref(window.innerHeight)
  const screenSize = ref(getScreenSize())
  
  const updateSize = () => {
    screenWidth.value = window.innerWidth
    screenHeight.value = window.innerHeight
    screenSize.value = getScreenSize()
  }
  
  onMounted(() => {
    window.addEventListener('resize', updateSize)
    updateSize()
  })
  
  onUnmounted(() => {
    window.removeEventListener('resize', updateSize)
  })
  
  return {
    screenWidth,
    screenHeight,
    screenSize,
    isMobile: () => isMobile(screenWidth.value),
    isTablet: () => isTablet(screenWidth.value),
    isDesktop: () => isDesktop(screenWidth.value)
  }
}

/**
 * 响应式栅格配置
 */
export function getResponsiveGrid(config) {
  const { screenSize } = useResponsive()
  
  const defaultConfig = {
    xs: { span: 24, gutter: 10 },
    sm: { span: 12, gutter: 15 },
    md: { span: 8, gutter: 20 },
    lg: { span: 6, gutter: 20 },
    xl: { span: 6, gutter: 20 }
  }
  
  const mergedConfig = { ...defaultConfig, ...config }
  
  return mergedConfig[screenSize.value] || mergedConfig.md
}

/**
 * 响应式表格配置
 */
export function getResponsiveTableConfig() {
  const { isMobile, isTablet } = useResponsive()
  
  if (isMobile()) {
    return {
      size: 'small',
      showHeader: true,
      stripe: true,
      border: false,
      fit: true,
      highlightCurrentRow: true
    }
  }
  
  if (isTablet()) {
    return {
      size: 'default',
      showHeader: true,
      stripe: true,
      border: true,
      fit: true,
      highlightCurrentRow: true
    }
  }
  
  return {
    size: 'default',
    showHeader: true,
    stripe: true,
    border: true,
    fit: false,
    highlightCurrentRow: true
  }
}

/**
 * 响应式分页配置
 */
export function getResponsivePaginationConfig() {
  const { isMobile, isTablet } = useResponsive()
  
  if (isMobile()) {
    return {
      layout: 'prev, pager, next',
      pageSizes: [5, 10, 20],
      pageSize: 5,
      size: 'small'
    }
  }
  
  if (isTablet()) {
    return {
      layout: 'total, prev, pager, next',
      pageSizes: [10, 20, 50],
      pageSize: 10,
      size: 'default'
    }
  }
  
  return {
    layout: 'total, sizes, prev, pager, next, jumper',
    pageSizes: [10, 20, 50, 100],
    pageSize: 10,
    size: 'default'
  }
}

/**
 * 响应式对话框配置
 */
export function getResponsiveDialogConfig(baseWidth = '600px') {
  const { isMobile, isTablet } = useResponsive()
  
  if (isMobile()) {
    return {
      width: '95%',
      fullscreen: false,
      top: '5vh',
      destroyOnClose: true
    }
  }
  
  if (isTablet()) {
    return {
      width: '80%',
      fullscreen: false,
      top: '10vh',
      destroyOnClose: true
    }
  }
  
  return {
    width: baseWidth,
    fullscreen: false,
    top: '15vh',
    destroyOnClose: true
  }
}

/**
 * 响应式表单配置
 */
export function getResponsiveFormConfig() {
  const { isMobile, isTablet } = useResponsive()
  
  if (isMobile()) {
    return {
      labelPosition: 'top',
      labelWidth: 'auto',
      size: 'default'
    }
  }
  
  if (isTablet()) {
    return {
      labelPosition: 'right',
      labelWidth: '100px',
      size: 'default'
    }
  }
  
  return {
    labelPosition: 'right',
    labelWidth: '120px',
    size: 'default'
  }
}

/**
 * 响应式卡片配置
 */
export function getResponsiveCardConfig() {
  const { isMobile } = useResponsive()
  
  if (isMobile()) {
    return {
      shadow: 'never',
      bodyStyle: { padding: '15px' }
    }
  }
  
  return {
    shadow: 'hover',
    bodyStyle: { padding: '20px' }
  }
}

/**
 * 响应式导航配置
 */
export function getResponsiveNavConfig() {
  const { isMobile, isTablet } = useResponsive()
  
  if (isMobile()) {
    return {
      mode: 'vertical',
      collapse: true,
      collapseTransition: true
    }
  }
  
  if (isTablet()) {
    return {
      mode: 'horizontal',
      collapse: false,
      collapseTransition: false
    }
  }
  
  return {
    mode: 'horizontal',
    collapse: false,
    collapseTransition: false
  }
}

/**
 * 获取响应式类名
 */
export function getResponsiveClass(baseClass = '') {
  const { screenSize } = useResponsive()
  
  const classes = [baseClass]
  
  if (baseClass) {
    classes.push(`${baseClass}--${screenSize.value}`)
  }
  
  return classes.filter(Boolean).join(' ')
}

/**
 * 响应式样式混入
 */
export const responsiveMixin = {
  data() {
    return {
      screenWidth: window.innerWidth,
      screenHeight: window.innerHeight
    }
  },
  
  computed: {
    screenSize() {
      return getScreenSize(this.screenWidth)
    },
    
    isMobile() {
      return isMobile(this.screenWidth)
    },
    
    isTablet() {
      return isTablet(this.screenWidth)
    },
    
    isDesktop() {
      return isDesktop(this.screenWidth)
    }
  },
  
  mounted() {
    window.addEventListener('resize', this.handleResize)
  },
  
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize)
  },
  
  methods: {
    handleResize() {
      this.screenWidth = window.innerWidth
      this.screenHeight = window.innerHeight
    }
  }
}

export default {
  breakpoints,
  getScreenSize,
  isMobile,
  isTablet,
  isDesktop,
  useResponsive,
  getResponsiveGrid,
  getResponsiveTableConfig,
  getResponsivePaginationConfig,
  getResponsiveDialogConfig,
  getResponsiveFormConfig,
  getResponsiveCardConfig,
  getResponsiveNavConfig,
  getResponsiveClass,
  responsiveMixin
}