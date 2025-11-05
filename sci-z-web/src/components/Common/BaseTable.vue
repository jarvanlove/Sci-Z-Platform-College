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
      :default-sort="defaultSort"
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
        label="操作"
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
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="pageSizes"
        :layout="paginationLayout"
        :background="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

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

// 响应式数据
const selectedRows = ref([])

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
    overflow: hidden;
  }

  &__pagination {
    display: flex;
    justify-content: center;
    margin-top: var(--gap-lg);
    padding: var(--gap-lg);
    background: var(--surface);
    border-radius: var(--radius-lg);
    border: 1px solid var(--border);
  }
}

// 表格样式优化
:deep(.el-table) {
  .el-table__header {
    background: #fafafa;
  }

  .el-table__header th {
    background: #fafafa;
    color: var(--text-2);
    font-weight: 600;
    border-bottom: 1px solid var(--border);
  }

  .el-table__body tr:hover > td {
    background: var(--hover);
  }

  .el-table__body td {
    border-bottom: 1px solid #f5f5f5;
  }
}
</style>
