<!--
  系统消息页：站内消息列表、详情；产教研分发类消息可接受/拒绝，拒绝时填写原因，领导将收到结果通知。
-->
<template>
  <div class="user-messages-page">
    <h1 class="page-title">{{ $t('message.systemMessage') }}</h1>

    <div class="toolbar">
      <el-checkbox v-model="unreadOnly" @change="fetchList">
        {{ $t('message.unreadOnly') }}
      </el-checkbox>
      <el-button v-if="unreadCount > 0" type="primary" link @click="handleMarkAllRead">
        {{ $t('message.read') }}全部
      </el-button>
    </div>

    <div v-loading="loading" class="message-list-wrap">
      <template v-if="list.length">
        <div
          v-for="item in list"
          :key="item.id"
          class="message-item"
          :class="{ unread: item.status === 'pending' }"
          @click="openDetail(item.id)"
        >
          <div class="message-item-header">
            <span class="message-type-tag">{{ typeTag(item.type) }}</span>
            <span class="message-time">{{ formatTime(item.createdTime) }}</span>
          </div>
          <div class="message-item-title">{{ item.title }}</div>
          <div class="message-item-content">{{ item.content }}</div>
          <div class="message-item-meta">
            {{ $t('message.sender') }}：{{ item.senderName || '—' }}
            <span v-if="item.status !== 'pending'" class="message-status">{{ statusText(item.status) }}</span>
          </div>
        </div>
      </template>
      <el-empty v-else :description="$t('message.noMessage')" />
    </div>

    <el-pagination
      v-if="total > pageSize"
      :current-page="page"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      class="pagination"
      @current-change="(p) => { page = p; fetchList() }"
    />

    <!-- 消息详情抽屉：展示内容，分发类显示接受/拒绝 -->
    <el-drawer
      v-model="drawerVisible"
      :title="detail?.title"
      direction="rtl"
      size="480px"
      class="message-detail-drawer"
    >
      <template v-if="detail">
        <div class="detail-meta">
          <span>{{ $t('message.sender') }}：{{ detail.senderName || '—' }}</span>
          <span>{{ $t('message.time') }}：{{ formatTime(detail.createdTime) }}</span>
        </div>
        <div class="detail-content">{{ detail.content }}</div>
        <!-- 申报基础信息（从 extra 解析，仅分发类且有 extra 时展示） -->
        <div v-if="detail.type === 'industry_education_distribute' && extraInfo" class="detail-extra">
          <div class="extra-title">下发时的申报基础信息</div>
          <dl class="extra-dl">
            <template v-if="extraInfo.research_topic">
              <dt>研究课题</dt>
              <dd>{{ extraInfo.research_topic }}</dd>
            </template>
            <template v-if="extraInfo.research_direction">
              <dt>研究方向</dt>
              <dd>{{ extraInfo.research_direction }}</dd>
            </template>
            <template v-if="extraInfo.department">
              <dt>部门</dt>
              <dd>{{ extraInfo.department }}</dd>
            </template>
            <template v-if="extraInfo.document_publish_time">
              <dt>红头文件发布时间</dt>
              <dd>{{ extraInfo.document_publish_time }}</dd>
            </template>
            <template v-if="extraInfo.project_start_time">
              <dt>项目开始时间</dt>
              <dd>{{ extraInfo.project_start_time }}</dd>
            </template>
            <template v-if="extraInfo.project_end_time">
              <dt>项目结束时间</dt>
              <dd>{{ extraInfo.project_end_time }}</dd>
            </template>
            <template v-if="extraInfo.research_fields && extraInfo.research_fields.length">
              <dt>研究领域</dt>
              <dd>{{ Array.isArray(extraInfo.research_fields) ? extraInfo.research_fields.join('、') : extraInfo.research_fields }}</dd>
            </template>
          </dl>
        </div>
        <!-- 接受/拒绝：仅分发类且待处理时显示 -->
        <div v-if="detail.type === 'industry_education_distribute' && detail.status === 'pending'" class="detail-actions">
          <el-input
            v-model="rejectReason"
            type="textarea"
            :placeholder="$t('message.rejectReasonPlaceholder')"
            :rows="3"
            maxlength="500"
            show-word-limit
            class="reject-reason-input"
          />
          <div class="action-buttons">
            <el-button type="primary" :loading="acceptLoading" @click="handleAccept">
              {{ $t('message.accept') }}
            </el-button>
            <el-button :loading="rejectLoading" @click="handleReject">
              {{ $t('message.reject') }}
            </el-button>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getMessageList, getMessageDetail, getUnreadCount, markMessageRead, markAllMessagesRead } from '@/api/Message/message'
import { acceptDistribute, rejectDistribute } from '@/api/Practice/industryEducation'

const { t } = useI18n()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const unreadOnly = ref(false)
const unreadCount = ref(0)

const drawerVisible = ref(false)
const detail = ref(null)
const acceptLoading = ref(false)
const rejectLoading = ref(false)
const rejectReason = ref('')

const extraInfo = computed(() => {
  if (!detail.value?.extraJson) return null
  try {
    return JSON.parse(detail.value.extraJson)
  } catch {
    return null
  }
})

function typeTag(type) {
  if (type === 'industry_education_distribute') return t('message.distributeTitle')
  if (type === 'industry_education_distribute_result') return t('message.distributeResultTitle')
  return type || '—'
}

function statusText(status) {
  const map = { pending: t('message.pending'), read: t('message.read'), accepted: t('message.accepted'), rejected: t('message.rejected') }
  return map[status] || status
}

function formatTime(val) {
  if (!val) return '—'
  const d = new Date(val)
  const now = new Date()
  const sameDay = d.toDateString() === now.toDateString()
  if (sameDay) return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return d.toLocaleDateString('zh-CN')
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getMessageList({ page: page.value, size: pageSize.value, unreadOnly: unreadOnly.value })
    list.value = res?.data?.records ?? res?.records ?? []
    total.value = res?.data?.total ?? res?.total ?? 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res?.data ?? res ?? 0
    window.dispatchEvent(new CustomEvent('messagesUnreadCountChanged'))
  } catch {
    unreadCount.value = 0
    window.dispatchEvent(new CustomEvent('messagesUnreadCountChanged'))
  }
}

async function openDetail(id) {
  try {
    const res = await getMessageDetail(id)
    detail.value = res?.data ?? res
    rejectReason.value = ''
    if (detail.value?.id) markRead(detail.value.id)
    drawerVisible.value = true
  } catch {
    detail.value = null
  }
}

function markRead(id) {
  markMessageRead(id).catch(() => {})
  const item = list.value.find((m) => m.id === id)
  if (item) item.status = 'read'
  fetchUnreadCount()
}

async function handleMarkAllRead() {
  try {
    await markAllMessagesRead()
    fetchList()
    fetchUnreadCount()
    ElMessage.success(t('message.read') + '全部成功')
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function handleAccept() {
  if (!detail.value?.id) return
  acceptLoading.value = true
  try {
    await acceptDistribute(detail.value.id)
    ElMessage.success(t('message.acceptSuccess'))
    drawerVisible.value = false
    detail.value = null
    fetchList()
    fetchUnreadCount()
  } catch (e) {
    ElMessage.error(e?.message || '接受失败')
  } finally {
    acceptLoading.value = false
  }
}

async function handleReject() {
  if (!detail.value?.id) return
  const reason = (rejectReason.value || '').trim()
  if (!reason) {
    ElMessage.warning(t('message.rejectReasonPlaceholder'))
    return
  }
  rejectLoading.value = true
  try {
    await rejectDistribute(detail.value.id, { reason })
    ElMessage.success(t('message.rejectSuccess'))
    drawerVisible.value = false
    detail.value = null
    rejectReason.value = ''
    fetchList()
    fetchUnreadCount()
  } catch (e) {
    ElMessage.error(e?.message || '拒绝失败')
  } finally {
    rejectLoading.value = false
  }
}

onMounted(() => {
  fetchList()
  fetchUnreadCount()
})
</script>

<style lang="scss" scoped>
.user-messages-page {
  padding: 24px;
  max-width: 900px;
  margin: 0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-1, #1f2937);
  margin: 0 0 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.message-list-wrap {
  min-height: 200px;
}

.message-item {
  padding: 14px 16px;
  margin-bottom: 8px;
  background: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
    border-color: var(--el-border-color);
  }
  &.unread {
    border-left: 3px solid var(--el-color-primary);
    background: rgba(59, 130, 246, 0.04);
  }
}

.message-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.message-type-tag {
  font-size: 12px;
  color: var(--el-color-primary);
  font-weight: 500;
}

.message-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.message-item-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-1);
  margin-bottom: 4px;
}

.message-item-content {
  font-size: 14px;
  color: var(--text-2, #6b7280);
  line-height: 1.5;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-item-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.message-status {
  margin-left: 8px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-start;
}

.detail-meta {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-content {
  font-size: 14px;
  color: var(--text-2);
  line-height: 1.6;
  margin-bottom: 16px;
  white-space: pre-wrap;
}

.detail-extra {
  margin-bottom: 20px;
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}
.extra-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
  margin-bottom: 8px;
}
.extra-dl {
  margin: 0;
  font-size: 13px;
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 4px 16px;
  dt { color: var(--el-text-color-secondary); }
  dd { margin: 0; color: var(--text-2); }
}

.detail-actions {
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}
.reject-reason-input {
  margin-bottom: 12px;
}
.action-buttons {
  display: flex;
  gap: 12px;
}
</style>
