<!--
/**
 * @description 文献搜索页面
 * 搜索和展示文献列表，支持分页、筛选等功能
 */
-->
<template>
  <div class="literature-search-container">
    <!-- 顶部搜索栏 -->
    <div class="search-header">
      <div class="search-bar-wrapper">
        <el-input
          v-model="searchForm.search"
          placeholder="搜索文献..."
          clearable
          size="large"
          class="main-search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      
      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-input
          v-model="searchForm.publicationYearFilter"
          placeholder="发表年份，如：2023-2025"
          clearable
          style="width: 200px"
          size="small"
        />
        <el-select
          v-model="searchForm.dataSource"
          placeholder="数据来源"
          style="width: 150px"
          size="small"
        >
          <el-option label="OpenAlex" value="openalex" />
        </el-select>
        <el-button type="primary" :loading="loading" size="small" @click="handleSearch">
          搜索
        </el-button>
        <el-button size="small" @click="handleReset">
          重置
        </el-button>
      </div>
    </div>

    <!-- 结果统计 -->
    <div v-if="hasSearched && !loading" class="result-header">
      <span class="result-count">论文共 {{ pagination.total }} 篇</span>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>搜索中...</span>
    </div>

    <!-- 文献列表 -->
    <div v-if="!loading && literatureList.length > 0" class="literature-list">
      <div
        v-for="literature in literatureList"
        :key="literature.paperInfo?.id || literature.paperInfo?.globalId"
        class="literature-card"
      >
        <!-- 标题区域 -->
        <div class="card-title-section">
          <h2 class="literature-title" @click="handleViewDetail(literature)">
            {{ literature.paperInfo?.title || '' }}
          </h2>
          <div v-if="literature.paperInfo?.titleTranslated" class="title-translated">
            {{ literature.paperInfo.titleTranslated }}
          </div>
          <div class="title-badges">
            <el-tag v-if="literature.accessInfo?.isOpenAccess" type="success" size="small" class="oa-badge">
              <el-icon><CircleCheck /></el-icon>
              Open Access
            </el-tag>
            <el-tag v-if="literature.accessInfo?.pdfLink" type="info" size="small" class="fulltext-badge">
              全文可用
            </el-tag>
          </div>
        </div>

        <!-- 作者区域 -->
        <div v-if="literature.authorsInfo && literature.authorsInfo.length > 0" class="card-authors">
          <div
            v-for="(author, index) in getDisplayAuthors(literature.authorsInfo)"
            :key="index"
            class="author-item"
          >
            <div class="author-avatar">{{ getAuthorInitial(author.name) }}</div>
            <span class="author-name">{{ author.name }}</span>
            <el-tag v-if="author.isCorresponding" type="success" size="small" style="margin-left: 4px">通讯</el-tag>
          </div>
          <span v-if="literature.authorsInfo.length > 3" class="more-authors">
            +{{ literature.authorsInfo.length - 3 }}
          </span>
        </div>

        <!-- 发表信息 -->
        <div class="card-publication">
          <span v-if="literature.paperInfo?.publicationDate" class="pub-date">{{ formatDate(literature.paperInfo.publicationDate) }}</span>
          <span v-if="literature.paperInfo?.publicationYear && literature.sourceInfo" class="divider">|</span>
          <span v-if="literature.sourceInfo" class="pub-source">
            {{ literature.paperInfo?.publicationYear }} {{ literature.sourceInfo.journalName }}
          </span>
        </div>

        <!-- 摘要区域 -->
        <div v-if="literature.paperInfo?.abstractText" class="card-abstract">
          <div class="abstract-label">摘要</div>
          <div class="abstract-content" :class="{ 'expanded': expandedAbstracts[literature.paperInfo?.id] }">
            <p>{{ expandedAbstracts[literature.paperInfo?.id] ? literature.paperInfo.abstractText : truncateText(literature.paperInfo.abstractText, 150) }}</p>
            <span
              v-if="literature.paperInfo.abstractText.length > 150"
              class="expand-toggle"
              @click.stop="toggleAbstract(literature.paperInfo.id)"
            >
              {{ expandedAbstracts[literature.paperInfo.id] ? '收起' : '展开' }}
            </span>
          </div>
          <div v-if="literature.paperInfo?.abstractTranslated" class="abstract-translated">
            <div class="abstract-label">摘要（中文）</div>
            <div class="abstract-content" :class="{ 'expanded': expandedAbstracts[literature.paperInfo?.id + '_translated'] }">
              <p>{{ expandedAbstracts[literature.paperInfo?.id + '_translated'] ? literature.paperInfo.abstractTranslated : truncateText(literature.paperInfo.abstractTranslated, 150) }}</p>
              <span
                v-if="literature.paperInfo.abstractTranslated.length > 150"
                class="expand-toggle"
                @click.stop="toggleAbstract(literature.paperInfo.id + '_translated')"
              >
                {{ expandedAbstracts[literature.paperInfo.id + '_translated'] ? '收起' : '展开' }}
              </span>
            </div>
          </div>
        </div>

        <!-- 分类信息 -->
        <div v-if="literature.taxonomy" class="card-topic">
          <el-tag v-if="literature.taxonomy.topic" size="small" type="primary">{{ literature.taxonomy.topic }}</el-tag>
          <el-tag v-if="literature.taxonomy.primaryField" size="small" type="info" style="margin-left: 4px">{{ literature.taxonomy.primaryField }}</el-tag>
        </div>

        <!-- 底部操作栏 -->
        <div class="card-footer">
          <div class="footer-stats">
            <span v-if="literature.impactMetrics?.citationCount !== null" class="stat-item citation-stat">
              <el-icon><DocumentCopy /></el-icon>
              被引 {{ literature.impactMetrics.citationCount }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && literatureList.length === 0 && hasSearched" class="empty-state">
      <el-empty description="暂无搜索结果，请尝试其他关键词" />
    </div>

    <!-- 分页 -->
    <div v-if="!loading && pagination.total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  Loading,
  DocumentCopy,
  CircleCheck
} from '@element-plus/icons-vue'
import { searchLiterature } from '@/api/Literature/literature'
import { createLogger } from '@/utils/simpleLogger'

const router = useRouter()
const logger = createLogger('LiteratureSearch')

// 缓存 key
const CACHE_KEY = 'literature_search_cache'

// 响应式数据
const loading = ref(false)
const hasSearched = ref(false)
const literatureList = ref([])
const expandedAbstracts = ref({})
const searchForm = reactive({
  search: '',
  publicationYearFilter: '',
  dataSource: 'openalex',
  perPage: 10,
  page: 1
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
  pages: 0
})

// 生成缓存 key（基于搜索参数）
const generateCacheKey = () => {
  const params = {
    search: searchForm.search.trim(),
    publicationYearFilter: searchForm.publicationYearFilter || '',
    dataSource: searchForm.dataSource,
    perPage: pagination.size,
    page: pagination.current
  }
  return `${CACHE_KEY}_${JSON.stringify(params)}`
}

// 保存搜索结果到缓存
const saveSearchCache = () => {
  try {
    const cacheData = {
      searchForm: {
        search: searchForm.search,
        publicationYearFilter: searchForm.publicationYearFilter,
        dataSource: searchForm.dataSource,
        perPage: searchForm.perPage,
        page: searchForm.page
      },
      pagination: {
        current: pagination.current,
        size: pagination.size,
        total: pagination.total,
        pages: pagination.pages
      },
      literatureList: literatureList.value,
      hasSearched: hasSearched.value,
      timestamp: Date.now()
    }
    sessionStorage.setItem(CACHE_KEY, JSON.stringify(cacheData))
    logger.info('搜索结果已缓存', { 
      count: literatureList.value.length,
      total: pagination.total 
    })
  } catch (error) {
    logger.error('保存搜索结果缓存失败', error)
  }
}

// 从缓存恢复搜索结果
const restoreSearchCache = () => {
  try {
    const cachedData = sessionStorage.getItem(CACHE_KEY)
    if (!cachedData) {
      logger.info('未找到搜索结果缓存')
      return false
    }

    const cache = JSON.parse(cachedData)
    
    // 检查缓存是否过期（30分钟）
    const cacheAge = Date.now() - (cache.timestamp || 0)
    const maxAge = 30 * 60 * 1000 // 30分钟
    if (cacheAge > maxAge) {
      logger.info('搜索结果缓存已过期，清除缓存')
      sessionStorage.removeItem(CACHE_KEY)
      return false
    }

    // 恢复搜索表单
    if (cache.searchForm) {
      searchForm.search = cache.searchForm.search || ''
      searchForm.publicationYearFilter = cache.searchForm.publicationYearFilter || ''
      searchForm.dataSource = cache.searchForm.dataSource || 'openalex'
      searchForm.perPage = cache.searchForm.perPage || 10
      searchForm.page = cache.searchForm.page || 1
    }

    // 恢复分页信息
    if (cache.pagination) {
      pagination.current = cache.pagination.current || 1
      pagination.size = cache.pagination.size || 10
      pagination.total = cache.pagination.total || 0
      pagination.pages = cache.pagination.pages || 0
    }

    // 恢复搜索结果列表
    if (cache.literatureList && Array.isArray(cache.literatureList)) {
      literatureList.value = cache.literatureList
    }

    // 恢复搜索状态
    hasSearched.value = cache.hasSearched || false

    logger.info('搜索结果已从缓存恢复', { 
      count: literatureList.value.length,
      total: pagination.total,
      cacheAge: Math.round(cacheAge / 1000) + '秒'
    })
    return true
  } catch (error) {
    logger.error('恢复搜索结果缓存失败', error)
    // 清除损坏的缓存
    sessionStorage.removeItem(CACHE_KEY)
    return false
  }
}

// 清除搜索结果缓存
const clearSearchCache = () => {
  sessionStorage.removeItem(CACHE_KEY)
  logger.info('搜索结果缓存已清除')
}

// 搜索文献
const handleSearch = async (forceRefresh = false) => {
  if (!searchForm.search.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  // 如果不是强制刷新，先检查缓存
  if (!forceRefresh) {
    const cacheKey = generateCacheKey()
    const cachedData = sessionStorage.getItem(CACHE_KEY)
    if (cachedData) {
      try {
        const cache = JSON.parse(cachedData)
        const currentParams = {
          search: searchForm.search.trim(),
          publicationYearFilter: searchForm.publicationYearFilter || '',
          dataSource: searchForm.dataSource,
          perPage: pagination.size,
          page: pagination.current
        }
        const cachedParams = {
          search: cache.searchForm?.search?.trim() || '',
          publicationYearFilter: cache.searchForm?.publicationYearFilter || '',
          dataSource: cache.searchForm?.dataSource || 'openalex',
          perPage: cache.pagination?.size || 10,
          page: cache.pagination?.current || 1
        }
        
        // 如果参数相同，使用缓存
        if (JSON.stringify(currentParams) === JSON.stringify(cachedParams)) {
          logger.info('使用缓存的搜索结果，跳过API调用')
          restoreSearchCache()
          return
        }
      } catch (error) {
        logger.warn('检查缓存失败，继续调用API', error)
      }
    }
  }

  loading.value = true
  hasSearched.value = true

  try {
    const params = {
      search: searchForm.search.trim(),
      publicationYearFilter: searchForm.publicationYearFilter || undefined,
      dataSource: searchForm.dataSource,
      perPage: pagination.size,
      page: pagination.current
    }

    logger.info('搜索文献', params)
    const response = await searchLiterature(params)

    if (response.code === 200 && response.data) {
      literatureList.value = response.data.records || []
      pagination.total = response.data.total || 0
      pagination.pages = response.data.pages || 0
      pagination.current = response.data.current || 1
      pagination.size = response.data.size || 10

      // 保存到缓存
      saveSearchCache()

      if (literatureList.value.length === 0) {
        ElMessage.info('未找到相关文献')
      }
    } else {
      ElMessage.error(response.message || '搜索失败')
    }
  } catch (error) {
    logger.error('搜索文献失败', error)
    ElMessage.error('搜索失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.search = ''
  searchForm.publicationYearFilter = ''
  searchForm.dataSource = 'openalex'
  pagination.current = 1
  literatureList.value = []
  hasSearched.value = false
  // 清除缓存
  clearSearchCache()
}

// 查看详情
const handleViewDetail = (literature) => {
  const literatureId = literature.paperInfo?.id || literature.paperInfo?.globalId?.split('/').pop() || 'unknown'
  const globalId = literature.paperInfo?.globalId || literature.paperInfo?.id
  
  // 存储数据到 sessionStorage
  const storageKey = `literature_detail_${literatureId}`
  sessionStorage.setItem(storageKey, JSON.stringify(literature))
  
  if (globalId && globalId !== literatureId) {
    const globalKey = `literature_detail_${globalId}`
    sessionStorage.setItem(globalKey, JSON.stringify(literature))
  }
  
  router.push({
    name: 'LiteratureDetail',
    params: {
      id: globalId || literatureId
    },
    query: {
      dataSource: 'openalex'
    }
  })
}

// 获取显示的作者（最多3个）
const getDisplayAuthors = (authorsInfo) => {
  if (!authorsInfo || authorsInfo.length === 0) return []
  return authorsInfo.slice(0, 3)
}

// 获取作者首字母
const getAuthorInitial = (name) => {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 切换摘要展开/收起
const toggleAbstract = (paperId) => {
  expandedAbstracts.value[paperId] = !expandedAbstracts.value[paperId]
}

// 截断文本
const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

// 分页变化
const handlePageChange = (page) => {
  pagination.current = page
  searchForm.page = page
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  searchForm.perPage = size
  searchForm.page = 1
  handleSearch()
}

// 页面激活时恢复缓存
onActivated(() => {
  // 从详情页返回时，尝试恢复缓存
  if (hasSearched.value === false && literatureList.value.length === 0) {
    const restored = restoreSearchCache()
    if (restored) {
      logger.info('从详情页返回，已恢复搜索结果')
    }
  }
})

// 组件挂载时恢复缓存
onMounted(() => {
  // 尝试从缓存恢复数据
  restoreSearchCache()
})
</script>

<style scoped lang="scss">
.literature-search-container {
  padding: 0;
  min-height: calc(100vh - 60px);
  background: #f5f5f5;

  .search-header {
    background: #fff;
    padding: 20px 24px;
    border-bottom: 1px solid #e5e7eb;
    margin-bottom: 0;

    .search-bar-wrapper {
      margin-bottom: 16px;

      .main-search-input {
        max-width: 600px;

        :deep(.el-input__wrapper) {
          border-radius: 8px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        }
      }
    }

    .filter-bar {
      display: flex;
      gap: 12px;
      align-items: center;
      flex-wrap: wrap;
    }
  }

  .result-header {
    background: #fff;
    padding: 12px 24px;
    border-bottom: 1px solid #e5e7eb;
    display: flex;
    align-items: center;
    gap: 8px;

    .result-count {
      font-size: 14px;
      color: #6b7280;
      font-weight: 500;
    }
  }

  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    gap: 12px;
    color: var(--el-text-color-secondary);

    .el-icon {
      font-size: 32px;
    }
  }

  .literature-list {
    padding: 20px 24px;
    display: flex;
    flex-direction: column;
    gap: 16px;

    .literature-card {
      background: #fff;
      border-radius: 8px;
      padding: 24px;
      border: 1px solid #e5e7eb;
      transition: all 0.2s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        border-color: #d1d5db;
      }

      .card-title-section {
        margin-bottom: 16px;

        .literature-title {
          font-size: 20px;
          font-weight: 600;
          color: #111827;
          margin: 0 0 8px 0;
          line-height: 1.4;
          cursor: pointer;
          transition: color 0.2s;

          &:hover {
            color: #3b82f6;
          }
        }

        .title-badges {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }

        .title-translated {
          font-size: 14px;
          color: #6b7280;
          margin-top: 8px;
          line-height: 1.5;
        }
      }

      .card-authors {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;
        flex-wrap: wrap;

        .author-item {
          display: flex;
          align-items: center;
          gap: 8px;

          .author-avatar {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            font-size: 14px;
          }

          .author-name {
            font-size: 14px;
            color: #374151;
            font-weight: 500;
          }
        }

        .more-authors {
          font-size: 14px;
          color: #6b7280;
        }
      }

      .card-publication {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 16px;
        font-size: 14px;
        color: #6b7280;
        flex-wrap: wrap;
      }

      .card-abstract {
        margin-bottom: 16px;

        .abstract-label {
          font-size: 13px;
          font-weight: 600;
          color: #6b7280;
          margin-bottom: 8px;
        }

        .abstract-content {
          font-size: 14px;
          color: #4b5563;
          line-height: 1.6;

          .expand-toggle {
            color: #3b82f6;
            cursor: pointer;
            margin-left: 4px;
            font-weight: 500;

            &:hover {
              text-decoration: underline;
            }
          }
        }

        .abstract-translated {
          margin-top: 16px;
          padding-top: 16px;
          border-top: 1px solid #f3f4f6;
        }
      }

      .card-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 16px;
        border-top: 1px solid #f3f4f6;

        .footer-stats {
          display: flex;
          gap: 16px;
          align-items: center;

          .stat-item {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 14px;
            color: #6b7280;

            &.citation-stat {
              color: #3b82f6;
              font-weight: 500;
            }
          }
        }
      }
    }
  }

  .empty-state {
    padding: 60px 20px;
    text-align: center;
  }

  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    padding: 20px;
  }
}
</style>
