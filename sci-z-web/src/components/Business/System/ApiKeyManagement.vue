<!--
/**
 * @description 系统管理 - API Key 配置业务组件
 * 负责 API Key 列表展示、搜索筛选、新增/编辑/删除等功能
 */
-->
<template>
  <div class="apikey-management-container">
    <div class="page-header">
      <h1 class="page-title">{{ t('system.apikey.title') }}</h1>
    </div>

    <BaseCard class="content-card">
      <!-- 搜索筛选区域 -->
      <div class="filter-section">
        <el-input
          v-model="searchForm.keyword"
          :placeholder="t('system.apikey.searchPlaceholder')"
          clearable
          style="width: 250px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="searchForm.keyType"
          :placeholder="t('system.apikey.keyTypePlaceholder')"
          clearable
          style="width: 180px"
        >
          <el-option :label="t('common.all')" value="" />
          <el-option :label="t('system.apikey.keyTypeDataset')" value="dataset" />
          <el-option :label="t('system.apikey.keyTypeWorkflow')" value="workflow" />
          <el-option :label="t('system.apikey.keyTypeFile')" value="file" />
        </el-select>
        
        <el-select
          v-model="searchForm.isActive"
          :placeholder="t('system.apikey.statusPlaceholder')"
          clearable
          style="width: 150px"
        >
          <el-option :label="t('common.all')" value="" />
          <el-option :label="t('system.apikey.active')" :value="true" />
          <el-option :label="t('system.apikey.inactive')" :value="false" />
        </el-select>
        
        <el-button type="primary" :loading="loading" @click="handleSearch">
          <el-icon><Search /></el-icon>
          {{ t('common.search') }}
        </el-button>
        <el-button type="primary" @click="handleReset">
          <el-icon><Refresh /></el-icon>
          <span class="button-text">{{ t('common.reset') }}</span>
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          {{ t('system.apikey.addApiKey') }}
        </el-button>
      </div>

      <!-- API Key 列表表格 -->
      <BaseTable
        :data="apiKeyList"
        :columns="tableColumns"
        :loading="loading"
        :pagination="pagination"
        :action-width="200"
        action-fixed="right"
        :empty-text="t('common.noData')"
        stripe
        class="apikey-table"
        @current-change="handlePageChange"
        @size-change="handlePageSizeChange"
      >
        <!-- 调试信息（开发环境） -->
        <template v-if="false" #toolbar>
          <div style="padding: 10px; background: #f0f0f0; margin-bottom: 10px;">
            <p>数据条数: {{ apiKeyList.length }}</p>
            <p>分页总数: {{ pagination.total }}</p>
            <pre>{{ JSON.stringify(apiKeyList.slice(0, 2), null, 2) }}</pre>
          </div>
        </template>
        <!-- 密钥类型列自定义 -->
        <template #keyType="{ row }">
          <el-tag :type="getKeyTypeTagType(row.keyType)" class="key-type-tag">
            {{ getKeyTypeText(row.keyType) }}
          </el-tag>
        </template>
        <!-- 状态列自定义 -->
        <template #isActive="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'" class="status-tag">
            {{ row.isActive ? t('system.apikey.active') : t('system.apikey.inactive') }}
          </el-tag>
        </template>
        <!-- API Key 列自定义（部分隐藏，悬浮显示完整） -->
        <template #apiKey="{ row }">
          <BaseTooltip :content="row.apiKey || ''" placement="top">
            <div class="api-key-cell">
              <span class="api-key-masked">{{ maskApiKey(row.apiKey) }}</span>
            </div>
          </BaseTooltip>
        </template>

        <!-- 创建时间列自定义 -->
        <template #createdTime="{ row }">
          {{ formatDisplayTime(row.createdTime) }}
        </template>

        <!-- 操作列 -->
        <template #actions="{ row }">
          <div class="action-buttons">
            <!-- 编辑按钮 -->
            <BaseTooltip :content="t('common.edit')" placement="top">
              <button
                class="action-btn btn-primary"
                @click.stop="handleEdit(row)"
              >
                <el-icon><Edit /></el-icon>
              </button>
            </BaseTooltip>
            <!-- 启用/禁用按钮 -->
            <BaseTooltip 
              :content="row.isActive ? t('common.disable') : t('common.enable')" 
              placement="top"
            >
              <button
                class="action-btn"
                :class="row.isActive ? 'btn-info' : 'btn-success'"
                @click.stop="handleToggleStatus(row)"
              >
                <el-icon v-if="row.isActive">
                  <CircleClose />
                </el-icon>
                <el-icon v-else>
                  <CircleCheck />
                </el-icon>
              </button>
            </BaseTooltip>
            <!-- 删除按钮 -->
            <BaseTooltip :content="t('common.delete')" placement="top">
              <button
                class="action-btn btn-danger"
                @click.stop="handleDelete(row)"
              >
                <el-icon><Delete /></el-icon>
              </button>
            </BaseTooltip>
          </div>
        </template>
      </BaseTable>
    </BaseCard>

    <!-- 新增/编辑 API Key 对话框 -->
    <BaseDialog
      v-model="showDialog"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="apikey-form"
      >
        <el-form-item :label="t('system.apikey.keyType')" prop="keyType">
          <el-select
            v-model="formData.keyType"
            :placeholder="t('system.apikey.keyTypePlaceholder')"
            style="width: 100%"
          >
            <el-option :label="t('system.apikey.keyTypeDataset')" value="dataset" />
            <el-option :label="t('system.apikey.keyTypeWorkflow')" value="workflow" />
            <el-option :label="t('system.apikey.keyTypeFile')" value="file" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('system.apikey.resourceId')" prop="resourceId">
          <el-input
            v-model="formData.resourceId"
            :placeholder="t('system.apikey.resourceIdPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('system.apikey.apiKey')" prop="apiKey">
          <el-input
            v-model="formData.apiKey"
            :placeholder="t('system.apikey.apiKeyPlaceholder')"
            show-password
          />
        </el-form-item>
        <el-form-item :label="t('system.apikey.keyName')" prop="keyName">
          <el-input
            v-model="formData.keyName"
            :placeholder="t('system.apikey.keyNamePlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('system.apikey.description')" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            :placeholder="t('system.apikey.descriptionPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('common.status')" prop="isActive">
          <el-switch
            v-model="formData.isActive"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <BaseButton @click="showDialog = false">
          {{ t('common.cancel') }}
        </BaseButton>
        <BaseButton type="primary" @click="handleSubmit" :loading="submitting">
          {{ t('common.confirm') }}
        </BaseButton>
      </template>
    </BaseDialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Plus, Search, Refresh, Edit, Delete, CircleClose, CircleCheck } from '@element-plus/icons-vue'
import { BaseButton, BaseCard, BaseTable, BaseDialog, BaseTooltip } from '@/components/Common'
import { getApiKeyList, createApiKey, updateApiKey, deleteApiKey } from '@/api/Dify/dify'
import { formatDate } from '@/utils/date'
import { createLogger } from '@/utils/simpleLogger'
import { useAuthStore } from '@/store/modules/auth'

const { t } = useI18n()
const logger = createLogger('ApiKeyManagement')
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  keyType: '',
  isActive: null
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表单数据
const formData = reactive({
  id: null,
  userId: null,
  keyType: '',
  resourceId: '',
  apiKey: '',
  keyName: '',
  description: '',
  isActive: true
})

// API Key 列表
const apiKeyList = ref([])

// 对话框标题
const dialogTitle = computed(() => {
  return isEdit.value ? t('system.apikey.editApiKey') : t('system.apikey.addApiKey')
})

// 表格列配置 - 优化自适应策略，参考 DeclarationList.vue
const tableColumns = computed(() => [
  {
    prop: 'keyName',
    label: t('system.apikey.keyName'),
    minWidth: 150,
    align: 'center',
    showOverflowTooltip: true
  },
  {
    prop: 'keyType',
    label: t('system.apikey.keyType'),
    minWidth: 100,
    align: 'center'
  },
  {
    prop: 'apiKey',
    label: t('system.apikey.apiKey'),
    minWidth: 180,
    align: 'center',
    showOverflowTooltip: false
  },
  {
    prop: 'description',
    label: t('system.apikey.description'),
    minWidth: 200,
    showOverflowTooltip: false,
    wrap: true,
    align: 'center',
    headerAlign: 'center'
  },
  {
    prop: 'isActive',
    label: t('common.status'),
    minWidth: 100,
    align: 'center'
  },
  {
    prop: 'createdTime',
    label: t('common.createTime'),
    minWidth: 140,
    align: 'center',
    showOverflowTooltip: true
  }
])

// 表单验证规则
const formRules = computed(() => ({
  keyType: [
    { required: true, message: t('system.apikey.keyTypeRequired'), trigger: 'change' }
  ],
  resourceId: [
    { required: true, message: t('system.apikey.resourceIdRequired'), trigger: 'blur' }
  ],
  apiKey: [
    { required: true, message: t('system.apikey.apiKeyRequired'), trigger: 'blur' }
  ],
  keyName: [
    { required: true, message: t('system.apikey.keyNameRequired'), trigger: 'blur' }
  ]
}))

// 获取密钥类型文本
const getKeyTypeText = (keyType) => {
  const map = {
    dataset: t('system.apikey.keyTypeDataset'),
    workflow: t('system.apikey.keyTypeWorkflow'),
    file: t('system.apikey.keyTypeFile')
  }
  return map[keyType] || keyType
}

// 获取密钥类型标签类型
const getKeyTypeTagType = (keyType) => {
  const map = {
    dataset: 'primary',
    workflow: 'success',
    file: 'warning'
  }
  return map[keyType] || ''
}

// 隐藏 API Key（只显示前后几位）
const maskApiKey = (apiKey) => {
  if (!apiKey) return ''
  if (apiKey.length <= 8) return apiKey
  return `${apiKey.substring(0, 4)}****${apiKey.substring(apiKey.length - 4)}`
}


// 格式化显示时间 - 只显示年月日时分
const formatDisplayTime = (time) => {
  if (!time) return '—'
  return formatDate(time, 'YYYY-MM-DD HH:mm')
}

// 加载 API Key 列表
const loadApiKeyList = async () => {
  loading.value = true
  try {
    const currentUser = authStore.userInfo
    // 根据接口文档，构建查询参数
    const requestData = {}
    
    // 如果有关键词，同时搜索 keyName 和 resourceId（模糊匹配）
    if (searchForm.keyword) {
      requestData.keyName = searchForm.keyword
      requestData.resourceId = searchForm.keyword
    }
    
    // 其他筛选条件
    if (searchForm.keyType) {
      requestData.keyType = searchForm.keyType
    }
    
    if (searchForm.isActive !== null && searchForm.isActive !== '') {
      requestData.isActive = searchForm.isActive
    }
    
    // 用户ID（可选，如果需要筛选当前用户的密钥）
    // 尝试多种可能的字段名：userId, id, user_id
    const userId = currentUser?.userId || currentUser?.id || currentUser?.user_id
    if (userId) {
      requestData.userId = userId
    }

    const response = await getApiKeyList(requestData)
    
    // 注意：axios 响应拦截器已经返回了 response.data
    // 所以这里的 response 就是接口返回的 ResultVO: { flag, code, message, data }
    const result = response || {}
    
    logger.info('API response received', { result, flag: result.flag, dataType: typeof result.data, isArray: Array.isArray(result.data) })
    
    if (!result.flag) {
      throw new Error(result.message || t('system.apikey.loadError'))
    }
    
    const data = result.data || []
    
    // 处理返回的数据格式（可能是数组或分页对象）
    if (Array.isArray(data)) {
      // 清理数据中的分页字段（如果存在）
      const cleanedData = data.map(item => {
        const { pageNum, pageSize, ...rest } = item
        return rest
      })
      apiKeyList.value = cleanedData
      pagination.total = cleanedData.length
      logger.info('Data assigned to apiKeyList', { count: cleanedData.length, firstItem: cleanedData[0] })
    } else {
      apiKeyList.value = data.records || data.list || []
      pagination.total = data.total || 0
      pagination.current = data.current || data.pageNo || pagination.current
      pagination.size = data.size || data.pageSize || pagination.size
      logger.info('Pagination data assigned', { count: apiKeyList.value.length, total: pagination.total })
    }

    logger.info('API Key list loaded successfully', { count: apiKeyList.value.length })
  } catch (error) {
    logger.error('Failed to load API Key list', error)
    const errorMessage = error.response?.data?.message || error.message || t('system.apikey.loadError')
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadApiKeyList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.keyType = ''
  searchForm.isActive = null
  pagination.current = 1
  loadApiKeyList()
  ElMessage.success(t('common.resetSuccess'))
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  const currentUser = authStore.userInfo
  // 尝试多种可能的字段名：userId, id, user_id
  formData.userId = currentUser?.userId || currentUser?.id || currentUser?.user_id || null
  if (!formData.userId) {
    logger.warn('无法获取当前用户ID', { userInfo: currentUser })
    ElMessage.warning('无法获取当前用户信息，请重新登录')
  }
  showDialog.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    userId: row.userId,
    keyType: row.keyType,
    resourceId: row.resourceId,
    apiKey: row.apiKey,
    keyName: row.keyName,
    description: row.description || '',
    isActive: row.isActive !== undefined ? row.isActive : true
  })
  showDialog.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      t('system.apikey.deleteConfirm', { name: row.keyName }),
      t('system.apikey.confirmDelete'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )

    const response = await deleteApiKey(row.id)
    // axios 响应拦截器已经返回了 response.data，所以 response 就是 ResultVO
    const result = response || {}
    
    if (!result.flag) {
      throw new Error(result.message || t('system.apikey.deleteFailed'))
    }
    
    ElMessage.success(t('system.apikey.deleteSuccess'))
    loadApiKeyList()
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('Failed to delete API Key', error)
      const errorMessage = error.response?.data?.message || error.message || t('system.apikey.deleteFailed')
      ElMessage.error(errorMessage)
    }
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  const isActive = row.isActive
  const action = isActive ? t('common.disable') : t('common.enable')
  try {
    await ElMessageBox.confirm(
      t('system.apikey.toggleStatusConfirm', { action, name: row.keyName }),
      t('system.apikey.toggleStatus'),
      {
        type: 'warning',
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel')
      }
    )

    logger.info('Toggle API Key status', {
      id: row.id,
      keyName: row.keyName,
      action
    })

    const newStatus = !row.isActive
    const response = await updateApiKey({
      id: row.id,
      isActive: newStatus
    })
    
    // axios 响应拦截器已经返回了 response.data，所以 response 就是 ResultVO
    const result = response || {}
    
    if (!result.flag) {
      throw new Error(result.message || t('system.apikey.toggleFailed'))
    }
    
    ElMessage.success(t('system.apikey.toggleStatusSuccess', { action }))
    logger.info('API Key status toggled successfully', {
      id: row.id,
      newStatus
    })
    
    loadApiKeyList()
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('Failed to toggle API Key status', error)
      const errorMessage = error.response?.data?.message || error.message || t('system.apikey.toggleFailed')
      ElMessage.error(errorMessage)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    // 如果是新建，确保 userId 存在
    if (!isEdit.value && !formData.userId) {
      const currentUser = authStore.userInfo
      formData.userId = currentUser?.userId || currentUser?.id || currentUser?.user_id || null
      if (!formData.userId) {
        ElMessage.error('无法获取当前用户信息，请重新登录')
        return
      }
    }
    
    submitting.value = true

    let response
    if (isEdit.value) {
      response = await updateApiKey(formData)
    } else {
      // 确保提交时包含 userId
      const submitData = {
        ...formData,
        userId: formData.userId
      }
      logger.info('Creating API Key', { userId: submitData.userId, keyType: submitData.keyType })
      response = await createApiKey(submitData)
    }
    
    // axios 响应拦截器已经返回了 response.data，所以 response 就是 ResultVO
    const result = response || {}
    
    if (!result.flag) {
      throw new Error(result.message || t('common.operationFailed'))
    }

    ElMessage.success(isEdit.value ? t('system.apikey.updateSuccess') : t('system.apikey.createSuccess'))
    showDialog.value = false
    loadApiKeyList()
  } catch (error) {
    if (error !== false) {
      logger.error('Failed to submit form', error)
      const errorMessage = error.response?.data?.message || error.message || t('common.operationFailed')
      ElMessage.error(errorMessage)
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    userId: null,
    keyType: '',
    resourceId: '',
    apiKey: '',
    keyName: '',
    description: '',
    isActive: true
  })
  formRef.value?.clearValidate()
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.current = page
  loadApiKeyList()
}

// 每页数量变化
const handlePageSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadApiKeyList()
}

// 初始化
onMounted(() => {
  loadApiKeyList()
})
</script>

<style lang="scss" scoped>
.apikey-management-container {
  padding: 20px;
  overflow-x: hidden;

  .page-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: var(--color-primary);
      margin: 0;
    }
  }

  .content-card {
    .filter-section {
      display: flex;
      gap: 12px;
      margin-bottom: 20px;
      flex-wrap: wrap;
      align-items: center;
      
      // 🔥 按钮图标和文字间距优化
      :deep(.el-button) {
        display: inline-flex !important;
        align-items: center !important;
        gap: 8px !important; // 🔥 图标和文字之间的间距
        
        // 确保图标和文字之间有间距
        .el-icon {
          margin-right: 0 !important;
          margin-left: 0 !important;
        }
        
        .button-text,
        span:not(.el-icon) {
          margin-left: 0 !important;
          margin-right: 0 !important;
        }
        
        // 针对 Element Plus 按钮内部结构
        > * {
          margin-left: 0 !important;
          margin-right: 0 !important;
        }
      }
    }

    .apikey-table {
      width: 100%;
      border-radius: 8px;
      overflow: visible;
      border: 1px solid var(--border);
      
      // 🔥 确保表格自适应，参考 DeclarationList.vue
      :deep(.base-table) {
        width: 100%;
        display: flex;
        flex-direction: column;
        overflow: visible;
      }
      
      :deep(.base-table__table) {
        width: 100% !important;
        flex: 1;
        overflow: visible;
      }
      
      // 🔥 确保表格自适应，不出现横向滚动条
      :deep(.el-table) {
        width: 100%;
        
        // 表头样式
        .el-table__header {
          th {
            padding: 14px 16px !important;
            font-size: 14px;
            font-weight: 600 !important;
            color: var(--text-2);
            background-color: var(--surface) !important;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          
          // 🔥 所有表头居中对齐
          th.is-center {
            text-align: center !important;
            
            .cell {
              text-align: center !important;
              display: flex !important;
              justify-content: center !important;
              align-items: center !important;
            }
          }
        }
        
        // 表体样式
        .el-table__body {
          td {
            padding: 12px 16px !important;
            font-size: 14px;
            color: var(--text);
          }
          
          // 🔥 所有列内容居中对齐
          td.is-center {
            text-align: center !important;
            
            .cell {
              text-align: center !important;
              display: flex !important;
              justify-content: center !important;
              align-items: center !important;
            }
          }
          
          // 🔥 描述列允许换行，但内容居中
          td[data-column-key="description"] {
            white-space: normal;
            word-break: break-word;
            line-height: 1.6;
            
            .cell {
              text-align: center !important;
            }
          }
        }
        
        // 操作列固定右侧
        .el-table__fixed-right {
          right: 0 !important;
          box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
        }
      }
      
      .api-key-cell {
        display: flex;
        align-items: center;
        cursor: default;

        .api-key-masked {
          font-family: monospace;
          color: var(--text-3);
        }
      }

      // 🔥 密钥类型和状态标签圆角处理
      .key-type-tag,
      .status-tag {
        border-radius: 12px !important;
        padding: 4px 8px !important;
        font-size: 12px !important;
        font-weight: 500 !important;
      }

      .action-buttons {
        display: flex;
        flex-direction: row; // 🔥 改为横向排列，图标按钮一行显示
        gap: 8px;
        justify-content: center;
        align-items: center;
        flex-wrap: nowrap; // 🔥 确保不换行

        .action-btn {
          padding: 4px; // 🔥 图标按钮使用更小的内边距
          min-width: 32px; // 🔥 确保图标按钮有最小宽度
          height: 28px; // 🔥 统一高度
          border: 1px solid transparent;
          border-radius: 4px;
          font-size: 13px;
          cursor: pointer;
          transition: all 0.2s;
          background: none;
          white-space: nowrap;
          display: inline-flex;
          align-items: center;
          justify-content: center; // 🔥 图标居中显示
          gap: 0; // 🔥 只有图标，不需要 gap
          
          // 🔥 图标样式
          .el-icon {
            font-size: 16px; // 🔥 图标大小
            display: flex;
            align-items: center;
            justify-content: center;
          }

          &:disabled {
            opacity: 0.5;
            cursor: not-allowed;

            &:hover {
              background: none;
              color: inherit;
            }
          }

          &.btn-primary {
            color: var(--color-primary);
            border-color: var(--color-primary);

            &:hover:not(:disabled) {
              background: var(--color-primary);
              color: var(--surface);
            }
          }

          &.btn-info {
            color: var(--text-3);
            border-color: var(--text-3);

            &:hover:not(:disabled) {
              background: var(--text-3);
              color: var(--surface);
            }
          }

          &.btn-success {
            color: #16a34a;
            border-color: #16a34a;

            &:hover:not(:disabled) {
              background: #16a34a;
              color: var(--surface);
            }
          }

          &.btn-danger {
            color: #dc2626;
            border-color: #dc2626;

            &:hover:not(:disabled) {
              background: #dc2626;
              color: var(--surface);
            }
          }
        }
      }
    }

  }
}

// 🔥 表单样式 - 按照 @页面修改.md
.apikey-form {
  :deep(.el-form-item__label) {
    font-size: 14px !important;
    color: var(--text-2) !important;
    font-weight: 600 !important;
  }
  
  :deep(.el-input__inner),
  :deep(.el-textarea__inner),
  :deep(.el-select .el-input__inner) {
    font-size: 14px !important;
    color: var(--text-3) !important;
    font-weight: 400 !important;
  }
  
  :deep(.el-tag),
  :deep(.status-tag) {
    border-radius: 12px !important;
    padding: 4px 8px !important;
    font-size: 12px !important;
    font-weight: 500 !important;
  }
}

// 🔥 统一弹窗取消按钮样式 - 确保与删除弹窗一致
:deep(.base-dialog__footer .base-button:not(.el-button--primary)) {
  color: var(--text-2) !important;
  border-color: var(--border) !important;
  background-color: var(--surface) !important;
  
  &:hover {
    color: var(--text-2) !important;
    border-color: var(--border-hover) !important;
    background-color: var(--hover) !important;
  }
  
  &:active {
    color: var(--text-2) !important;
    border-color: var(--border) !important;
    background-color: var(--surface) !important;
  }
}
</style>

