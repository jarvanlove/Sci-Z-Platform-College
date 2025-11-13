/**
 * 通用组件统一导出
 * 提供统一的组件导入入口，便于在业务页面中使用
 */

// 基础组件
export { default as BaseButton } from './BaseButton.vue'
export { default as BaseCard } from './BaseCard.vue'
export { default as BaseDialog } from './BaseDialog.vue'
export { default as BaseTable } from './BaseTable.vue'
export { default as BasePagination } from './BasePagination.vue'
export { default as BaseScrollbar } from './BaseScrollbar.vue'
export { default as BaseDatePicker } from './BaseDatePicker.vue'
export { default as BaseSwitch } from './BaseSwitch.vue'

// 功能组件
export { default as LanguageSwitcher } from './LanguageSwitcher.vue'

// 业务组件
export * from '../Business'
