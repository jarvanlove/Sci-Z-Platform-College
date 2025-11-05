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
      共 {{ total }} 条记录
    </div>

    <!-- 每页显示数量选择器 -->
    <div v-if="showSizeSelector" class="base-pagination__size-selector">
      每页显示
      <el-select
        v-model="pageSize"
        :disabled="disabled"
        @change="handleSizeChange"
        class="base-pagination__size-select"
      >
        <el-option
          v-for="size in pageSizes"
          :key="size"
          :label="`${size} 条`"
          :value="size"
        />
      </el-select>
    </div>

    <!-- 分页控制器 -->
    <div class="base-pagination__controls">
      <!-- 上一页按钮 -->
      <el-button
        :disabled="currentPage <= 1 || disabled"
        @click="handlePrevPage"
        class="base-pagination__btn"
      >
        上一页
      </el-button>

      <!-- 页码按钮 -->
      <div class="base-pagination__pages">
        <el-button
          v-for="page in visiblePages"
          :key="page"
          :type="page === currentPage ? 'primary' : 'default'"
          :disabled="disabled"
          @click="handlePageChange(page)"
          class="base-pagination__page-btn"
          :class="{ 'base-pagination__page-btn--active': page === currentPage }"
        >
          {{ page }}
        </el-button>
      </div>

      <!-- 下一页按钮 -->
      <el-button
        :disabled="currentPage >= totalPages || disabled"
        @click="handleNextPage"
        class="base-pagination__btn"
      >
        下一页
      </el-button>
    </div>

    <!-- 快速跳转 -->
    <div v-if="showJumper" class="base-pagination__jumper">
      跳至
      <el-input-number
        v-model="jumpPage"
        :min="1"
        :max="totalPages"
        :disabled="disabled"
        @change="handleJump"
        class="base-pagination__jumper-input"
      />
      页
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

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

    if (end - start + 1 < maxVisible) {
      start = Math.max(1, end - maxVisible + 1)
    }

    for (let i = start; i <= end; i++) {
      pages.push(i)
    }
  }

  return pages
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

const handleSizeChange = (size) => {
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

const handleJump = (page) => {
  if (page && page >= 1 && page <= totalPages.value) {
    handlePageChange(page)
  }
}
</script>

<style lang="scss" scoped>
.base-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--gap-lg);
  padding: var(--gap-lg);
  background: var(--surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);

  &__info {
    font-size: var(--font-size-sm);
    color: var(--text-2);
    white-space: nowrap;
  }

  &__size-selector {
    display: flex;
    align-items: center;
    gap: var(--gap-sm);
    font-size: var(--font-size-sm);
    color: var(--text-2);
  }

  &__size-select {
    width: 80px;
  }

  &__controls {
    display: flex;
    align-items: center;
    gap: var(--gap-sm);
  }

  &__btn {
    min-width: 32px;
    height: 32px;
    padding: 0 var(--gap-sm);
    font-size: var(--font-size-sm);
  }

  &__pages {
    display: flex;
    align-items: center;
    gap: var(--gap-xs);
  }

  &__page-btn {
    min-width: 32px;
    height: 32px;
    padding: 0 var(--gap-sm);
    font-size: var(--font-size-sm);
    border-radius: var(--radius-sm);

    &--active {
      background: var(--color-primary);
      border-color: var(--color-primary);
      color: var(--surface);
    }
  }

  &__jumper {
    display: flex;
    align-items: center;
    gap: var(--gap-sm);
    font-size: var(--font-size-sm);
    color: var(--text-2);
  }

  &__jumper-input {
    width: 60px;
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
