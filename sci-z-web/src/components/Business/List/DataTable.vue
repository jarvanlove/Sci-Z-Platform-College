<!--
/**
 * @description 数据表格组件
 * 用于展示列表数据，支持搜索、分页、操作等功能
 * 集成了BaseTable、SearchForm、ActionButtons等组件
 */
-->
<template>
  <div class="data-table-container">
    <!-- 搜索表单 -->
    <SearchForm
      v-if="showSearch"
      :fields="searchFields"
      :advanced-fields="advancedSearchFields"
      :show-advanced="showAdvancedSearch"
      :loading="loading"
      :initial-values="searchParams"
      @search="handleSearch"
      @reset="handleReset"
    />
    
    <!-- 表格操作栏 -->
    <BaseCard v-if="showToolbar" class="table-toolbar">
      <div class="toolbar-content">
        <div class="toolbar-left">
          <slot name="toolbar-left">
            <h3 v-if="title" class="table-title">{{ title }}</h3>
          </slot>
        </div>
        <div class="toolbar-right">
          <slot name="toolbar-right">
            <BaseButton
              v-if="showRefresh"
              type="primary"
              :loading="loading"
              @click="handleRefresh"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </BaseButton>
            <BaseButton
              v-if="showAdd"
              type="primary"
              @click="handleAdd"
            >
              <el-icon><Plus /></el-icon>
              新增
            </BaseButton>
          </slot>
        </div>
      </div>
    </BaseCard>
    
    <!-- 数据表格 -->
    <BaseCard class="table-card">
      <BaseTable
        :data="tableData"
        :loading="loading"
        :border="border"
        :stripe="stripe"
        :selectable="selectable"
        :show-index="showIndex"
        :pagination="paginationConfig"
        @selection-change="handleSelectionChange"
        @update:currentPage="handlePageChange"
        @update:pageSize="handlePageSizeChange"
      >
        <!-- 表格列插槽 -->
        <template v-for="column in columns" :key="column.prop">
          <!-- 选择列 -->
          <el-table-column
            v-if="column.type === 'selection'"
            type="selection"
            :width="column.width || 55"
            :selectable="column.selectable"
          />
          
          <!-- 序号列 -->
          <el-table-column
            v-else-if="column.type === 'index'"
            type="index"
            :label="column.label || '序号'"
            :width="column.width || 60"
            :index="column.indexMethod"
          />
          
          <!-- 状态列 -->
          <el-table-column
            v-else-if="column.type === 'status'"
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
            :min-width="column.minWidth"
            :align="column.align || 'center'"
          >
            <template #default="{ row }">
              <StatusTag
                :status="getNestedValue(row, column.prop)"
                :status-map="column.statusMap"
                :show-icon="column.showIcon !== false"
                :size="column.tagSize || 'small'"
              />
            </template>
          </el-table-column>
          
          <!-- 操作列 -->
          <el-table-column
            v-else-if="column.type === 'actions'"
            :label="column.label || '操作'"
            :width="column.width || 160"
            :align="column.align || 'center'"
            :fixed="column.fixed"
          >
            <template #default="{ row, $index }">
              <ActionButtons
                :actions="column.actions"
                :row-data="row"
                :size="column.buttonSize || 'small'"
                :permission-check="permissionCheck"
                @action="(payload) => handleAction(payload, row, $index)"
              />
            </template>
          </el-table-column>
          
          <!-- 自定义列 -->
          <el-table-column
            v-else-if="column.type === 'slot'"
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
            :min-width="column.minWidth"
            :align="column.align"
            :fixed="column.fixed"
          >
            <template #default="{ row, $index }">
              <slot
                :name="column.slotName"
                :row="row"
                :index="$index"
                :value="getNestedValue(row, column.prop)"
              />
            </template>
          </el-table-column>
          
          <!-- 普通列 -->
          <el-table-column
            v-else
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
            :min-width="column.minWidth"
            :align="column.align"
            :fixed="column.fixed"
            :sortable="column.sortable"
            :formatter="column.formatter"
          >
            <template v-if="column.render" #default="{ row, $index }">
              <component
                :is="column.render"
                :row="row"
                :index="$index"
                :value="getNestedValue(row, column.prop)"
              />
            </template>
          </el-table-column>
        </template>
      </BaseTable>
    </BaseCard>
    
    <!-- 批量操作栏 -->
    <BaseCard v-if="showBatchActions && selectedRows.length > 0" class="batch-actions">
      <div class="batch-content">
        <span class="batch-info">已选择 {{ selectedRows.length }} 项</span>
        <div class="batch-buttons">
          <ActionButtons
            :actions="batchActions"
            :row-data="{ selectedRows }"
            :size="'small'"
            :permission-check="permissionCheck"
            @action="handleBatchAction"
          />
        </div>
      </div>
    </BaseCard>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { BaseCard, BaseTable, BaseButton } from '@/components/Common'
import SearchForm from './SearchForm.vue'
import StatusTag from './StatusTag.vue'
import ActionButtons from './ActionButtons.vue'
import { Refresh, Plus } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 表格标题
  title: {
    type: String,
    default: ''
  },
  // 表格列配置
  columns: {
    type: Array,
    default: () => []
  },
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 搜索字段配置
  searchFields: {
    type: Array,
    default: () => []
  },
  // 高级搜索字段配置
  advancedSearchFields: {
    type: Array,
    default: () => []
  },
  // 是否显示搜索
  showSearch: {
    type: Boolean,
    default: true
  },
  // 是否显示高级搜索
  showAdvancedSearch: {
    type: Boolean,
    default: false
  },
  // 是否显示工具栏
  showToolbar: {
    type: Boolean,
    default: true
  },
  // 是否显示刷新按钮
  showRefresh: {
    type: Boolean,
    default: true
  },
  // 是否显示新增按钮
  showAdd: {
    type: Boolean,
    default: true
  },
  // 是否显示批量操作
  showBatchActions: {
    type: Boolean,
    default: false
  },
  // 批量操作配置
  batchActions: {
    type: Array,
    default: () => []
  },
  // 表格配置
  border: {
    type: Boolean,
    default: true
  },
  stripe: {
    type: Boolean,
    default: true
  },
  selectable: {
    type: Boolean,
    default: false
  },
  showIndex: {
    type: Boolean,
    default: true
  },
  // 分页配置
  pagination: {
    type: Object,
    default: () => ({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
  },
  // 权限检查函数
  permissionCheck: {
    type: Function,
    default: () => true
  }
})

// Emits
const emit = defineEmits([
  'search',
  'reset',
  'refresh',
  'add',
  'edit',
  'delete',
  'view',
  'action',
  'batch-action',
  'selection-change',
  'page-change',
  'page-size-change'
])

// 响应式数据
const selectedRows = ref([])
const searchParams = reactive({})

// 计算属性
const tableData = computed(() => props.data)

const paginationConfig = computed(() => ({
  currentPage: props.pagination.currentPage,
  pageSize: props.pagination.pageSize,
  total: props.pagination.total
}))

// 获取嵌套对象的值
const getNestedValue = (obj, path) => {
  return path.split('.').reduce((current, key) => current?.[key], obj)
}

// 处理搜索
const handleSearch = (params) => {
  Object.assign(searchParams, params)
  emit('search', params)
}

// 处理重置
const handleReset = () => {
  Object.keys(searchParams).forEach(key => {
    searchParams[key] = ''
  })
  emit('reset')
}

// 处理刷新
const handleRefresh = () => {
  emit('refresh')
}

// 处理新增
const handleAdd = () => {
  emit('add')
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

// 处理操作
const handleAction = (payload, row, index) => {
  const { action, data, actionConfig } = payload
  
  // 触发具体操作事件
  switch (action) {
    case 'edit':
      emit('edit', row, index)
      break
    case 'delete':
      emit('delete', row, index)
      break
    case 'view':
      emit('view', row, index)
      break
    default:
      emit('action', { action, row, index, data, actionConfig })
  }
}

// 处理批量操作
const handleBatchAction = (payload) => {
  emit('batch-action', {
    ...payload,
    selectedRows: selectedRows.value
  })
}

// 处理分页变化
const handlePageChange = (page) => {
  emit('page-change', page)
}

const handlePageSizeChange = (size) => {
  emit('page-size-change', size)
}
</script>

<style lang="scss" scoped>
.data-table-container {
  .table-toolbar {
    margin-bottom: var(--gap-lg);
    
    .toolbar-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .table-title {
        margin: 0;
        font-size: var(--font-size-lg);
        font-weight: 600;
        color: var(--text);
      }
      
      .toolbar-right {
        display: flex;
        gap: var(--gap-sm);
      }
    }
  }
  
  .table-card {
    margin-bottom: var(--gap-lg);
  }
  
  .batch-actions {
    position: sticky;
    bottom: 0;
    z-index: 10;
    
    .batch-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .batch-info {
        color: var(--text-2);
        font-size: var(--font-size-sm);
      }
      
      .batch-buttons {
        display: flex;
        gap: var(--gap-sm);
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .data-table-container {
    .table-toolbar {
      .toolbar-content {
        flex-direction: column;
        gap: var(--gap-md);
        align-items: stretch;
        
        .toolbar-right {
          justify-content: center;
        }
      }
    }
    
    .batch-actions {
      .batch-content {
        flex-direction: column;
        gap: var(--gap-sm);
        align-items: stretch;
        
        .batch-buttons {
          justify-content: center;
        }
      }
    }
  }
}
</style>
