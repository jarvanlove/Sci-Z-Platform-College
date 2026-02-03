<!--
/**
 * @description 知识库内容列表组件
 * 展示知识库中的文件和文件夹，支持分页、搜索、操作等功能
 */
-->
<template>
  <div class="knowledge-content-list">
    <!-- 搜索框 -->
    <div
      v-if="showSearch"
      class="search-input-wrap"
      style="margin: 0 0 12px 0"
    >
      <span class="search-left-icon">
        <el-icon><Search /></el-icon>
      </span>
      <input
        class="form-input"
        :value="localSearchQuery"
        @input="handleSearchInput"
        :placeholder="$t('knowledge.searchInKb')"
        style="width: 100%; padding: 10px 40px 10px 36px"
      />
      <div
        class="search-dismiss"
        :title="$t('knowledge.closeSearch')"
        @click="$emit('close-search')"
      >
        <el-icon><Close /></el-icon>
      </div>
    </div>

    <!-- 内容列表 -->
    <div class="content-list">
      <!-- 文件项 -->
      <div
        v-for="item in fileItems"
        :key="`file-${item.id}`"
        class="content-item"
      >
        <div class="content-item-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="content-item-info">
          <div class="content-item-name" :title="item.name">{{ truncateFileName(item.name, 20) }}</div>
          <div class="content-item-meta">
            <span>{{ item.ext }}</span>
            <span>|</span>
            <span>{{ item.time }}</span>
          </div>
        </div>
        <div class="content-item-actions">
          <button
            class="action-icon"
            :title="$t('common.preview')"
            @click.stop="$emit('preview-file', item)"
          >
            <el-icon><View /></el-icon>
          </button>
          <button
            class="action-icon"
            :title="$t('knowledge.renameTitle')"
            @click.stop="$emit('rename-item', item)"
          >
            <el-icon><Edit /></el-icon>
          </button>
          <button
            class="action-icon"
            :title="$t('common.delete')"
            @click.stop="$emit('delete-item', item)"
          >
            <el-icon><Delete /></el-icon>
          </button>
          <el-dropdown
            class="content-actions-dropdown"
            trigger="click"
            @command="(cmd) => $emit('content-action', cmd, item)"
          >
            <button class="action-icon more-action" :title="$t('knowledge.moreActions')">
              <el-icon><MoreFilled /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="preview">
                  {{ $t('common.preview') }}
                </el-dropdown-item>
                <el-dropdown-item command="rename">
                  {{ $t('knowledge.renameTitle') }}
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  {{ $t('common.delete') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 文件夹项 -->
      <div
        v-for="item in folderItems"
        :key="`folder-${item.id}`"
        class="content-item"
        @click="$emit('enter-folder', item)"
      >
        <div class="content-item-icon folder-icon">
          <el-icon><Folder /></el-icon>
        </div>
        <div class="content-item-info">
          <div class="content-item-name">{{ item.name }}</div>
          <div class="content-item-meta">
            <span>{{ (item.files || []).length }} {{ $t('knowledge.fileCount') }}</span>
          </div>
        </div>
        <div class="content-item-actions">
          <span
            v-if="(item.files || []).length > 2"
            style="color: #9ca3af"
          >▼</span>
          <button
            class="action-icon"
            :title="$t('knowledge.renameTitle')"
            @click.stop="$emit('rename-item', item)"
          >
            <el-icon><Edit /></el-icon>
          </button>
          <button
            class="action-icon"
            :title="$t('common.delete')"
            @click.stop="$emit('delete-item', item)"
          >
            <el-icon><Delete /></el-icon>
          </button>
          <el-dropdown
            class="content-actions-dropdown"
            trigger="click"
            @command="(cmd) => $emit('content-action', cmd, item)"
          >
            <button class="action-icon more-action" :title="$t('knowledge.moreActions')">
              <el-icon><MoreFilled /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="rename">
                  {{ $t('knowledge.renameTitle') }}
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  {{ $t('common.delete') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div
      v-if="currentFolder && displayItems.length === 0"
      class="content-empty"
    >
      <div>{{ $t('knowledge.emptyFolder') }}</div>
    </div>
    <div
      v-else-if="!currentFolder && displayItems.length === 0"
      class="content-empty"
    >
      {{ $t('knowledge.noMoreContent') }}
    </div>

    <!-- 分页组件 -->
    <div v-if="pagination.total > 0" class="pagination-wrapper">
      <el-pagination
        :current-page="pagination.currentPage"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  Document,
  Folder,
  View,
  Edit,
  Delete,
  MoreFilled,
  Search,
  Close
} from '@element-plus/icons-vue'

const props = defineProps({
  items: {
    type: Array,
    default: () => []
  },
  searchQuery: {
    type: String,
    default: ''
  },
  showSearch: {
    type: Boolean,
    default: false
  },
  currentFolder: {
    type: Object,
    default: null
  },
  pagination: {
    type: Object,
    default: () => ({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
  }
})

const emit = defineEmits([
  'close-search',
  'update:searchQuery',
  'preview-file',
  'rename-item',
  'delete-item',
  'enter-folder',
  'content-action',
  'page-change',
  'page-size-change'
])

const { t } = useI18n()

// 本地搜索查询（用于 v-model）
const localSearchQuery = ref(props.searchQuery)

// 监听 prop 变化，同步到本地
watch(() => props.searchQuery, (newVal) => {
  localSearchQuery.value = newVal
})

// 处理搜索输入
const handleSearchInput = (event) => {
  localSearchQuery.value = event.target.value
  emit('update:searchQuery', event.target.value)
}

// 计算显示项（支持搜索过滤）
const displayItems = computed(() => {
  const q = localSearchQuery.value.trim().toLowerCase()
  if (!q) return props.items
  return props.items.filter((it) =>
    (it.name || '').toLowerCase().includes(q)
  )
})

// 计算文件项列表
const fileItems = computed(() => {
  return displayItems.value.filter(item => item.type === 'file')
})

// 计算文件夹项列表
const folderItems = computed(() => {
  return displayItems.value.filter(item => item.type !== 'file')
})

// 截断文件名
const truncateFileName = (name, maxLength) => {
  if (!name) return ''
  if (name.length <= maxLength) return name
  return name.substring(0, maxLength) + '...'
}

// 分页处理
const handlePageChange = (page) => {
  emit('page-change', page)
}

const handlePageSizeChange = (size) => {
  emit('page-size-change', size)
}
</script>

<style scoped lang="scss">
.knowledge-content-list {
  width: 100%;
  height: 100%; // 占满父容器
  display: flex;
  flex-direction: column;
  min-height: 0; // 允许 flex 子元素正确收缩
  overflow: hidden; // 容器不滚动
}

.search-input-wrap {
  position: relative;
}

.search-left-icon {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.search-dismiss {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #6b7280;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s ease;

  &:hover {
    background: #374151;
  }
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  color: #111827;
  outline: none;
  transition: all 0.2s ease;

  &:focus {
    border-color: #0ea5e9;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
  }
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  min-height: 0; // 允许 flex 子元素正确收缩
  overflow-y: auto; // 内容过多时滚动
  overflow-x: hidden; // 隐藏横向滚动
}

.content-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #f9fafb;
    border-color: #0ea5e9;
  }
}

.content-item-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  border-radius: 8px;
  margin-right: 12px;
  color: #6b7280;
  font-size: 20px;

  &.folder-icon {
    color: #f59e0b;
  }
}

.content-item-info {
  flex: 1;
  min-width: 0;
}

.content-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-item-meta {
  font-size: 12px;
  color: #6b7280;
  display: flex;
  gap: 8px;
}

.content-item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s ease;

  &:hover {
    background: #f3f4f6;
    color: #111827;
  }
}

.content-empty {
  padding: 60px 20px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
  flex-shrink: 0; // 防止被压缩
}

.pagination-wrapper {
  padding: 20px 24px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #e5e7eb;
  margin-top: 16px;
  flex-shrink: 0; // 防止被压缩
  box-sizing: border-box; // 包含 padding
}
</style>

