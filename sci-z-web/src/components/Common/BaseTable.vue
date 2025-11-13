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
import { computed, ref } from 'vue'
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
    overflow-x: auto;
  }

  .el-table__body-wrapper {
    overflow-x: auto;
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
      }
    }

    td {
      border-bottom: 1px solid var(--border) !important;
      color: var(--text) !important;
      padding: 12px 16px !important;
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
