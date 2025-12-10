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
      @selection-change="handleSelectionChange"
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
        :class-name="getColumnClassName(column)"
      >
        <template #default="{ row, $index }">
          <div :class="getColumnCellClass(column)">
            <slot
              :name="column.prop"
              :row="row"
              :index="$index"
              :column="column"
            >
              {{ getColumnValue(row, column) }}
            </slot>
          </div>
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

// 获取列的类名，用于控制换行行为（Element Plus 表格列支持 class-name）
const getColumnClassName = (column) => {
  // 如果列配置了 wrap: true，则添加允许换行的类
  if (column.wrap === true) {
    return 'base-table__column-wrap'
  }
  // 如果列配置了 noWrap: true，则添加不换行的类
  if (column.noWrap === true) {
    return 'base-table__column-nowrap'
  }
  // 如果列配置了 showOverflowTooltip: false，默认允许换行
  if (column.showOverflowTooltip === false && column.wrap !== false) {
    return 'base-table__column-wrap'
  }
  return ''
}

// 获取单元格内容的类名，用于更精确的控制
const getColumnCellClass = (column) => {
  const classes = []
  // 如果列配置了 wrap: true，则添加允许换行的类
  if (column.wrap === true || (column.showOverflowTooltip === false && column.wrap !== false)) {
    classes.push('base-table__cell-wrap')
  }
  // 如果列配置了 noWrap: true，则添加不换行的类
  if (column.noWrap === true || (column.showOverflowTooltip !== false && column.wrap === false)) {
    classes.push('base-table__cell-nowrap')
  }
  return classes.join(' ')
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
  
  // 🔥 初始同步：确保表头和表体初始状态对齐
  const initialSync = () => {
    requestAnimationFrame(() => {
      if (bodyWrapper.scrollLeft !== headerWrapper.scrollLeft) {
        headerWrapper.scrollLeft = bodyWrapper.scrollLeft
      }
    })
  }
  
  // 立即执行一次初始同步
  initialSync()
  
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
  
  // 🔥 监听表体滚动，同步到表头（主要同步方向）
  const bodyScrollHandler = () => {
    if (isScrolling) return
    isScrolling = true
    requestAnimationFrame(() => {
      headerWrapper.scrollLeft = bodyWrapper.scrollLeft
      isScrolling = false
    })
  }
  bodyWrapper.addEventListener('scroll', bodyScrollHandler, { passive: true })
  
  // 返回清理函数
  return () => {
    scrollHandlers.forEach(({ container, handler }) => {
      container.removeEventListener('scroll', handler)
    })
    bodyWrapper.removeEventListener('scroll', bodyScrollHandler)
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

// 暴露内部tableRef，供父组件访问
defineExpose({
  tableRef
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
    // 🔥 Element Plus 标准做法：表格容器使用 overflow: hidden，不允许内容超出
    // 横向滚动由内部的 .el-table__body-wrapper 控制
    overflow: hidden;
    width: 100% !important;
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    margin-top: 16px;
    padding: 16px 0;
    width: 100%;
    
    // 确保分页组件在表格容器内正确显示
    :deep(.base-pagination) {
      width: 100%;
      min-width: fit-content;
      flex-wrap: wrap;
      gap: 12px;
      
      // 确保确认按钮样式正确
      .base-pagination__jump-btn {
        white-space: nowrap;
        flex-shrink: 0;
      }
    }
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

  // 🔥 表头：隐藏滚动条但保持滚动功能（通过表体滚动同步）
  // 关键：必须使用 overflow-x: scroll 才能设置 scrollLeft，不能用 hidden
  .el-table__header-wrapper {
    overflow-x: scroll !important; // 保持滚动功能
    overflow-y: hidden;
    // 隐藏滚动条
    scrollbar-width: none; /* Firefox */
    -ms-overflow-style: none; /* IE 10+ */
    
    &::-webkit-scrollbar {
      display: none; /* Chrome, Safari, Edge */
      width: 0;
      height: 0;
    }
  }

  // 🔥 表体：显示滚动条（用户可见）
  // Element Plus 标准做法：当列数过多时，表体显示横向滚动条
  .el-table__body-wrapper {
    overflow-x: auto !important;
    overflow-y: auto !important; // 允许纵向滚动（当数据过多时）
    // 自定义滚动条样式
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
    
    &::-webkit-scrollbar-corner {
      background: transparent;
    }
  }
  
  // 🔥 Element Plus 标准做法：确保表头和表体的列宽完全一致
  // 关键：表头的列宽决定表体的列宽，所有行必须使用相同的列宽
  .el-table__header th,
  .el-table__body td {
    box-sizing: border-box;
    // 🔥 移除可能干扰列宽同步的样式，让 Element Plus 自动计算
    // 不要设置 width、min-width、max-width，让 Element Plus 根据表头自动同步
  }
  
  // 🔥 确保表头和表体使用相同的表格布局算法
  .el-table__header-wrapper table,
  .el-table__body-wrapper table {
    // Element Plus 默认使用 table-layout: fixed，列宽由表头决定
    // 不要改为 auto，否则会导致列宽不一致
    table-layout: fixed !important;
  }
  
  // 固定列样式 - 确保在滚动时不缩进去
  .el-table__fixed,
  .el-table__fixed-right {
    // 固定列应该使用 absolute 定位（Element Plus 默认），不要改为 fixed
    // 确保固定列在滚动时保持固定位置
    position: absolute !important;
    
    .el-table__fixed-body-wrapper {
      // 固定列禁用横向滚动，只允许纵向滚动
      overflow-x: hidden !important;
      overflow-y: auto;
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
      
      // 确保固定列中的所有行（包括 stripe 行）都不缩进去
      tr {
        td {
          position: relative !important;
          white-space: nowrap;
          overflow: visible !important;
        }
      }
      
      // stripe 行（灰色背景）也要保持固定
      tr.el-table__row--striped {
        td {
          position: relative !important;
          overflow: visible !important;
          
          .cell {
            overflow: visible !important;
            position: relative !important;
          }
        }
      }
    }
    
    .el-table__fixed-header-wrapper {
      // 固定列表头禁用横向滚动
      overflow-x: hidden !important;
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
  }

  .el-table__header th {
    background-color: var(--surface) !important;
    color: var(--text-2) !important;
    font-weight: 600 !important;
    font-family: "Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    font-size: 14px;
    letter-spacing: 0.01em;
    border-bottom: 1px solid var(--border) !important;
    border-right: none !important; // 🔥 移除右侧边框，避免白色线条
    padding: 14px 16px !important;
    // 确保表头列宽与表体一致
    box-sizing: border-box;
    // 🔥 确保表头内容自适应居中显示，即使其他列内容过多
    vertical-align: middle; // 垂直居中
    position: relative; // 确保定位上下文
    
    .cell {
      // 🔥 确保单元格内容根据列的 align 属性正确对齐，不受其他列影响
      padding: 0 !important;
      line-height: 1.5;
      white-space: nowrap; // 表头默认不换行
      overflow: hidden;
      text-overflow: ellipsis;
      display: block; // 使用 block 确保对齐生效
      width: 100%;
    }
    
    // 🔥 根据列的 align 属性强制设置表头对齐（优先级最高）
    // Element Plus 会自动添加这些类名，我们确保样式生效
    &.is-center {
      text-align: center !important;
      
      .cell {
        text-align: center !important;
      }
    }
    
    &.is-right {
      text-align: right !important;
      
      .cell {
        text-align: right !important;
      }
    }
    
    &.is-left {
      text-align: left !important;
      
      .cell {
        text-align: left !important;
      }
    }
    
    // 🔥 如果没有明确的对齐类，默认左对齐（Element Plus 默认行为）
    &:not(.is-center):not(.is-right) {
      text-align: left;
      
      .cell {
        text-align: left;
      }
    }
  }

  .el-table__body {
    tr {
      background-color: var(--surface) !important;
      color: var(--text) !important;

      // 🔥 修复暗色主题下悬浮行背景色问题
      &:hover > td {
        background-color: var(--hover) !important;
        color: var(--text) !important; // 确保文字颜色正确
      }

      &.el-table__row--striped {
        background-color: var(--hover-light) !important;
        
        // 确保 stripe 行的内容可以被滚动隐藏 - 强制设置
        td {
          overflow: visible !important;
          position: relative !important;
          background-color: var(--hover-light) !important; // 确保 stripe 行背景色正确
        }
        
        // 🔥 修复暗色主题下 stripe 行悬浮时的背景色
        &:hover > td {
          background-color: var(--hover) !important;
        }
      }
    }

    td {
      border-bottom: 1px solid var(--border) !important;
      border-right: none !important; // 🔥 移除右侧边框，避免白色线条
      // 🔥 表格字体颜色：比 placeholder 稍深一点，使用 var(--text-3)
      color: var(--text-3) !important;
      padding: 12px 16px !important;
      position: relative;
      // 🔥 默认不换行，保持向后兼容
      white-space: nowrap;
      // 确保表体列宽与表头一致
      box-sizing: border-box;
      // 🔥 垂直居中对齐：当某些列换行时，其他列应该居中对齐（如图2红色标记）
      vertical-align: middle !important;
      // 🔥 Element Plus 标准做法：表体的列宽由表头决定，不允许根据内容自动调整
      // 移除所有可能干扰列宽同步的样式，让 Element Plus 自动处理
      // 不要设置 width、min-width、max-width，让 Element Plus 根据表头自动同步
      
      .cell {
        padding: 0 !important;
        line-height: 1.6;
        // 默认不换行
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        // 🔥 确保单元格内容字体颜色比 placeholder 稍深一点
        color: var(--text-3) !important;
      }
      
      // 🔥 支持换行的列：通过类名控制（两种方式：列级别和单元格级别）
      &.base-table__column-wrap {
        white-space: normal;
        
        .cell {
          white-space: normal;
          word-break: break-word; // 长单词自动换行
          overflow: visible;
        }
      }
      
      // 🔥 单元格级别的换行控制（更精确）
      .base-table__cell-wrap {
        white-space: normal;
        word-break: break-word;
        overflow: visible;
      }
      
      // 🔥 明确不换行的列
      &.base-table__column-nowrap {
        white-space: nowrap;
        
        .cell {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
      
      // 🔥 单元格级别的不换行控制
      .base-table__cell-nowrap {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }

  .el-table__empty-block {
    background-color: var(--surface) !important;
    color: var(--text-3) !important;
  }

  // 固定列样式 - 确保背景色和阴影正确
  .el-table__fixed,
  .el-table__fixed-right {
    background-color: var(--surface) !important;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.12);
    
    // 确保固定列中的 stripe 行背景色正确
    .el-table__fixed-body-wrapper {
      tr.el-table__row--striped {
        background-color: var(--hover-light) !important;
        
        td {
          background-color: var(--hover-light) !important;
          position: relative !important;
          overflow: visible !important;
        }
      }
      
      tr {
        td {
          position: relative !important;
          white-space: nowrap;
          overflow: visible !important;
        }
      }
    }
  }

  .el-table__fixed-right-patch {
    background-color: var(--surface) !important;
  }

  // 🔥 修复表格外边框颜色，避免白色线条
  &::before,
  &::after {
    background-color: var(--border) !important;
  }
  
  // 🔥 确保表格内部所有边框都使用主题色
  .el-table__inner-wrapper::before {
    background-color: var(--border) !important;
  }
  
  // 🔥 修复表格单元格之间的边框颜色
  .el-table__header th,
  .el-table__body td {
    border-right: none !important; // 移除右侧边框，统一使用底部边框
  }
  
  // 🔥 修复表格最右侧边框（如果有）
  .el-table__border-column-patch {
    background-color: var(--border) !important;
    border-color: var(--border) !important;
  }
}
</style>
