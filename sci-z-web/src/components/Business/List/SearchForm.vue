<!--
/**
 * @description 搜索表单组件
 * 用于列表页面的搜索功能，支持多种表单控件类型
 * 包含搜索、重置、高级搜索等功能
 */
-->
<template>
  <BaseCard class="search-form-card">
    <el-form
      ref="searchFormRef"
      :model="searchForm"
      :inline="inline"
      :label-width="labelWidth"
      class="search-form"
    >
      <el-form-item
        v-for="field in fields"
        :key="field.prop"
        :label="field.label"
        :prop="field.prop"
        :class="field.className"
      >
        <!-- 输入框 -->
        <el-input
          v-if="field.type === 'input'"
          v-model="searchForm[field.prop]"
          :placeholder="field.placeholder || `请输入${field.label}`"
          :clearable="field.clearable !== false"
          :style="{ width: field.width || '200px' }"
        />
        
        <!-- 选择器 -->
        <el-select
          v-else-if="field.type === 'select'"
          v-model="searchForm[field.prop]"
          :placeholder="field.placeholder || `请选择${field.label}`"
          :clearable="field.clearable !== false"
          :style="{ width: field.width || '200px' }"
        >
          <el-option
            v-for="option in field.options"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        
        <!-- 日期选择器 -->
        <el-date-picker
          v-else-if="field.type === 'date'"
          v-model="searchForm[field.prop]"
          type="date"
          :placeholder="field.placeholder || `请选择${field.label}`"
          :clearable="field.clearable !== false"
          :style="{ width: field.width || '200px' }"
        />
        
        <!-- 日期范围选择器 -->
        <el-date-picker
          v-else-if="field.type === 'daterange'"
          v-model="searchForm[field.prop]"
          type="daterange"
          :start-placeholder="field.startPlaceholder || '开始日期'"
          :end-placeholder="field.endPlaceholder || '结束日期'"
          :clearable="field.clearable !== false"
          :style="{ width: field.width || '240px' }"
        />
        
        <!-- 自定义插槽 -->
        <slot
          v-else-if="field.type === 'slot'"
          :name="field.slotName"
          :field="field"
          :value="searchForm[field.prop]"
          :update="(value) => updateField(field.prop, value)"
        />
      </el-form-item>
      
      <!-- 操作按钮 -->
      <el-form-item class="search-actions">
        <BaseButton type="primary" :loading="loading" @click="handleSearch">
          <el-icon><Search /></el-icon>
          查询
        </BaseButton>
        <BaseButton @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </BaseButton>
        <BaseButton
          v-if="showAdvanced"
          type="text"
          @click="toggleAdvanced"
        >
          {{ showAdvancedForm ? '收起' : '展开' }}
          <el-icon>
            <ArrowDown v-if="!showAdvancedForm" />
            <ArrowUp v-else />
          </el-icon>
        </BaseButton>
      </el-form-item>
    </el-form>
    
    <!-- 高级搜索表单 -->
    <el-collapse-transition>
      <div v-show="showAdvancedForm" class="advanced-search">
        <el-divider>高级搜索</el-divider>
        <el-form
          ref="advancedFormRef"
          :model="advancedForm"
          :inline="inline"
          :label-width="labelWidth"
          class="advanced-form"
        >
          <el-form-item
            v-for="field in advancedFields"
            :key="field.prop"
            :label="field.label"
            :prop="field.prop"
            :class="field.className"
          >
            <!-- 复用基础字段的渲染逻辑 -->
            <el-input
              v-if="field.type === 'input'"
              v-model="advancedForm[field.prop]"
              :placeholder="field.placeholder || `请输入${field.label}`"
              :clearable="field.clearable !== false"
              :style="{ width: field.width || '200px' }"
            />
            <el-select
              v-else-if="field.type === 'select'"
              v-model="advancedForm[field.prop]"
              :placeholder="field.placeholder || `请选择${field.label}`"
              :clearable="field.clearable !== false"
              :style="{ width: field.width || '200px' }"
            >
              <el-option
                v-for="option in field.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            <slot
              v-else-if="field.type === 'slot'"
              :name="`advanced-${field.slotName}`"
              :field="field"
              :value="advancedForm[field.prop]"
              :update="(value) => updateAdvancedField(field.prop, value)"
            />
          </el-form-item>
        </el-form>
      </div>
    </el-collapse-transition>
  </BaseCard>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { BaseButton, BaseCard } from '@/components/Common'
import { Search, Refresh, ArrowDown, ArrowUp } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 搜索字段配置
  fields: {
    type: Array,
    default: () => []
  },
  // 高级搜索字段配置
  advancedFields: {
    type: Array,
    default: () => []
  },
  // 是否显示高级搜索
  showAdvanced: {
    type: Boolean,
    default: false
  },
  // 是否内联布局
  inline: {
    type: Boolean,
    default: true
  },
  // 标签宽度
  labelWidth: {
    type: String,
    default: '80px'
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 初始搜索值
  initialValues: {
    type: Object,
    default: () => ({})
  }
})

// Emits
const emit = defineEmits(['search', 'reset', 'advanced-search'])

// Refs
const searchFormRef = ref()
const advancedFormRef = ref()

// 搜索表单数据
const searchForm = reactive({})
const advancedForm = reactive({})

// 高级搜索展开状态
const showAdvancedForm = ref(false)

// 初始化表单数据
const initFormData = () => {
  // 初始化基础搜索表单
  props.fields.forEach(field => {
    if (!(field.prop in searchForm)) {
      searchForm[field.prop] = props.initialValues[field.prop] || field.defaultValue || ''
    }
  })
  
  // 初始化高级搜索表单
  props.advancedFields.forEach(field => {
    if (!(field.prop in advancedForm)) {
      advancedForm[field.prop] = props.initialValues[field.prop] || field.defaultValue || ''
    }
  })
}

// 更新字段值
const updateField = (prop, value) => {
  searchForm[prop] = value
}

// 更新高级搜索字段值
const updateAdvancedField = (prop, value) => {
  advancedForm[prop] = value
}

// 切换高级搜索
const toggleAdvanced = () => {
  showAdvancedForm.value = !showAdvancedForm.value
}

// 处理搜索
const handleSearch = () => {
  const searchData = { ...searchForm }
  if (showAdvancedForm.value) {
    Object.assign(searchData, advancedForm)
  }
  emit('search', searchData)
}

// 处理重置
const handleReset = () => {
  // 重置基础表单
  searchFormRef.value?.resetFields()
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = props.initialValues[key] || ''
  })
  
  // 重置高级表单
  if (showAdvancedForm.value) {
    advancedFormRef.value?.resetFields()
    Object.keys(advancedForm).forEach(key => {
      advancedForm[key] = props.initialValues[key] || ''
    })
  }
  
  emit('reset')
}

// 监听字段变化，重新初始化表单数据
watch(() => props.fields, initFormData, { immediate: true, deep: true })
watch(() => props.advancedFields, initFormData, { immediate: true, deep: true })
watch(() => props.initialValues, initFormData, { immediate: true, deep: true })

// 暴露方法给父组件
defineExpose({
  searchForm,
  advancedForm,
  handleSearch,
  handleReset,
  resetForm: handleReset
})
</script>

<style lang="scss" scoped>
.search-form-card {
  margin-bottom: var(--gap-lg);
  
  .search-form {
    .search-actions {
      margin-left: var(--gap-md);
    }
  }
  
  .advanced-search {
    margin-top: var(--gap-lg);
    
    .advanced-form {
      margin-top: var(--gap-md);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .search-form {
    .el-form-item {
      margin-bottom: var(--gap-md);
    }
    
    .search-actions {
      margin-left: 0;
      margin-top: var(--gap-md);
    }
  }
}
</style>
