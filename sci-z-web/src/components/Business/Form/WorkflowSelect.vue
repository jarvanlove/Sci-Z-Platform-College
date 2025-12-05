<!--
/**
 * @description 工作流选择组件
 * 可复用的工作流选择器，支持搜索、高亮、清除、信息展示等功能
 * 适用于申报、报告等需要选择工作流的场景
 */
-->
<template>
  <div class="workflow-select-container">
    <div class="workflow-select-wrapper">
      <el-select
        ref="selectRef"
        :model-value="modelValue"
        :placeholder="placeholder"
        :loading="loading"
        :disabled="disabled"
        popper-class="workflow-select-dropdown"
        style="width: 100%"
        filterable
        :filter-method="filterWorkflow"
        :no-match-text="noMatchText"
        :no-data-text="noDataText"
        @update:model-value="handleValueChange"
        @visible-change="handleVisibleChange"
      >
        <el-option
          v-for="workflow in filteredWorkflowOptions"
          :key="workflow.id"
          :label="workflow.name"
          :value="workflow.id"
        >
          <div class="workflow-option">
            <div class="workflow-name" v-html="highlightText(workflow.name, workflowSearchQuery)"></div>
            <div class="workflow-description" v-html="highlightText(workflow.description, workflowSearchQuery)"></div>
          </div>
        </el-option>
      </el-select>
      <div
        v-if="modelValue && showClear"
        class="workflow-clear-btn"
        @click.stop="handleClear"
      >
        <el-icon><Close /></el-icon>
      </div>
    </div>
    <div v-if="selectedWorkflow && showInfo" class="workflow-info">
      <div class="workflow-info-title">{{ selectedWorkflow.name }}</div>
      <div class="workflow-info-description">{{ selectedWorkflow.description }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Close } from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  // 绑定值
  modelValue: {
    type: [String, Number],
    default: ''
  },
  // 工作流选项列表
  options: {
    type: Array,
    default: () => []
  },
  // 占位符
  placeholder: {
    type: String,
    default: '请选择工作流'
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 禁用状态
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否显示清除按钮
  showClear: {
    type: Boolean,
    default: true
  },
  // 是否显示选中信息卡片
  showInfo: {
    type: Boolean,
    default: true
  },
  // 无匹配文本
  noMatchText: {
    type: String,
    default: '未找到匹配的工作流'
  },
  // 无数据文本
  noDataText: {
    type: String,
    default: '暂无工作流数据'
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'clear'])

const { t } = useI18n()

const selectRef = ref()

// 工作流搜索查询
const workflowSearchQuery = ref('')
const filteredWorkflowOptions = ref([])

// 选中的工作流
const selectedWorkflow = computed(() => {
  return props.options.find(workflow => workflow.id === props.modelValue)
})

// 初始化过滤选项
const initFilteredOptions = () => {
  filteredWorkflowOptions.value = [...props.options]
}

// 工作流过滤
const filterWorkflow = (query) => {
  workflowSearchQuery.value = query || ''
  if (!query) {
    filteredWorkflowOptions.value = [...props.options]
    return
  }

  const lowerQuery = query.toLowerCase()
  filteredWorkflowOptions.value = props.options.filter(workflow => {
    return (
      workflow.name.toLowerCase().includes(lowerQuery) ||
      workflow.description?.toLowerCase().includes(lowerQuery)
    )
  })
}

// 文本高亮方法
const highlightText = (text, query) => {
  if (!query || !text) return text

  const regex = new RegExp(`(${query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

// 处理值变化
const handleValueChange = (value) => {
  emit('update:modelValue', value)
  emit('change', value)
  // 清除搜索查询
  if (value) {
    workflowSearchQuery.value = ''
    filteredWorkflowOptions.value = [...props.options]
  }
}

// 处理下拉框显示/隐藏
const handleVisibleChange = (visible) => {
  if (!visible) {
    // 关闭时重置搜索
    workflowSearchQuery.value = ''
    filteredWorkflowOptions.value = [...props.options]
  }
}

// 清除工作流选择
const handleClear = () => {
  emit('update:modelValue', '')
  emit('change', '')
  emit('clear')
  workflowSearchQuery.value = ''
  filteredWorkflowOptions.value = [...props.options]
}

// 监听选项变化
watch(() => props.options, () => {
  initFilteredOptions()
}, { immediate: true, deep: true })

// 暴露方法
defineExpose({
  clear: handleClear,
  focus: () => selectRef.value?.focus(),
  blur: () => selectRef.value?.blur()
})
</script>

<style lang="scss" scoped>
// 工作流选择容器
.workflow-select-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.workflow-select-wrapper {
  position: relative;
  width: 100%;
  flex-shrink: 0;
  
  .workflow-clear-btn {
    position: absolute;
    right: 35px;
    top: 50%;
    transform: translateY(-50%);
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    color: #9ca3af;
    transition: all 0.2s ease;
    border-radius: 50%;
    background-color: transparent;
    
    &:hover {
      color: #dc2626;
      background-color: rgba(220, 38, 38, 0.1);
    }
    
    &:active {
      transform: translateY(-50%) scale(0.95);
    }
    
    .el-icon {
      font-size: 14px;
    }
  }
  
  // 调整箭头图标位置，为清除按钮留出空间
  :deep(.el-select .el-input__suffix) {
    right: 12px;
  }
}

// 工作流选项内容样式（用于 scoped 样式中）
.workflow-option {
  padding: 0;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;

  .workflow-name {
    font-size: 14px;
    font-weight: 500;
    color: #374151;
    line-height: 1.5;
    margin: 0;
    padding: 0;
    word-break: break-word;
    display: block;
  }

  .workflow-description {
    font-size: 12px;
    color: #6b7280;
    line-height: 1.4;
    margin: 0;
    padding: 0;
    word-break: break-word;
    display: block;
  }
}

// 选中状态下的选项样式
:deep(.el-select-dropdown__item.is-selected .workflow-option .workflow-name) {
  color: var(--color-primary) !important;
  font-weight: 500;
}

// 搜索高亮样式
.workflow-option :deep(.highlight) {
  background-color: #fef3c7;
  color: #92400e;
  padding: 0 2px;
  border-radius: 2px;
}

// 无搜索结果样式
:deep(.el-select-dropdown__empty) {
  padding: 20px 0 !important;
  color: #9ca3af !important;
  text-align: center !important;
  font-size: 14px !important;
}

// 搜索框样式
:deep(.el-select .el-input__inner) {
  cursor: pointer;
}

:deep(.el-select .el-input.is-focus .el-input__inner) {
  cursor: text;
}

// 选中工作流信息卡片
// 确保信息卡片在新的一行显示，不占用下拉菜单的位置
.workflow-info {
  width: 100%;
  margin-top: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  box-sizing: border-box;
  // 确保在新行显示，不占用下拉菜单空间
  display: block;
  flex-shrink: 0;
  // 确保信息卡片不会影响下拉菜单的位置计算
  position: relative;
  z-index: 0;

  .workflow-info-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-primary);
    margin-bottom: 8px;
  }

  .workflow-info-description {
    font-size: 14px;
    color: #64748b;
    line-height: 1.5;
  }
}
</style>

<!-- 全局样式 - 用于控制 teleported 的下拉框 -->
<style lang="scss">
// 工作流下拉框全局样式
// 使用 BaseScrollbar 组件的滚动条样式规范
.workflow-select-dropdown {
  // 设置下拉框最大高度
  .el-select-dropdown__wrap {
    max-height: 300px !important;
    overflow-y: auto !important;
    
    // 使用 BaseScrollbar 的滚动条样式规范
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
  
  // Element Plus 的 el-scrollbar 组件（主要使用这个）
  .el-scrollbar {
    max-height: 300px !important;
    
    .el-scrollbar__wrap {
      max-height: 300px !important;
      overflow-y: auto !important;
      
      // 使用 BaseScrollbar 的滚动条样式规范
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
    
    // Element Plus 自定义滚动条样式（备用）
    .el-scrollbar__bar {
      &.is-vertical {
        .el-scrollbar__thumb {
          background-color: var(--border) !important;
          border-radius: 4px !important;
          transition: all 0.3s ease !important;
          
          &:hover {
            background-color: var(--border-hover) !important;
          }
        }
      }
    }
  }
  
  // 下拉选项样式
  .el-select-dropdown__item {
    height: auto !important;
    padding: 10px 20px !important;
    line-height: normal !important;
    white-space: normal !important;
    min-height: 64px !important;
    margin: 0 !important;
    display: flex !important;
    align-items: center !important;
    box-sizing: border-box !important;
    
    // 确保选项之间有清晰的间距
    & + .el-select-dropdown__item {
      border-top: 1px solid transparent;
    }
    
    &:hover {
      background-color: #f8fafc !important;
    }
    
    &.is-selected {
      background-color: #eff6ff !important;
      color: #1e3a8a !important;
      
      .workflow-option .workflow-name {
        color: #1e3a8a !important;
        font-weight: 500;
      }
    }
  }
  
  // 工作流选项内容样式
  .workflow-option {
    padding: 0;
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 6px;
    
    .workflow-name {
      font-size: 14px;
      font-weight: 500;
      color: #374151;
      line-height: 1.5;
      margin: 0;
      padding: 0;
      word-break: break-word;
      display: block;
    }
    
    .workflow-description {
      font-size: 12px;
      color: #6b7280;
      line-height: 1.4;
      margin: 0;
      padding: 0;
      word-break: break-word;
      display: block;
    }
    
    // 搜索高亮样式
    :deep(.highlight) {
      background-color: #fef3c7;
      color: #92400e;
      padding: 0 2px;
      border-radius: 2px;
    }
  }
  
  // 空状态样式
  .el-select-dropdown__empty {
    padding: 20px 0 !important;
    color: #9ca3af !important;
    text-align: center !important;
    font-size: 14px !important;
  }
}
</style>
