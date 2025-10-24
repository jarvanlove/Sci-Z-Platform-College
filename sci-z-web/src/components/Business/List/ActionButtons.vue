<!--
/**
 * @description 操作按钮组组件
 * 用于表格行操作、页面操作等场景
 * 支持权限控制、按钮配置、操作确认等功能
 */
-->
<template>
  <div class="action-buttons" :class="[`action-buttons--${size}`, customClass]">
    <!-- 主要操作按钮 -->
    <template v-for="action in visibleActions" :key="action.key">
      <BaseButton
        v-if="action.type === 'button'"
        :type="action.buttonType || 'primary'"
        :size="size"
        :loading="action.loading"
        :disabled="action.disabled"
        :icon="action.icon"
        @click="handleAction(action, rowData)"
      >
        {{ action.label }}
      </BaseButton>
      
      <!-- 文本按钮 -->
      <el-button
        v-else-if="action.type === 'text'"
        type="primary"
        :size="size"
        :loading="action.loading"
        :disabled="action.disabled"
        text
        @click="handleAction(action, rowData)"
      >
        {{ action.label }}
      </el-button>
      
      <!-- 链接按钮 -->
      <el-button
        v-else-if="action.type === 'link'"
        type="primary"
        :size="size"
        :loading="action.loading"
        :disabled="action.disabled"
        link
        @click="handleAction(action, rowData)"
      >
        {{ action.label }}
      </el-button>
      
      <!-- 下拉菜单 -->
      <el-dropdown
        v-else-if="action.type === 'dropdown'"
        :trigger="action.trigger || 'click'"
        @command="(command) => handleDropdownCommand(command, action, rowData)"
      >
        <BaseButton
          :type="action.buttonType || 'primary'"
          :size="size"
          :loading="action.loading"
          :disabled="action.disabled"
        >
          {{ action.label }}
          <el-icon class="el-icon--right">
            <ArrowDown />
          </el-icon>
        </BaseButton>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="item in action.items"
              :key="item.key"
              :command="item.key"
              :disabled="item.disabled"
              :divided="item.divided"
            >
              <el-icon v-if="item.icon">
                <component :is="item.icon" />
              </el-icon>
              {{ item.label }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      
      <!-- 分割线 -->
      <el-divider
        v-else-if="action.type === 'divider'"
        direction="vertical"
        class="action-divider"
      />
      
      <!-- 自定义插槽 -->
      <slot
        v-else-if="action.type === 'slot'"
        :name="action.slotName"
        :action="action"
        :row="rowData"
        :handle-action="(action, data) => handleAction(action, data)"
      />
    </template>
    
    <!-- 更多操作下拉菜单 -->
    <el-dropdown
      v-if="showMoreActions"
      trigger="click"
      @command="(command) => handleDropdownCommand(command, null, rowData)"
    >
      <BaseButton type="text" :size="size">
        更多
        <el-icon class="el-icon--right">
          <ArrowDown />
        </el-icon>
      </BaseButton>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="action in moreActions"
            :key="action.key"
            :command="action.key"
            :disabled="action.disabled"
            :divided="action.divided"
          >
            <el-icon v-if="action.icon">
              <component :is="action.icon" />
            </el-icon>
            {{ action.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BaseButton } from '@/components/Common'
import { ArrowDown } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 操作按钮配置
  actions: {
    type: Array,
    default: () => []
  },
  // 行数据
  rowData: {
    type: Object,
    default: () => ({})
  },
  // 按钮尺寸
  size: {
    type: String,
    default: 'small',
    validator: (value) => ['large', 'default', 'small'].includes(value)
  },
  // 最大显示按钮数量
  maxVisible: {
    type: Number,
    default: 3
  },
  // 权限检查函数
  permissionCheck: {
    type: Function,
    default: () => true
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['action', 'confirm'])

// 计算可见的操作按钮
const visibleActions = computed(() => {
  const actions = props.actions.filter(action => {
    // 权限检查
    if (action.permission && !props.permissionCheck(action.permission)) {
      return false
    }
    
    // 条件显示检查
    if (action.show && typeof action.show === 'function') {
      return action.show(props.rowData)
    }
    
    return true
  })
  
  return actions.slice(0, props.maxVisible)
})

// 计算更多操作按钮
const moreActions = computed(() => {
  const actions = props.actions.filter(action => {
    // 权限检查
    if (action.permission && !props.permissionCheck(action.permission)) {
      return false
    }
    
    // 条件显示检查
    if (action.show && typeof action.show === 'function') {
      return action.show(props.rowData)
    }
    
    return true
  })
  
  return actions.slice(props.maxVisible)
})

// 是否显示更多操作
const showMoreActions = computed(() => {
  return moreActions.value.length > 0
})

// 处理操作点击
const handleAction = async (action, data) => {
  try {
    // 确认操作
    if (action.confirm) {
      const confirmResult = await ElMessageBox.confirm(
        action.confirm.message || '确认执行此操作吗？',
        action.confirm.title || '确认操作',
        {
          confirmButtonText: action.confirm.confirmText || '确定',
          cancelButtonText: action.confirm.cancelText || '取消',
          type: action.confirm.type || 'warning'
        }
      )
      
      if (!confirmResult) return
    }
    
    // 执行操作
    if (action.handler && typeof action.handler === 'function') {
      await action.handler(data, action)
    }
    
    // 触发事件
    emit('action', {
      action: action.key,
      data,
      actionConfig: action
    })
    
    // 成功提示
    if (action.successMessage) {
      ElMessage.success(action.successMessage)
    }
  } catch (error) {
    console.error('Action error:', error)
    
    // 错误提示
    if (action.errorMessage) {
      ElMessage.error(action.errorMessage)
    }
  }
}

// 处理下拉菜单命令
const handleDropdownCommand = async (command, dropdownAction, data) => {
  const action = dropdownAction?.items?.find(item => item.key === command) || 
                 moreActions.value.find(action => action.key === command)
  
  if (action) {
    await handleAction(action, data)
  }
}
</script>

<style lang="scss" scoped>
.action-buttons {
  display: flex;
  align-items: center;
  gap: var(--gap-xs);
  
  &--large {
    gap: var(--gap-sm);
  }
  
  &--small {
    gap: 2px;
  }
  
  .action-divider {
    margin: 0 var(--gap-xs);
    height: 16px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .action-buttons {
    flex-wrap: wrap;
    gap: var(--gap-xs);
  }
}
</style>
