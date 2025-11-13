<!--
/**
 * @description 基础日期选择器组件
 * 统一封装 el-date-picker，支持中文显示，可在所有页面中复用
 */
-->
<template>
  <el-config-provider :locale="datePickerLocale">
    <el-date-picker
      :model-value="modelValue"
      v-bind="$attrs"
      :type="type"
      :placeholder="placeholder"
      :format="format"
      :value-format="valueFormat"
      :disabled="disabled"
      :clearable="clearable"
      :range-separator="rangeSeparator"
      :start-placeholder="startPlaceholder"
      :end-placeholder="endPlaceholder"
      @update:model-value="handleChange"
      @blur="handleBlur"
      @focus="handleFocus"
      class="base-date-picker"
    />
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'
import ja from 'element-plus/dist/locale/ja.mjs'
import ko from 'element-plus/dist/locale/ko.mjs'

const props = defineProps({
  modelValue: {
    type: [String, Array, Date],
    default: null
  },
  type: {
    type: String,
    default: 'date',
    validator: (value) => ['date', 'daterange', 'datetime', 'datetimerange', 'month', 'year'].includes(value)
  },
  placeholder: {
    type: String,
    default: ''
  },
  format: {
    type: String,
    default: 'YYYY-MM-DD'
  },
  valueFormat: {
    type: String,
    default: 'YYYY-MM-DD'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  clearable: {
    type: Boolean,
    default: true
  },
  rangeSeparator: {
    type: String,
    default: '至'
  },
  startPlaceholder: {
    type: String,
    default: '开始日期'
  },
  endPlaceholder: {
    type: String,
    default: '结束日期'
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'blur', 'focus'])

const { locale } = useI18n()

// 根据当前语言设置日期选择器的语言
const datePickerLocale = computed(() => {
  const localeMap = {
    'zh-CN': zhCn,
    'en-US': en,
    'ja-JP': ja,
    'ko-KR': ko
  }
  return localeMap[locale.value] || zhCn
})

// 处理日期变化
const handleChange = (value) => {
  emit('update:modelValue', value)
  // change 事件由 el-date-picker 自动触发，这里不需要重复触发
}

// 处理失焦事件
const handleBlur = (event) => {
  emit('blur', event)
}

// 处理聚焦事件
const handleFocus = (event) => {
  emit('focus', event)
}
</script>

<style lang="scss" scoped>
.base-date-picker {
  width: 100%;

  /* 输入框样式 - 确保暗色主题下正确显示 */
  :deep(.el-input__wrapper) {
    border-radius: 4px;
    transition: all 0.3s;
    border-color: var(--border) !important;
    background-color: var(--surface) !important;
    background: var(--surface) !important;

    &:hover {
      border-color: var(--color-primary) !important;
    }
  }

  :deep(.el-input.is-focus .el-input__wrapper) {
    border-color: var(--color-primary) !important;
    box-shadow: 0 0 0 2px rgba(30, 58, 138, 0.2);
  }
  
  :deep(.el-input__inner) {
    color: var(--text) !important;
    background-color: transparent !important;
  }
  
  /* 日期范围选择器的分隔符和图标 */
  :deep(.el-range-separator) {
    color: var(--text-2) !important;
  }
  
  :deep(.el-range__icon) {
    color: var(--text-2) !important;
  }
  
  :deep(.el-input__prefix),
  :deep(.el-input__suffix) {
    color: var(--text-2) !important;
  }
}

/* 注意：日历弹窗样式已在 design-system.scss 中全局定义，这里不需要重复定义 */
</style>

