<!--
/**
 * @description 基础表格组件
 * 统一的表格样式和功能，支持分页、排序、选择等
 * 可在所有列表页面中复用，确保表格风格统一
 */
-->
<template>
  <div class="base-table">
    <!-- 表格头部操作区 -->
    <div v-if="$slots.toolbar" class="base-table__toolbar">
      <slot name="toolbar" />
    </div>

    <!-- 表格主体 -->
    <el-table
      ref="tableRef"
      :data="data"
      :loading="loading"
      :stripe="stripe"
      :border="border"
      :height="height"
      :max-height="maxHeight"
      :row-key="rowKey"
      :default-sort="defaultSort || undefined"
      :empty-text="emptyText"
      :selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      @row-click="handleRowClick"
      @row-dblclick="handleRowDblClick"
      class="base-table__table"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="selectable"
        type="selection"
        width="50"
        align="center"
      />

      <!-- 序号列 -->
      <el-table-column
        v-if="showIndex"
        type="index"
        label="序号"
        width="80"
        align="center"
        :index="getIndex"
      />

      <!-- 动态列 -->
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth"
        :fixed="column.fixed"
        :sortable="column.sortable"
        :align="column.align || 'left'"
        :show-overflow-tooltip="column.showOverflowTooltip !== false"
      >
        <template #default="{ row, $index }">
          <slot
            :name="column.prop"
            :row="row"
            :index="$index"
            :column="column"
          >
            {{ getColumnValue(row, column) }}
          </slot>
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column
        v-if="$slots.actions"
        :label="computedActionLabel"
        :width="actionWidth"
        :fixed="actionFixed"
        align="center"
      >
        <template #default="{ row, $index }">
          <slot name="actions" :row="row" :index="$index" />
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div v-if="showPagination && pagination" class="base-table__pagination">
      <BasePagination
        :current="pagination.current"
        :size="pagination.size"
        :total="pagination.total"
        @change="handleCurrentChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import BasePagination from './BasePagination.vue'

// Props 定义
const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  stripe: {
    type: Boolean,
    default: true
  },
  border: {
    type: Boolean,
    default: true
  },
  height: {
    type: [String, Number],
    default: null
  },
  maxHeight: {
    type: [String, Number],
    default: null
  },
  rowKey: {
    type: [String, Function],
    default: 'id'
  },
  defaultSort: {
    type: Object,
    default: null
  },
  selectable: {
    type: Boolean,
    default: false
  },
  showIndex: {
    type: Boolean,
    default: false
  },
  actionWidth: {
    type: [String, Number],
    default: '120'
  },
  actionFixed: {
    type: [String, Boolean],
    default: 'right'
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  pagination: {
    type: Object,
    default: () => ({
      current: 1,
      size: 10,
      total: 0
    })
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  paginationLayout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  actionLabel: {
    type: String,
    default: null
  },
  emptyText: {
    type: String,
    default: '暂无数据'
  }
})

// Emits 定义
const emit = defineEmits([
  'selection-change',
  'sort-change',
  'row-click',
  'row-dblclick',
  'size-change',
  'current-change'
])

// 国际化
const { t } = useI18n()

// 响应式数据
const selectedRows = ref([])
const tableRef = ref(null)
let cleanupScrollSync = null

// 计算属性
const computedActionLabel = computed(() => {
  return props.actionLabel || t('common.actions')
})

// 计算属性
const getIndex = (index) => {
  return (props.pagination.current - 1) * props.pagination.size + index + 1
}

// 方法
const getColumnValue = (row, column) => {
  if (column.formatter && typeof column.formatter === 'function') {
    return column.formatter(row, column)
  }
  
  const keys = column.prop.split('.')
  let value = row
  for (const key of keys) {
    value = value?.[key]
  }
  return value ?? ''
}

// 事件处理
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

const handleSortChange = (sort) => {
  emit('sort-change', sort)
}

const handleRowClick = (row, column, event) => {
  emit('row-click', row, column, event)
}

const handleRowDblClick = (row, column, event) => {
  emit('row-dblclick', row, column, event)
}

const handleSizeChange = (size) => {
  emit('size-change', size)
}

const handleCurrentChange = (current) => {
  emit('current-change', current)
}

// 同步表头和表体的滚动
const syncScroll = () => {
  if (!tableRef.value) return
  
  // 获取表格的 DOM 元素
  const tableEl = tableRef.value.$el || tableRef.value
  if (!tableEl) return
  
  const headerWrapper = tableEl.querySelector('.el-table__header-wrapper')
  const bodyWrapper = tableEl.querySelector('.el-table__body-wrapper')
  
  if (!headerWrapper || !bodyWrapper) return
  
  // 获取所有滚动容器（包括固定列）
  const getAllScrollContainers = () => {
    const containers = [headerWrapper, bodyWrapper]
    
    // 查找所有固定列的滚动容器 - 使用更全面的选择器
    const fixedRightBody = tableEl.querySelector('.el-table__fixed-right .el-table__fixed-body-wrapper')
    const fixedRightHeader = tableEl.querySelector('.el-table__fixed-right .el-table__fixed-header-wrapper')
    const fixedBody = tableEl.querySelector('.el-table__fixed .el-table__fixed-body-wrapper')
    const fixedHeader = tableEl.querySelector('.el-table__fixed .el-table__fixed-header-wrapper')
    
    if (fixedRightBody) containers.push(fixedRightBody)
    if (fixedRightHeader) containers.push(fixedRightHeader)
    if (fixedBody) containers.push(fixedBody)
    if (fixedHeader) containers.push(fixedHeader)
    
    return containers.filter(Boolean)
  }
  
  const allContainers = getAllScrollContainers()
  
  if (allContainers.length === 0) return
  
  // 防止循环触发
  let isScrolling = false
  
  // 同步所有容器的滚动位置 - 强制同步，不检查条件
  const syncAllContainers = (sourceScrollLeft) => {
    allContainers.forEach(container => {
      // 强制设置，确保所有容器都同步
      if (container.scrollLeft !== sourceScrollLeft) {
        container.scrollLeft = sourceScrollLeft
      }
    })
  }
  
  // 为每个容器添加滚动监听
  const scrollHandlers = allContainers.map(container => {
    const handleScroll = () => {
      if (isScrolling) return
      isScrolling = true
      requestAnimationFrame(() => {
        syncAllContainers(container.scrollLeft)
        isScrolling = false
      })
    }
    
    container.addEventListener('scroll', handleScroll, { passive: true })
    return { container, handler: handleScroll }
  })
  
  // 返回清理函数
  return () => {
    scrollHandlers.forEach(({ container, handler }) => {
      container.removeEventListener('scroll', handler)
    })
  }
}

// 生命周期
onMounted(() => {
  // 延迟初始化，确保固定列已经渲染完成
  nextTick(() => {
    // 使用更长的延迟，确保所有 DOM 都已渲染
    setTimeout(() => {
      if (tableRef.value) {
        cleanupScrollSync = syncScroll()
        // 如果第一次没找到固定列，再试一次
        if (!cleanupScrollSync) {
          setTimeout(() => {
            if (tableRef.value) {
              cleanupScrollSync = syncScroll()
            }
          }, 200)
        }
      }
    }, 200)
  })
})

onUnmounted(() => {
  if (cleanupScrollSync) {
    cleanupScrollSync()
  }
})
</script>

<style lang="scss" scoped>
.base-table {
  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--gap-lg);
    padding: var(--gap-md);
    background: var(--surface);
    border-radius: var(--radius-md);
    border: 1px solid var(--border);
  }

  &__table {
    background: var(--surface);
    border-radius: var(--radius-lg);
    overflow: visible;
    width: 100% !important;
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}

// 表格样式优化 - 暗色主题适配
:deep(.el-table) {
  background-color: var(--surface) !important;
  color: var(--text) !important;
  border-color: var(--border) !important;

  .el-table__header {
    background-color: var(--surface) !important;
  }

  .el-table__header-wrapper {
    overflow-x: auto !important;
    overflow-y: hidden;
    // 使用自定义滚动条样式
    scrollbar-width: thin; /* Firefox */
    scrollbar-color: var(--border) transparent; /* Firefox */
    -ms-overflow-style: -ms-autohiding-scrollbar; /* IE 10+ */
    
    &::-webkit-scrollbar {
      width: 8px;
      height: 8px;
    }
    
    &::-webkit-scrollbar-track {
      background: transparent;
      border-radius: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: var(--border);
      border-radius: 4px;
      transition: all 0.3s ease;
      
      &:hover {
        background: var(--border-hover);
      }
    }
  }

  .el-table__body-wrapper {
    overflow-x: auto !important;
    overflow-y: hidden;
    // 使用自定义滚动条样式
    scrollbar-width: thin; /* Firefox */
    scrollbar-color: var(--border) transparent; /* Firefox */
    -ms-overflow-style: -ms-autohiding-scrollbar; /* IE 10+ */
    
    &::-webkit-scrollbar {
      width: 8px;
      height: 8px;
    }
    
    &::-webkit-scrollbar-track {
      background: transparent;
      border-radius: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: var(--border);
      border-radius: 4px;
      transition: all 0.3s ease;
      
      &:hover {
        background: var(--border-hover);
      }
    }
  }
  
  // 确保表格内容可以超出容器宽度，触发横向滚动
  // 同时确保表头和表体的列宽一致
  .el-table__header,
  .el-table__body {
    width: 100% !important;
    min-width: 100%;
    // 不设置 table-layout: fixed，让 Element Plus 自动计算列宽
    // Element Plus 会自动同步表头和表体的列宽
  }
  
  // 确保表头和表体的列宽完全一致
  .el-table__header-wrapper,
  .el-table__body-wrapper {
    // 确保滚动容器宽度一致
    width: 100%;
  }
  
  // 确保所有行都能正确滚动 - 统一处理，不区分 stripe 行
  .el-table__body {
    tr {
      td {
        position: relative;
        white-space: nowrap;
        // 不设置 overflow，让父容器的滚动来控制隐藏
        overflow: visible !important;
      }
    }
  }
  
  // 固定列也需要支持滚动
  .el-table__fixed,
  .el-table__fixed-right {
    .el-table__fixed-body-wrapper {
      overflow-x: auto !important;
      overflow-y: hidden;
      // 使用自定义滚动条样式
      scrollbar-width: thin; /* Firefox */
      scrollbar-color: var(--border) transparent; /* Firefox */
      -ms-overflow-style: -ms-autohiding-scrollbar; /* IE 10+ */
      
      &::-webkit-scrollbar {
        width: 8px;
        height: 8px;
      }
      
      &::-webkit-scrollbar-track {
        background: transparent;
        border-radius: 4px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: var(--border);
        border-radius: 4px;
        transition: all 0.3s ease;
        
        &:hover {
          background: var(--border-hover);
        }
      }
    }
    
    .el-table__fixed-header-wrapper {
      overflow-x: auto !important;
      overflow-y: hidden;
      // 使用自定义滚动条样式
      scrollbar-width: thin; /* Firefox */
      scrollbar-color: var(--border) transparent; /* Firefox */
      -ms-overflow-style: -ms-autohiding-scrollbar; /* IE 10+ */
      
      &::-webkit-scrollbar {
        width: 8px;
        height: 8px;
      }
      
      &::-webkit-scrollbar-track {
        background: transparent;
        border-radius: 4px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: var(--border);
        border-radius: 4px;
        transition: all 0.3s ease;
        
        &:hover {
          background: var(--border-hover);
        }
      }
    }
    
    // 确保固定列中的所有行都能正确滚动 - 统一处理
    .el-table__fixed-body-wrapper {
      tr {
        td {
          position: relative;
          white-space: nowrap;
          // 确保内容可以被滚动隐藏
          overflow: visible !important;
        }
      }
    }
  }

  .el-table__header th {
    background-color: var(--surface) !important;
    color: var(--text-2) !important;
    font-weight: 600 !important;
    font-family: "Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    font-size: 14px;
    letter-spacing: 0.01em;
    border-bottom: 1px solid var(--border) !important;
    padding: 14px 16px !important;
    // 确保表头列宽与表体一致
    box-sizing: border-box;
  }

  .el-table__body {
    tr {
      background-color: var(--surface) !important;
      color: var(--text) !important;

      &:hover > td {
        background-color: var(--hover) !important;
      }

      &.el-table__row--striped {
        background-color: var(--hover-light) !important;
        
        // 确保 stripe 行的内容可以被滚动隐藏 - 强制设置
        td {
          overflow: visible !important;
          position: relative !important;
        }
      }
    }

    td {
      border-bottom: 1px solid var(--border) !important;
      color: var(--text) !important;
      padding: 12px 16px !important;
      position: relative;
      white-space: nowrap;
      // 确保表体列宽与表头一致
      box-sizing: border-box;
      // 不设置 overflow，让父容器的滚动来控制隐藏
    }
  }

  .el-table__empty-block {
    background-color: var(--surface) !important;
    color: var(--text-3) !important;
  }

  // 固定列样式
  .el-table__fixed,
  .el-table__fixed-right {
    background-color: var(--surface) !important;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.12);
    
    // 确保固定列中的 stripe 行背景色正确，并且能正确滚动
    .el-table__fixed-body-wrapper {
      tr.el-table__row--striped {
        background-color: var(--hover-light) !important;
        
        td {
          background-color: var(--hover-light) !important;
          // 确保 stripe 行的内容可以被滚动隐藏 - 强制设置
          overflow: visible !important;
          position: relative !important;
        }
      }
      
      tr {
        td {
          position: relative;
          white-space: nowrap;
          // 不设置 overflow，让父容器的滚动来控制隐藏
        }
      }
    }
  }

  .el-table__fixed-right-patch {
    background-color: var(--surface) !important;
  }

  &::before,
  &::after {
    background-color: var(--border) !important;
  }
}
</style>
