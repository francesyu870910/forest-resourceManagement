<template>
  <div class="help">
    <div class="page-header">
      <h2>帮助文档</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索帮助内容"
          style="width: 300px"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="6">
        <!-- 导航菜单 -->
        <el-card class="nav-card">
          <template #header>
            <span>帮助目录</span>
          </template>
          <el-menu
            :default-active="activeSection"
            @select="handleSectionChange"
            class="help-menu"
          >
            <el-menu-item index="overview">
              <el-icon><InfoFilled /></el-icon>
              <span>系统概述</span>
            </el-menu-item>
            <el-menu-item index="getting-started">
              <el-icon><VideoPlay /></el-icon>
              <span>快速入门</span>
            </el-menu-item>
            <el-menu-item index="tree-archive">
              <el-icon><Document /></el-icon>
              <span>林木档案管理</span>
            </el-menu-item>
            <el-menu-item index="forest-classify">
              <el-icon><MapLocation /></el-icon>
              <span>林地分类管理</span>
            </el-menu-item>
            <el-menu-item index="resource-monitor">
              <el-icon><TrendCharts /></el-icon>
              <span>资源动态监测</span>
            </el-menu-item>
            <el-menu-item index="forest-rights">
              <el-icon><Notebook /></el-icon>
              <span>林权证书管理</span>
            </el-menu-item>
            <el-menu-item index="cutting-permit">
              <el-icon><EditPen /></el-icon>
              <span>采伐许可管理</span>
            </el-menu-item>
            <el-menu-item index="statistics">
              <el-icon><PieChart /></el-icon>
              <span>统计报表</span>
            </el-menu-item>
            <el-menu-item index="faq">
              <el-icon><QuestionFilled /></el-icon>
              <span>常见问题</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <el-col :span="18">
        <!-- 帮助内容 -->
        <el-card class="content-card">
          <div class="help-content" v-html="currentContent"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { 
  Search, InfoFilled, VideoPlay, Document, MapLocation, TrendCharts, 
  Notebook, EditPen, PieChart, QuestionFilled 
} from '@element-plus/icons-vue'

// 响应式数据
const activeSection = ref('overview')
const searchKeyword = ref('')

// 帮助内容数据
const helpContent = {
  overview: `
    <h3>系统概述</h3>
    <p>森林资源管理系统是一个基于Web的数字化林业管理平台，旨在为林业管理部门提供全面的森林资源管理解决方案。</p>
    
    <h4>主要功能</h4>
    <ul>
      <li><strong>林木资源档案管理</strong>：管理林木的基本信息，包括树种、胸径、树高、健康状态等</li>
      <li><strong>林地分类管理</strong>：对用材林、防护林、经济林进行分类管理</li>
      <li><strong>森林资源动态监测</strong>：监测生长量、蓄积量变化，提供趋势分析</li>
      <li><strong>林权证书管理</strong>：管理林权证书的全生命周期，包括到期提醒</li>
      <li><strong>采伐许可管理</strong>：处理采伐申请、审批流程和统计分析</li>
      <li><strong>统计报表</strong>：提供各类数据的统计分析和可视化展示</li>
    </ul>
    
    <h4>系统特点</h4>
    <ul>
      <li>界面友好，操作简单</li>
      <li>数据可视化，图表丰富</li>
      <li>功能完整，流程规范</li>
      <li>响应式设计，支持多设备</li>
    </ul>
  `,
  
  'getting-started': `
    <h3>快速入门</h3>
    <p>本指南将帮助您快速了解和使用森林资源管理系统。</p>
    
    <h4>第一步：了解系统布局</h4>
    <p>系统采用顶部导航设计，主要包含以下菜单：</p>
    <ul>
      <li>系统概览：查看整体数据统计</li>
      <li>林木档案：管理林木基本信息</li>
      <li>林地分类：管理不同类型的林地</li>
      <li>资源监测：查看资源变化趋势</li>
      <li>林权证书：管理林权证书</li>
      <li>采伐许可：处理采伐申请</li>
      <li>统计报表：查看各类统计数据</li>
      <li>系统设置：配置系统参数</li>
    </ul>
    
    <h4>第二步：基本操作</h4>
    <ol>
      <li>点击顶部导航菜单切换功能模块</li>
      <li>使用搜索框快速查找数据</li>
      <li>点击"新增"按钮添加新记录</li>
      <li>使用表格操作按钮进行编辑、删除等操作</li>
      <li>利用分页功能浏览大量数据</li>
    </ol>
    
    <h4>第三步：数据管理</h4>
    <p>系统使用演示数据，您可以：</p>
    <ul>
      <li>添加、编辑、删除各类记录</li>
      <li>查看数据统计和图表</li>
      <li>导出数据报表</li>
      <li>在系统设置中重置演示数据</li>
    </ul>
  `,
  
  'tree-archive': `
    <h3>林木档案管理</h3>
    <p>林木档案管理模块用于记录和管理森林中每棵树木的详细信息。</p>
    
    <h4>功能说明</h4>
    <ul>
      <li><strong>档案录入</strong>：记录树种、胸径、树高、健康状态等基本信息</li>
      <li><strong>档案查询</strong>：支持按树种、健康状态、位置等条件搜索</li>
      <li><strong>档案编辑</strong>：修改林木的基本信息</li>
      <li><strong>批量操作</strong>：支持批量删除等操作</li>
      <li><strong>统计分析</strong>：提供树种分布、健康状态统计等</li>
    </ul>
    
    <h4>操作步骤</h4>
    <ol>
      <li>点击"新增林木档案"按钮</li>
      <li>填写树种、胸径、树高等必填信息</li>
      <li>选择健康状态（健康、良好、一般、较差、病虫害）</li>
      <li>填写位置信息（可选）</li>
      <li>点击"确定"保存档案</li>
    </ol>
    
    <h4>注意事项</h4>
    <ul>
      <li>胸径范围：5-100cm</li>
      <li>树高范围：1-50m</li>
      <li>树种名称长度：2-20个字符</li>
      <li>健康状态必须从预设选项中选择</li>
    </ul>
  `,
  
  'forest-classify': `
    <h3>林地分类管理</h3>
    <p>林地分类管理模块用于对不同用途的林地进行分类管理和统计分析。</p>
    
    <h4>林地分类</h4>
    <ul>
      <li><strong>用材林</strong>：以生产木材为主要目的的森林</li>
      <li><strong>防护林</strong>：以防护为主要目的的森林</li>
      <li><strong>经济林</strong>：以生产果品、食用油料等为主要目的的森林</li>
    </ul>
    
    <h4>功能特点</h4>
    <ul>
      <li>分类标签页展示，便于按类型查看</li>
      <li>统计卡片显示各类林地的数量和面积</li>
      <li>支持林地信息的增删改查</li>
      <li>提供林地分布的可视化展示</li>
    </ul>
    
    <h4>操作指南</h4>
    <ol>
      <li>点击"新增林地信息"按钮</li>
      <li>输入林地名称和选择分类类型</li>
      <li>填写面积（单位：公顷）</li>
      <li>输入位置信息和坐标（可选）</li>
      <li>添加描述信息（可选）</li>
      <li>保存林地信息</li>
    </ol>
  `,
  
  'resource-monitor': `
    <h3>资源动态监测</h3>
    <p>资源动态监测模块提供森林资源的生长量和蓄积量监测，帮助了解森林资源的变化趋势。</p>
    
    <h4>监测指标</h4>
    <ul>
      <li><strong>生长量</strong>：反映森林的生长速度，单位：cm/年</li>
      <li><strong>蓄积量</strong>：反映森林的总体规模，单位：立方米</li>
    </ul>
    
    <h4>功能特色</h4>
    <ul>
      <li>实时监测数据展示</li>
      <li>趋势图表分析</li>
      <li>异常变化预警</li>
      <li>年度对比分析</li>
      <li>监测点管理</li>
    </ul>
    
    <h4>图表说明</h4>
    <ul>
      <li><strong>生长量趋势图</strong>：显示选定林地的生长量变化</li>
      <li><strong>蓄积量趋势图</strong>：显示选定林地的蓄积量变化</li>
      <li><strong>年度对比图</strong>：对比不同月份的监测数据</li>
    </ul>
    
    <h4>预警机制</h4>
    <ul>
      <li>变化率超过50%：严重预警</li>
      <li>变化率超过30%：警告预警</li>
      <li>变化率超过20%：提醒预警</li>
    </ul>
  `,
  
  'forest-rights': `
    <h3>林权证书管理</h3>
    <p>林权证书管理模块用于管理林权证书的全生命周期，确保林权关系的合法性和有效性。</p>
    
    <h4>主要功能</h4>
    <ul>
      <li>证书信息录入和管理</li>
      <li>证书状态跟踪</li>
      <li>到期提醒功能</li>
      <li>权利人信息管理</li>
      <li>林地关联管理</li>
    </ul>
    
    <h4>证书状态</h4>
    <ul>
      <li><strong>有效</strong>：证书在有效期内</li>
      <li><strong>即将到期</strong>：距离到期日不足30天</li>
      <li><strong>过期</strong>：已超过有效期</li>
      <li><strong>注销</strong>：已注销的证书</li>
    </ul>
    
    <h4>操作流程</h4>
    <ol>
      <li>点击"新增证书"按钮</li>
      <li>填写证书编号（必填）</li>
      <li>输入权利人信息</li>
      <li>选择关联的林地</li>
      <li>设置发证日期和到期日期</li>
      <li>填写发证机关和备注</li>
      <li>保存证书信息</li>
    </ol>
    
    <h4>注意事项</h4>
    <ul>
      <li>证书编号不能重复</li>
      <li>到期日期不能早于发证日期</li>
      <li>系统会自动计算证书状态</li>
      <li>支持证书状态的手动更新</li>
    </ul>
  `,
  
  'cutting-permit': `
    <h3>采伐许可管理</h3>
    <p>采伐许可管理模块处理采伐申请的提交、审批和管理，确保采伐活动的合规性。</p>
    
    <h4>业务流程</h4>
    <ol>
      <li><strong>申请提交</strong>：申请人提交采伐许可申请</li>
      <li><strong>审批处理</strong>：管理员审批申请（批准/拒绝）</li>
      <li><strong>许可管理</strong>：跟踪许可状态和有效期</li>
      <li><strong>统计分析</strong>：分析采伐数据和趋势</li>
    </ol>
    
    <h4>许可状态</h4>
    <ul>
      <li><strong>待审批</strong>：刚提交的申请，等待审批</li>
      <li><strong>已批准</strong>：审批通过的许可</li>
      <li><strong>已拒绝</strong>：审批未通过的申请</li>
      <li><strong>已过期</strong>：超过有效期的许可</li>
    </ul>
    
    <h4>申请操作</h4>
    <ol>
      <li>点击"新增许可申请"</li>
      <li>填写许可证编号和申请人信息</li>
      <li>选择采伐地点</li>
      <li>输入采伐面积和采伐量</li>
      <li>详细说明申请理由</li>
      <li>提交申请</li>
    </ol>
    
    <h4>审批操作</h4>
    <ol>
      <li>在列表中找到待审批的申请</li>
      <li>点击"审批"按钮</li>
      <li>查看申请详情</li>
      <li>填写审批人和审批意见</li>
      <li>选择"批准"或"拒绝"</li>
    </ol>
  `,
  
  statistics: `
    <h3>统计报表</h3>
    <p>统计报表模块提供系统各类数据的统计分析和可视化展示，帮助管理者了解整体情况。</p>
    
    <h4>统计内容</h4>
    <ul>
      <li><strong>综合统计</strong>：林木、林地、证书、许可的总体数量</li>
      <li><strong>分类统计</strong>：按不同维度进行数据分类统计</li>
      <li><strong>趋势分析</strong>：显示数据的变化趋势</li>
      <li><strong>状态分布</strong>：展示各种状态的分布情况</li>
    </ul>
    
    <h4>图表类型</h4>
    <ul>
      <li><strong>饼图</strong>：显示分类数据的占比</li>
      <li><strong>柱状图</strong>：对比不同类别的数量</li>
      <li><strong>折线图</strong>：展示数据的变化趋势</li>
      <li><strong>统计表格</strong>：详细的数据列表</li>
    </ul>
    
    <h4>功能特点</h4>
    <ul>
      <li>数据实时更新</li>
      <li>交互式图表</li>
      <li>支持数据导出</li>
      <li>多维度分析</li>
    </ul>
  `,
  
  faq: `
    <h3>常见问题</h3>
    
    <h4>Q: 系统支持哪些浏览器？</h4>
    <p>A: 系统支持现代浏览器，推荐使用Chrome、Firefox、Safari、Edge等。建议使用最新版本以获得最佳体验。</p>
    
    <h4>Q: 演示数据会丢失吗？</h4>
    <p>A: 系统使用内存存储演示数据，重启后会重新生成。您可以在系统设置中手动重置数据。</p>
    
    <h4>Q: 如何导出统计报表？</h4>
    <p>A: 在统计报表页面点击"导出报表"按钮，系统会自动生成包含所有统计数据的文本报表文件并下载到本地。</p>
    
    <h4>Q: 忘记了某个功能在哪里？</h4>
    <p>A: 可以使用本页面顶部的搜索功能查找相关帮助内容，或查看系统概览了解整体功能布局。</p>
    
    <h4>Q: 数据验证规则是什么？</h4>
    <p>A: 各个模块都有相应的数据验证规则，具体请参考对应模块的帮助文档。</p>
    
    <h4>Q: 如何设置系统参数？</h4>
    <p>A: 在"系统设置"页面可以配置基本设置、数据设置和通知设置等参数。</p>
    
    <h4>Q: 系统有哪些快捷操作？</h4>
    <p>A: 支持批量删除、快速搜索、一键刷新、数据导出等快捷操作。</p>
    
    <h4>Q: 如何查看系统版本信息？</h4>
    <p>A: 在"系统设置"页面的右侧可以查看系统版本、构建时间等信息。</p>
  `
}

// 计算属性
const currentContent = computed(() => {
  const content = helpContent[activeSection.value] || helpContent.overview
  
  if (searchKeyword.value) {
    // 简单的搜索高亮
    const keyword = searchKeyword.value.toLowerCase()
    return content.replace(
      new RegExp(keyword, 'gi'),
      `<mark>$&</mark>`
    )
  }
  
  return content
})

// 生命周期
onMounted(() => {
  // 初始化
})

// 方法
const handleSectionChange = (section) => {
  activeSection.value = section
  searchKeyword.value = ''
}

const handleSearch = () => {
  // 搜索功能已通过计算属性实现
}
</script>

<style scoped>
.help {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.nav-card {
  height: fit-content;
}

.help-menu {
  border: none;
}

.help-menu .el-menu-item {
  height: 45px;
  line-height: 45px;
}

.content-card {
  min-height: 600px;
}

.help-content {
  line-height: 1.8;
  font-size: 14px;
}

.help-content h3 {
  color: #303133;
  font-size: 20px;
  margin-bottom: 15px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
}

.help-content h4 {
  color: #606266;
  font-size: 16px;
  margin: 20px 0 10px 0;
}

.help-content p {
  color: #606266;
  margin-bottom: 15px;
}

.help-content ul,
.help-content ol {
  margin: 10px 0 15px 0;
  padding-left: 25px;
}

.help-content li {
  margin-bottom: 8px;
  color: #606266;
}

.help-content strong {
  color: #303133;
  font-weight: 600;
}

.help-content :deep(mark) {
  background-color: #fff3cd;
  padding: 2px 4px;
  border-radius: 2px;
}
</style>