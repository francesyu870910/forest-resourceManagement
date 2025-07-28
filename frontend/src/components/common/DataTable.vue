<template>
  <div class="data-table">
    <el-table
      :data="tableData"
      :loading="loading"
      :size="tableConfig.size"
      :stripe="tableConfig.stripe"
      :border="tableConfig.border"
      :fit="tableConfig.fit"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="55"
      />
      
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth"
        :sortable="column.sortable"
        :formatter="column.formatter"
      >
        <template v-if="column.slot" #default="scope">
          <slot :name="column.slot" :row="scope.row" :index="scope.$index" />
        </template>
      </el-table-column>
      
      <el-table-column
        v-if="showActions"
        label="操作"
        :width="actionWidth"
        fixed="right"
      >
        <template #default="scope">
          <slot name="actions" :row="scope.row" :index="scope.$index">
            <el-button
              v-if="showEdit"
              type="primary"
              size="small"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="showDelete"
              type="danger"
              size="small"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </slot>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      v-if="showPagination"
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
      :layout="paginationConfig.layout"
      :size="paginationConfig.size"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useResponsive } from '../../utils/responsive'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  showSelection: {
    type: Boolean,
    default: false
  },
  showActions: {
    type: Boolean,
    default: true
  },
  showEdit: {
    type: Boolean,
    default: true
  },
  showDelete: {
    type: Boolean,
    default: true
  },
  actionWidth: {
    type: [String, Number],
    default: 150
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  total: {
    type: Number,
    default: 0
  },
  pageSize: {
    type: Number,
    default: 10
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  }
})

const emit = defineEmits([
  'selection-change',
  'edit',
  'delete',
  'size-change',
  'current-change'
])

const { isMobile, isTablet } = useResponsive()

const tableData = computed(() => props.data)

// 响应式表格配置
const tableConfig = computed(() => {
  if (isMobile()) {
    return {
      size: 'small',
      stripe: true,
      border: false,
      fit: true
    }
  }
  
  if (isTablet()) {
    return {
      size: 'default',
      stripe: true,
      border: true,
      fit: true
    }
  }
  
  return {
    size: 'default',
    stripe: true,
    border: true,
    fit: false
  }
})

// 响应式分页配置
const paginationConfig = computed(() => {
  if (isMobile()) {
    return {
      layout: 'prev, pager, next',
      size: 'small'
    }
  }
  
  if (isTablet()) {
    return {
      layout: 'total, prev, pager, next',
      size: 'default'
    }
  }
  
  return {
    layout: 'total, sizes, prev, pager, next, jumper',
    size: 'default'
  }
})

const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

const handleEdit = (row) => {
  emit('edit', row)
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除这条记录吗？',
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    emit('delete', row)
  }).catch(() => {
    // 用户取消删除
  })
}

const handleSizeChange = (size) => {
  emit('size-change', size)
}

const handleCurrentChange = (page) => {
  emit('current-change', page)
}
</script>

<style scoped>
.data-table {
  width: 100%;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .pagination {
    text-align: center;
  }
}

@media (max-width: 480px) {
  .data-table {
    overflow-x: auto;
  }
  
  .pagination {
    margin-top: 15px;
  }
}
</style>