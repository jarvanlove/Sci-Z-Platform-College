<!--
/**
 * @description 基础分页组件
 * 统一的分页样式和功能，支持页码跳转、每页显示数量选择
 * 可在所有列表页面中复用，确保分页风格统一
 */
-->
<template>
  <div class="base-pagination">
    <!-- 分页信息 -->
    <div v-if="showInfo" class="base-pagination__info">
      {{ t('pagination.totalRecords', { total }) }}
    </div>

    <!-- 每页显示数量选择器 -->
    <div v-if="showSizeSelector" class="base-pagination__size-selector">
      {{ t('pagination.pageSizeLabel') }}
      <select
        :value="pageSize"
        :disabled="disabled"
        @change="handleSizeChange"
        class="base-pagination__size-select"
      >
        <option
          v-for="size in pageSizes"
          :key="size"
          :value="size"
        >
          {{ t('pagination.pageSize', { size }) }}
        </option>
      </select>
    </div>

    <!-- 分页控制器 -->
    <div class="base-pagination__controls">
      <!-- 上一页按钮 -->
      <button
        :disabled="currentPage <= 1 || disabled"
        @click="handlePrevPage"
        class="base-pagination__btn"
        :class="{ 'base-pagination__btn--disabled': currentPage <= 1 || disabled }"
      >
        {{ t('pagination.prevPage') }}
      </button>

      <!-- 页码按钮 -->
      <div class="base-pagination__pages">
        <!-- 第一页 -->
        <button
          v-if="showFirstPage"
          :disabled="disabled"
          @click="handlePageChange(1)"
          class="base-pagination__page-btn"
          :class="{ 'base-pagination__page-btn--active': currentPage === 1 }"
        >
          1
        </button>

        <!-- 前省略号 -->
        <span v-if="showPrevEllipsis" class="base-pagination__ellipsis">...</span>

        <!-- 中间页码 -->
        <button
          v-for="page in visiblePages"
          :key="page"
          :disabled="disabled"
          @click="handlePageChange(page)"
          class="base-pagination__page-btn"
          :class="{ 'base-pagination__page-btn--active': page === currentPage }"
        >
          {{ page }}
        </button>

        <!-- 后省略号 -->
        <span v-if="showNextEllipsis" class="base-pagination__ellipsis">...</span>

        <!-- 最后一页 -->
        <button
          v-if="showLastPage"
          :disabled="disabled"
          @click="handlePageChange(totalPages)"
          class="base-pagination__page-btn"
          :class="{ 'base-pagination__page-btn--active': currentPage === totalPages }"
        >
          {{ totalPages }}
        </button>
      </div>

      <!-- 下一页按钮 -->
      <button
        :disabled="currentPage >= totalPages || disabled"
        @click="handleNextPage"
        class="base-pagination__btn"
        :class="{ 'base-pagination__btn--disabled': currentPage >= totalPages || disabled }"
      >
        {{ t('pagination.nextPage') }}
      </button>
    </div>

    <!-- 快速跳转 -->
    <div v-if="showJumper" class="base-pagination__jumper">
      {{ t('pagination.jumpTo') }}
      <input
        v-model.number="jumpPage"
        type="number"
        :min="1"
        :max="totalPages"
        :disabled="disabled"
        @keyup.enter="handleJumpEnter"
        class="base-pagination__jumper-input"
      />
      {{ t('pagination.pageUnit') }}
      <button
        class="base-pagination__jump-btn"
        :disabled="disabled"
        @click="handleJumpEnter"
      >
        {{ t('common.confirm') }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

// Props 定义
const props = defineProps({
  current: {
    type: Number,
    default: 1
  },
  size: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  showInfo: {
    type: Boolean,
    default: true
  },
  showSizeSelector: {
    type: Boolean,
    default: true
  },
  showJumper: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  },
  maxVisiblePages: {
    type: Number,
    default: 7
  }
})

// Emits 定义
const emit = defineEmits(['change', 'size-change'])

// 响应式数据
const currentPage = ref(props.current)
const pageSize = ref(props.size)
const jumpPage = ref(props.current)

// 计算属性
const totalPages = computed(() => Math.ceil(props.total / pageSize.value))

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  const maxVisible = props.maxVisiblePages

  if (total <= maxVisible) {
    // 总页数小于等于最大显示页数，显示所有页
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    // 总页数大于最大显示页数，需要省略号
    const half = Math.floor(maxVisible / 2)
    let start = Math.max(1, current - half)
    let end = Math.min(total, start + maxVisible - 1)

    // 调整起始位置
    if (end - start + 1 < maxVisible) {
      start = Math.max(1, end - maxVisible + 1)
    }

    for (let i = start; i <= end; i++) {
      pages.push(i)
    }
  }

  return pages
})

// 是否显示第一页
const showFirstPage = computed(() => {
  return visiblePages.value.length > 0 && visiblePages.value[0] > 1
})

// 是否显示前省略号
const showPrevEllipsis = computed(() => {
  return visiblePages.value.length > 0 && visiblePages.value[0] > 2
})

// 是否显示最后一页
const showLastPage = computed(() => {
  if (visiblePages.value.length === 0) return false
  const lastVisiblePage = visiblePages.value[visiblePages.value.length - 1]
  return lastVisiblePage < totalPages.value
})

// 是否显示后省略号
const showNextEllipsis = computed(() => {
  if (visiblePages.value.length === 0) return false
  const lastVisiblePage = visiblePages.value[visiblePages.value.length - 1]
  return lastVisiblePage < totalPages.value - 1
})

// 监听器
watch(() => props.current, (newVal) => {
  currentPage.value = newVal
  jumpPage.value = newVal
})

watch(() => props.size, (newVal) => {
  pageSize.value = newVal
})

// 方法
const handlePageChange = (page) => {
  if (page !== currentPage.value && page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    jumpPage.value = page
    emit('change', page)
  }
}

const handleSizeChange = (event) => {
  // 确保从 event.target.value 获取值并转换为数字
  const size = Number(event.target.value)
  if (isNaN(size) || size <= 0) {
    return
  }
  pageSize.value = size
  currentPage.value = 1
  jumpPage.value = 1
  emit('size-change', size)
  emit('change', 1)
}

const handlePrevPage = () => {
  if (currentPage.value > 1) {
    handlePageChange(currentPage.value - 1)
  }
}

const handleNextPage = () => {
  if (currentPage.value < totalPages.value) {
    handlePageChange(currentPage.value + 1)
  }
}

const handleJumpEnter = () => {
  const page = parseInt(jumpPage.value)
  if (page && page >= 1 && page <= totalPages.value) {
    handlePageChange(page)
  } else {
    // 重置为当前页
    jumpPage.value = currentPage.value
  }
}
</script>

<style lang="scss" scoped>
.base-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 16px 0;
  background: var(--surface);
  border-radius: 6px;

  &__info {
    font-size: 14px;
    color: var(--text-2);
    white-space: nowrap;
  }

  &__size-selector {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: var(--text-2);
  }

  &__size-select {
    width: auto;
    min-width: 80px;
    margin: 0 8px;
    padding: 4px 8px;
    border: 1px solid var(--border);
    border-radius: 4px;
    font-size: 14px;
    color: var(--text-2);
    background-color: var(--surface);
    cursor: pointer;
    transition: all 0.3s;

    &:hover:not(:disabled) {
      border-color: var(--color-primary);
    }

    &:focus {
      outline: none;
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(30, 58, 138, 0.2);
    }

    &:disabled {
      color: var(--disabled);
      background-color: var(--hover);
      cursor: not-allowed;
    }
  }

  &__controls {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__btn {
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 32px;
    height: 32px;
    padding: 0 8px;
    border: 1px solid var(--border);
    border-radius: 4px;
    font-size: 14px;
    color: var(--text-2);
    background-color: var(--surface);
    cursor: pointer;
    transition: all 0.3s;
    text-decoration: none;

    &:hover:not(:disabled):not(.base-pagination__btn--disabled) {
      color: var(--color-primary);
      border-color: var(--color-primary);
      background-color: var(--hover);
    }

    &--disabled,
    &:disabled {
      color: var(--disabled);
      background-color: var(--surface);
      border-color: var(--border);
      cursor: not-allowed;

      &:hover {
        color: var(--disabled);
        border-color: var(--border);
        background-color: var(--surface);
      }
    }
  }

  &__pages {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  &__page-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 32px;
    height: 32px;
    padding: 0 8px;
    border: 1px solid var(--border);
    border-radius: 4px;
    font-size: 14px;
    color: var(--text-2);
    background-color: var(--surface);
    cursor: pointer;
    transition: all 0.3s;
    text-decoration: none;

    &:hover:not(:disabled):not(.base-pagination__page-btn--active) {
      color: var(--color-primary);
      border-color: var(--color-primary);
      background-color: var(--hover);
    }

    &--active {
      color: var(--surface);
      background-color: var(--color-primary);
      border-color: var(--color-primary);

      &:hover {
        color: var(--surface);
        background-color: var(--color-primary);
        border-color: var(--color-primary);
      }
    }

    &:disabled {
      color: var(--disabled);
      background-color: var(--surface);
      border-color: var(--border);
      cursor: not-allowed;

      &:hover {
        color: var(--disabled);
        border-color: var(--border);
        background-color: var(--surface);
      }
    }
  }

  &__ellipsis {
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 32px;
    height: 32px;
    color: var(--disabled);
    font-size: var(--font-size-sm);
  }

  &__jumper {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: 20px;
    font-size: 14px;
    color: var(--text-2);
  }

  &__jumper-input {
    width: 50px;
    height: 32px;
    margin: 0 8px;
    padding: 0 8px;
    border: 1px solid var(--border);
    border-radius: 4px;
    font-size: 14px;
    text-align: center;
    color: var(--text-2);
    background-color: var(--surface);
    transition: all 0.3s;

    &:hover {
      border-color: var(--color-primary);
    }

    &:focus {
      outline: none;
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(30, 58, 138, 0.2);
    }
  }

  &__jump-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 32px;
    height: 32px;
    padding: 0 8px;
    margin-left: 8px;
    border: 1px solid var(--border);
    border-radius: 4px;
    font-size: 14px;
    color: var(--text-2);
    background-color: var(--surface);
    cursor: pointer;
    transition: all 0.3s;

    &:hover:not(:disabled) {
      color: var(--color-primary);
      border-color: var(--color-primary);
      background-color: var(--hover);
    }

    &:disabled {
      color: var(--disabled);
      background-color: var(--surface);
      border-color: var(--border);
      cursor: not-allowed;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .base-pagination {
    flex-direction: column;
    gap: var(--gap-md);

    &__controls {
      order: -1;
    }
  }
}
</style>
