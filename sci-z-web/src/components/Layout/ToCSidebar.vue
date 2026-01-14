<!--
/**
 * @description ToC 产品左侧主导航栏
 * 参考图2设计：左侧主要菜单
 */
-->
<template>
  <div class="toc-sidebar" :class="{ 'is-collapsed': isCollapsed }">
    <!-- 顶部 Logo 区域 -->
    <div class="sidebar-header">
      <div class="logo-area" v-show="!isCollapsed">
        <img src="@/assets/images/logo.svg" alt="Sci-Z Platform" class="logo-img" />
      </div>
      <!-- 折叠/展开按钮 -->
      <el-tooltip
        :content="isCollapsed ? $t('common.expandSidebar') : $t('common.collapseSidebar')"
        placement="right"
        :offset="10"
        :hide-after="0"
        :show-after="200"
        :teleported="true"
        :popper-options="{
          modifiers: [
            {
              name: 'eventListeners',
              options: {
                scroll: false,
                resize: false
              }
            }
          ]
        }"
      >
        <button class="sidebar-toggle-btn" @click="toggleSidebar">
          <el-icon>
            <ArrowRight v-if="isCollapsed" />
            <ArrowLeft v-else />
          </el-icon>
        </button>
      </el-tooltip>
    </div>

    <!-- 新建对话按钮 -->
    <div class="new-chat-section">
      <el-tooltip
        :content="$t('ai.chat.newChat')"
        placement="right"
        :offset="10"
        :hide-after="0"
        :show-after="200"
        :teleported="true"
        :disabled="!isCollapsed"
        :popper-options="{
          modifiers: [
            {
              name: 'eventListeners',
              options: {
                scroll: false,
                resize: false
              }
            }
          ]
        }"
      >
        <button 
          :class="['new-chat-btn', { active: isNewChatActive }]" 
          @click="createNewChat"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span v-show="!isCollapsed">{{ $t('ai.chat.newChat') }}</span>
        </button>
      </el-tooltip>
    </div>

    <!-- 主要功能菜单（仅登录用户可见） -->
    <div v-if="authStore.isLoggedIn" class="main-menu">
      <nav class="menu-list">
        <el-tooltip
          v-for="menu in mainMenus"
          :key="menu.path"
          :content="menu.title"
          placement="right"
          :offset="10"
          :hide-after="0"
          :show-after="200"
          :teleported="true"
          :disabled="!isCollapsed"
          :popper-options="{
            modifiers: [
              {
                name: 'eventListeners',
                options: {
                  scroll: false,
                  resize: false
                }
              }
            ]
          }"
        >
          <a
            :class="['menu-item', { active: isActive(menu.path) }]"
            @click="handleMenuClick(menu)"
          >
            <el-icon class="menu-icon"><component :is="menu.icon" /></el-icon>
            <span class="menu-text" v-show="!isCollapsed">{{ menu.title }}</span>
          </a>
        </el-tooltip>
      </nav>
    </div>

    <!-- 历史对话区域（移到实践下面，仅登录用户可见） -->
    <div v-if="authStore.isLoggedIn" class="chat-history-section">
      <el-tooltip
        :content="$t('ai.chat.history')"
        placement="right"
        :offset="10"
        :hide-after="0"
        :show-after="200"
        :teleported="true"
        :disabled="!isCollapsed"
        :popper-options="{
          modifiers: [
            {
              name: 'eventListeners',
              options: {
                scroll: false,
                resize: false
              }
            }
          ]
        }"
      >
        <div 
          :class="['history-menu-item', { active: false }]"
          v-show="isCollapsed"
        >
          <el-icon class="menu-icon"><Clock /></el-icon>
        </div>
      </el-tooltip>
      <div v-show="!isCollapsed" class="section-header">
        <el-icon class="section-icon"><Clock /></el-icon>
        <span class="section-title">{{ $t('ai.chat.history') }}</span>
      </div>
      <BaseScrollbar
        v-show="!isCollapsed"
        class="chat-history-list"
        size="small"
      >
        <div
          v-for="chat in chatHistory"
          :key="chat.id"
          :class="['chat-history-item', { active: currentChatId === chat.id, pinned: chat.pinned }]"
          @click="selectChat(chat)"
        >
          <div class="chat-item-content">
            <div class="chat-item-title-wrapper">
              <div class="chat-item-title">
                <el-icon v-if="chat.pinned" class="pin-icon"><StarFilled /></el-icon>
                <span class="title-text">{{ chat.title || $t('ai.chat.newChat') }}</span>
              </div>
              <el-dropdown 
                trigger="click" 
                placement="bottom-end"
                @command="(command) => handleChatAction(command, chat)"
                @click.stop
              >
                <button class="more-btn" @click.stop>
                  <el-icon><MoreFilled /></el-icon>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="'edit'">
                      <el-icon><EditPen /></el-icon>
                      <span>{{ $t('ai.chat.rename') }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item :command="chat.pinned ? 'unpin' : 'pin'">
                      <el-icon><StarFilled /></el-icon>
                      <span>{{ chat.pinned ? $t('ai.chat.unpin') : $t('ai.chat.pin') }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item :command="'delete'" divided>
                      <el-icon><Delete /></el-icon>
                      <span>{{ $t('ai.chat.delete') }}</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div class="chat-item-preview">{{ chat.preview || '' }}</div>
            <div class="chat-item-time">{{ formatTime(chat.updatedAt) }}</div>
          </div>
        </div>
        <div v-if="chatHistory.length === 0" class="empty-history">
          {{ $t('ai.chat.noHistory') }}
        </div>
      </BaseScrollbar>
    </div>

    <!-- 底部用户信息区域（固定在左下角） -->
    <div class="sidebar-footer">
      <div v-if="authStore.isLoggedIn" class="user-info-area">
        <div class="user-info-wrapper">
          <el-dropdown 
            @command="handleUserCommand" 
            trigger="click" 
            :placement="isCollapsed ? 'right' : 'top-start'"
            @visible-change="handleDropdownVisibleChange"
          >
            <div class="user-info">
              <el-avatar :size="32" :src="avatarUrl">
                {{ userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
              </el-avatar>
              <div class="user-details" v-show="!isCollapsed">
                <div class="username">{{ userInfo?.username || 'User' }}</div>
              </div>
              <el-icon class="dropdown-icon" v-show="!isCollapsed">
                <ArrowUp v-if="isDropdownOpen" />
                <ArrowDown v-else />
              </el-icon>
            </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                {{ $t('user.profile.menu') }}
              </el-dropdown-item>
              <el-dropdown-item command="security">
                <el-icon><Lock /></el-icon>
                {{ $t('user.security.menu') }}
              </el-dropdown-item>
              <el-dropdown-item command="manual">
                <el-icon><Reading /></el-icon>
                {{ $t('manual.title') }}
              </el-dropdown-item>
              <!-- 系统设置（嵌套下拉菜单） -->
              <el-dropdown 
                v-if="isAdmin" 
                divided
                trigger="hover"
                placement="right-start"
                @command="handleSystemCommand"
                popper-class="system-submenu-popper"
              >
                <template #default>
                  <div class="system-menu-trigger">
                    <el-icon><Setting /></el-icon>
                    <span class="system-menu-text">{{ $t('menu.system') }}</span>
                  </div>
                </template>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="user">
                      <el-icon><User /></el-icon>
                      {{ $t('menu.systemUser') }}
                    </el-dropdown-item>
                    <el-dropdown-item command="role">
                      <el-icon><Key /></el-icon>
                      {{ $t('menu.systemRole') }}
                    </el-dropdown-item>
                    <el-dropdown-item command="logs">
                      <el-icon><Document /></el-icon>
                      {{ $t('menu.systemLogs') }}
                    </el-dropdown-item>
                    <el-dropdown-item command="apikey">
                      <el-icon><Key /></el-icon>
                      {{ $t('menu.apiKey') }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-dropdown-item command="theme">
                <el-icon><Sunny v-if="isDark" /><Moon v-else /></el-icon>
                {{ $t('theme.switchTheme') }}
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                {{ $t('user.logout') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <!-- 🔥 修改：i18n切换按钮放在用户信息右边 -->
        <div v-show="!isCollapsed" class="language-switcher-inline">
          <LanguageSwitcher />
        </div>
        </div>
      </div>
      
      <!-- 展开时显示登录按钮 -->
      <div v-if="!authStore.isLoggedIn" v-show="!isCollapsed" class="login-prompt">
        <button class="login-btn" @click="goToLogin">
          <el-icon><User /></el-icon>
          <span>{{ $t('user.login') }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/store/modules/auth'
import { useAppStore } from '@/store/modules/app'
import { ElMessage } from 'element-plus'
import { useLoginModal } from '@/composables/useLoginModal'
import { BaseScrollbar } from '@/components/Common'
import LanguageSwitcher from '@/components/Common/LanguageSwitcher.vue'
import {
  Plus,
  ChatDotRound,
  Search,
  Collection,
  Tools,
  ArrowDown,
  ArrowUp,
  ArrowLeft,
  ArrowRight,
  User,
  Lock,
  Setting,
  SwitchButton,
  Clock,
  EditPen,
  StarFilled,
  Delete,
  Top,
  MoreFilled,
  Reading,
  Sunny,
  Moon,
  Document
} from '@element-plus/icons-vue'
import {
  pageAiConversations,
  createAiConversation,
  updateAiConversation,
  deleteAiConversation,
  updateAiConversationPinnedStatus
} from '@/api/AI/ai'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const authStore = useAuthStore()
const appStore = useAppStore()
const { openLoginModal } = useLoginModal()

// 🔥 修复：不再使用 provide/inject（因为 ToCSidebar 和 AIChat 是兄弟组件）
// 改用事件系统进行通信

// 侧边栏折叠状态（使用独立的 ToC 侧边栏折叠状态）
const tocSidebarCollapsed = ref(false)

// 计算属性：是否折叠（暴露给父组件）
const isCollapsed = computed(() => tocSidebarCollapsed.value)

// 暴露给父组件访问
defineExpose({
  isCollapsed
})

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  tocSidebarCollapsed.value = !tocSidebarCollapsed.value
  // 可选：同步到 appStore（如果需要全局状态）
  // appStore.setSidebarCollapsed(tocSidebarCollapsed.value)
}

// 主要功能菜单配置
const mainMenus = computed(() => {
  const menus = [
    {
      path: '/literature/search',
      title: t('menu.literature'),
      icon: Search,
      requiresAuth: false // 不需要登录即可访问
    },
    {
      path: '/knowledge/list',
      title: t('menu.knowledge'),
      icon: Collection,
      requiresAuth: true // 需要登录
    },
    {
      path: '/practice',
      title: t('menu.practice'),
      icon: Tools,
      requiresAuth: true // 需要登录
    }
  ]

  // 过滤需要登录但用户未登录的菜单（暂时不隐藏，点击时提示登录）
  return menus
})

// 用户信息
const userInfo = computed(() => authStore.userInfo)
const avatarUrl = computed(() => userInfo.value?.avatar || '')
const isAdmin = computed(() => {
  // 🔥 修复：接口返回的 roles 是字符串数组，如 ["admin"]
  return authStore.roles?.includes('admin') || false
})
const userRole = computed(() => {
  if (!authStore.roles || authStore.roles.length === 0) return ''
  return authStore.roles[0]?.name || ''
})

// 主题状态
const isDark = computed(() => appStore.theme === 'dark')

// 当前激活的菜单
const isActive = (path) => {
  return route.path.startsWith(path)
}

// 菜单点击处理
const handleMenuClick = async (menu) => {
  // 如果菜单需要登录但用户未登录，显示登录弹窗
  if (menu.requiresAuth && !authStore.isLoggedIn) {
    ElMessage.warning(t('user.pleaseLogin'))
    openLoginModal()
    return
  }

  // 🔥 修复：清除历史对话的选中状态
  currentChatId.value = null
  sessionStorage.removeItem('currentConversationId')
  storedConversationId.value = null

  // 跳转到对应页面
  router.push(menu.path)
}

// 创建新对话
const createNewChat = async () => {
  // 如果未登录，显示登录弹窗
  if (!authStore.isLoggedIn) {
    openLoginModal()
    return
  }

  // 如果当前在 AI 对话页面，使用事件系统触发新建对话
  // 🔥 修复：ToCSidebar 和 AIChat 是兄弟组件，无法使用 provide/inject，改用事件系统
  if (route.path === '/ai/chat') {
    // 触发自定义事件，让 AIChat 组件监听并处理
    window.dispatchEvent(new CustomEvent('createNewChatRequest', { detail: {} }))
    return
  }

  // 如果不在 AI 对话页面，直接跳转
  router.push('/ai/chat').catch((err) => {
    // 忽略路由冗余导航错误
    if (err.name !== 'NavigationDuplicated' && err.name !== 'NavigationCancelled') {
      console.error('路由跳转失败', err)
    }
  })
}

// 处理下拉菜单展开/收起状态
const handleDropdownVisibleChange = (visible) => {
  isDropdownOpen.value = visible
}

// 处理系统设置二级菜单命令
const handleSystemCommand = (command) => {
  isDropdownOpen.value = false
  // 🔥 修复：清除历史对话的选中状态
  currentChatId.value = null
  sessionStorage.removeItem('currentConversationId')
  storedConversationId.value = null
  
  const routes = {
    user: '/system/user',
    role: '/system/role',
    logs: '/system/logs',
    apikey: '/system/apikey'
  }
  
  if (routes[command]) {
    router.push(routes[command])
  }
}

// 对话历史
const chatHistory = ref([])
const currentChatId = ref(null)
const isDropdownOpen = ref(false) // 下拉菜单是否展开

// 处理聊天操作菜单命令
const handleChatAction = (command, chat) => {
  switch (command) {
    case 'edit':
      editChatTitle(chat)
      break
    case 'pin':
      togglePinChat(chat)
      break
    case 'unpin':
      togglePinChat(chat)
      break
    case 'delete':
      deleteChatConfirm(chat)
      break
  }
}

// 🔥 修复：使用响应式的 ref 来跟踪 sessionStorage 中的对话ID
const storedConversationId = ref(sessionStorage.getItem('currentConversationId'))

// 计算属性：判断是否是最新对话（没有 conversationId 或为空）
const isNewChatActive = computed(() => {
  // 🔥 修复：使用响应式的 ref 而不是直接读取 sessionStorage
  const storedId = storedConversationId.value || sessionStorage.getItem('currentConversationId')
  const hasNoStoredId = !storedId || 
                         storedId === '' || 
                         storedId === 'null' ||
                         storedId === 'undefined'
  const hasNoCurrentChatId = !currentChatId.value
  
  // 只有在没有存储的对话ID且没有当前选中的对话ID时，才显示"新建对话"为选中状态
  return hasNoStoredId && hasNoCurrentChatId
})

// 加载对话历史
const loadChatHistory = async () => {
  if (!authStore.isLoggedIn) {
    chatHistory.value = []
    return
  }

  try {
    const response = await pageAiConversations({ pageNo: 1, pageSize: 100, sortBy: 'pinned', sortOrder: 'DESC' })
    if (response.code === 200 && response.data) {
      const records = response.data.records || response.data.list || []
      // 先按置顶状态排序，再按更新时间排序
      const sortedRecords = records.sort((a, b) => {
        // API返回的是 isPinned (0或1)，也可能是 pinned (boolean)
        const aPinned = (a.isPinned === 1 || a.isPinned === true || a.pinned === true || a.pinned === 1) ? 1 : 0
        const bPinned = (b.isPinned === 1 || b.isPinned === true || b.pinned === true || b.pinned === 1) ? 1 : 0
        if (aPinned !== bPinned) {
          return bPinned - aPinned
        }
        const aTime = new Date(a.updatedTime || a.updatedAt || a.createdTime || a.createdAt).getTime()
        const bTime = new Date(b.updatedTime || b.updatedAt || b.createdTime || b.createdAt).getTime()
        return bTime - aTime
      })
      chatHistory.value = sortedRecords.map(chat => ({
        id: chat.id,
        title: chat.title || t('ai.chat.newChat'),
        preview: chat.lastMessage || '',
        updatedAt: chat.updatedTime || chat.updatedAt || chat.createdTime || chat.createdAt,
        // API返回的是 isPinned (0或1)，转换为 boolean
        pinned: chat.isPinned === 1 || chat.isPinned === true || chat.pinned === true || chat.pinned === 1
      }))
    }
  } catch (error) {
    console.error('加载对话历史失败', error)
    chatHistory.value = []
  }
}

// 选择对话
const selectChat = (chat) => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning(t('user.pleaseLogin'))
    openLoginModal()
    return
  }

  const conversationIdStr = String(chat.id)
  currentChatId.value = conversationIdStr
  // 🔥 修复：只使用 sessionStorage 存储对话ID，不在路由中显示
  sessionStorage.setItem('currentConversationId', conversationIdStr)
  // 🔥 修复：更新响应式的 ref，触发计算属性重新计算
  storedConversationId.value = conversationIdStr
  
  // 🔥 修复：如果当前在 AI 对话页面，立即触发事件通知主组件加载对话
  if (route.path === '/ai/chat') {
    // 触发自定义事件，通知主组件立即加载对话
    window.dispatchEvent(new CustomEvent('chatSelected', { detail: { chatId: chat.id } }))
  } else {
    // 如果不在 AI 对话页面，跳转过去（不带参数）
    router.push({ path: '/ai/chat' })
  }
}

// 格式化时间
const formatTime = (date) => {
  if (!date) return ''
  
  const d = typeof date === 'string' || typeof date === 'number'
    ? new Date(date)
    : date
  
  if (isNaN(d.getTime())) return ''
  
  const now = new Date()
  const diff = now - d
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 1) return t('ai.chat.justNow')
  if (minutes < 60) return `${minutes}${t('common.minuteAgo')}`
  if (hours < 24) return `${hours}${t('common.hourAgo')}`
  if (days < 7) return `${days}${t('common.dayAgo')}`
  
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  return `${month}-${day}`
}

// 编辑对话标题
const editChatTitle = async (chat) => {
  try {
    const { value: newTitle } = await ElMessageBox.prompt(
      t('ai.chat.enterNewTitle'),
      t('ai.chat.editTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        inputValue: chat.title || '',
        customClass: 'edit-title-dialog'
      }
    )
    
    if (newTitle && newTitle.trim()) {
      try {
        await updateAiConversation({ id: chat.id, title: newTitle.trim() })
        chat.title = newTitle.trim()
        ElMessage.success(t('ai.chat.titleUpdated'))
      } catch (error) {
        console.error('更新对话标题失败', error)
        ElMessage.error(t('ai.chat.updateTitleFailed'))
      }
    }
  } catch (error) {
    // 用户取消
  }
}

// 切换置顶状态
const togglePinChat = async (chat) => {
  try {
    const newPinnedStatus = !chat.pinned
    await updateAiConversationPinnedStatus(chat.id, newPinnedStatus ? 1 : 0)
    chat.pinned = newPinnedStatus
    // 重新排序
    chatHistory.value.sort((a, b) => {
      const aPinned = a.pinned ? 1 : 0
      const bPinned = b.pinned ? 1 : 0
      if (aPinned !== bPinned) {
        return bPinned - aPinned
      }
      const aTime = new Date(a.updatedAt).getTime()
      const bTime = new Date(b.updatedAt).getTime()
      return bTime - aTime
    })
    ElMessage.success(newPinnedStatus ? t('ai.chat.chatPinned') : t('ai.chat.chatUnpinned'))
  } catch (error) {
    console.error('更新置顶状态失败', error)
    ElMessage.error(t('ai.chat.operationFailed'))
  }
}

// 删除对话确认
const deleteChatConfirm = async (chat) => {
  try {
    await ElMessageBox.confirm(
      t('ai.chat.deleteChatConfirm') || '确定要删除这个对话吗？',
      t('ai.chat.deleteChatTitle') || '删除对话',
      {
        confirmButtonText: t('common.confirm') || '确定',
        cancelButtonText: t('common.cancel') || '取消',
        type: 'warning'
      }
    )
    
    try {
      await deleteAiConversation(chat.id)
      const index = chatHistory.value.findIndex(c => c.id === chat.id)
      if (index !== -1) {
        chatHistory.value.splice(index, 1)
      }
      if (currentChatId.value === chat.id) {
        currentChatId.value = null
      }
      ElMessage.success(t('ai.chat.chatDeleted'))
    } catch (error) {
      console.error('删除对话失败', error)
      ElMessage.error(t('ai.chat.deleteFailed'))
    }
  } catch (error) {
    // 用户取消
  }
}

// 用户命令处理
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      // 🔥 修复：清除历史对话的选中状态
      currentChatId.value = null
      sessionStorage.removeItem('currentConversationId')
      storedConversationId.value = null
      router.push('/user/profile')
      break
    case 'security':
      // 🔥 修复：清除历史对话的选中状态
      currentChatId.value = null
      sessionStorage.removeItem('currentConversationId')
      storedConversationId.value = null
      router.push('/user/security')
      break
    case 'manual':
      // 🔥 修复：清除历史对话的选中状态
      currentChatId.value = null
      sessionStorage.removeItem('currentConversationId')
      storedConversationId.value = null
      router.push('/manual')
      break
    case 'system':
      // 系统设置不再跳转，而是显示二级菜单（通过鼠标悬浮触发）
      // 这里不做任何操作，二级菜单通过嵌套的 el-dropdown 控制显示
      break
    case 'theme':
      // 切换主题（appStore.toggleTheme() 内部已经会调用 applyTheme 更新 DOM）
      appStore.toggleTheme()
      break
    case 'logout':
      // 退出登录，保持在当前页面（不跳转到登录页）
      authStore.logout({ redirect: false })
      // 清除对话ID参数，跳转到AI对话页面（不带参数）
      if (route.path === '/ai/chat') {
        // 使用 replace 清除 conversationId 参数
        router.replace({ path: '/ai/chat', query: {} })
      } else {
        // 如果当前不在公开页面，跳转到AI对话页面（公开页面）
        const publicPages = ['/ai/chat', '/literature/search']
        if (!publicPages.includes(route.path)) {
          router.push('/ai/chat')
        }
      }
      break
  }
}

// 显示登录弹窗
const goToLogin = () => {
  openLoginModal()
}

// 🔥 修复：监听 sessionStorage 变化，更新当前对话ID和响应式ref
const checkConversationId = () => {
  const storedId = sessionStorage.getItem('currentConversationId')
  // 更新响应式的 ref，触发计算属性重新计算
  storedConversationId.value = storedId
  if (storedId && storedId !== 'null' && storedId !== 'undefined') {
    currentChatId.value = storedId
  } else {
    currentChatId.value = null
  }
}

// 监听 storage 事件（跨标签页同步）
window.addEventListener('storage', (e) => {
  if (e.key === 'currentConversationId') {
    checkConversationId()
  }
})

// 初始检查
checkConversationId()

// 🔥 修复：定期检查 sessionStorage（因为同标签页的 storage 事件不会触发）
// 同时监听自定义的 storage 事件（AIChat 组件会触发）
setInterval(() => {
  checkConversationId()
}, 500)

// 🔥 修复：监听登录状态，只在真正登录成功时清除对话ID，刷新页面时保留
watch(() => authStore.isLoggedIn, (isLoggedIn, wasLoggedIn) => {
  if (isLoggedIn) {
    loadChatHistory()
    // 🔥 修复：只有在真正登录成功时（从未登录变为已登录）才清除对话ID
    // 刷新页面时 wasLoggedIn 可能是 undefined，不应该清除
    // 只有当 wasLoggedIn 明确为 false 时，才是真正的登录成功
    if (wasLoggedIn === false) {
      // 真正登录成功，清除之前的对话ID，默认选中新建对话
      const storedId = sessionStorage.getItem('currentConversationId')
      // 只有在 sessionStorage 中没有对话ID时，才清除（避免清除刷新页面时的状态）
      if (!storedId || storedId === 'null' || storedId === 'undefined') {
        sessionStorage.removeItem('currentConversationId')
        currentChatId.value = null
        storedConversationId.value = null
        console.log('[ToCSidebar] 用户登录成功，清除之前的对话ID，默认选中新建对话')
      } else {
        // 刷新页面时，保留 sessionStorage 中的对话ID
        currentChatId.value = storedId
        storedConversationId.value = storedId
        console.log('[ToCSidebar] 刷新页面，保留 sessionStorage 中的对话ID', storedId)
      }
    }
    // 🔥 修复：刷新页面时（wasLoggedIn 可能是 undefined），不执行任何操作，保留 sessionStorage 中的状态
  } else {
    chatHistory.value = []
    currentChatId.value = null
    sessionStorage.removeItem('currentConversationId')
    storedConversationId.value = null // 🔥 修复：更新响应式的 ref
  }
}, { immediate: true })

// 🔥 修复：监听 chatCreated 事件（在组件挂载时设置，确保能接收到事件）
const handleChatCreated = async (e) => {
  const conversationId = e.detail?.conversationId
  if (conversationId) {
    console.log('[ToCSidebar] 收到 chatCreated 事件，刷新对话列表', conversationId)
    // 刷新对话列表，确保新创建的对话出现在历史对话中
    await loadChatHistory()
    // 更新选中状态
    const conversationIdStr = String(conversationId)
    currentChatId.value = conversationIdStr
    // 确保 sessionStorage 也更新
    sessionStorage.setItem('currentConversationId', conversationIdStr)
    // 🔥 修复：更新响应式的 ref，触发计算属性重新计算
    storedConversationId.value = conversationIdStr
    console.log('[ToCSidebar] 对话列表已刷新，已选中对话', conversationId)
  }
}

// 🔥 修复：监听 chatCleared 事件，清除选中状态，显示"新建对话"为选中
const handleChatCleared = () => {
  console.log('[ToCSidebar] 收到 chatCleared 事件，清除选中状态')
  currentChatId.value = null
  storedConversationId.value = null
  sessionStorage.removeItem('currentConversationId')
  console.log('[ToCSidebar] 已清除选中状态，显示"新建对话"为选中')
}

// 监听主题变化，确保 DOM 更新（应用到整个系统）
watch(() => appStore.theme, (newTheme) => {
  const html = document.documentElement
  if (newTheme === 'dark') {
    html.classList.add('dark')
    html.setAttribute('data-theme', 'dark')
  } else {
    html.classList.remove('dark')
    html.setAttribute('data-theme', 'light')
  }
}, { immediate: true })

// 组件挂载时加载对话历史
onMounted(() => {
  // 🔥 修复：确保主题在组件挂载时正确应用
  const html = document.documentElement
  const currentTheme = appStore.theme
  if (currentTheme === 'dark') {
    html.classList.add('dark')
    html.setAttribute('data-theme', 'dark')
  } else {
    html.classList.remove('dark')
    html.setAttribute('data-theme', 'light')
  }
  
  if (authStore.isLoggedIn) {
    loadChatHistory()
  }
  
  // 🔥 修复：初始化响应式的 ref
  const storedId = sessionStorage.getItem('currentConversationId')
  storedConversationId.value = storedId
  
  // 🔥 修复：如果 sessionStorage 中有对话ID（刷新页面时），应该读取并设置
  // 只有在真正登录成功时（从未登录变为已登录）才清除，刷新页面时不应该清除
  if (storedId && storedId !== 'null' && storedId !== 'undefined') {
    // 刷新页面时，恢复之前选中的对话
    currentChatId.value = storedId
    console.log('[ToCSidebar] 刷新页面，恢复之前选中的对话', storedId)
  } else {
    // 没有对话ID，默认选中新建对话
    currentChatId.value = null
  }
  
  // 🔥 修复：在组件挂载时添加事件监听器
  window.addEventListener('chatCreated', handleChatCreated)
  window.addEventListener('chatCleared', handleChatCleared)
})

// 组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('chatCreated', handleChatCreated)
  window.removeEventListener('chatCleared', handleChatCleared)
})
</script>

<style lang="scss" scoped>
.toc-sidebar {
  width: 280px;
  height: 100vh;
  background: var(--surface);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  &.is-collapsed {
    width: 64px;
  }
}

// Logo 区域
.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid var(--border);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 64px; // 确保折叠时也有足够高度

  .toc-sidebar.is-collapsed & {
    padding: 12px 8px;
    justify-content: center;
    min-height: 48px;
    padding-bottom: 8px; // 减少底部间距，与新建对话按钮保持合适距离
  }
}

// 折叠/展开按钮（参考千问AI设计：简洁的矩形按钮）
.sidebar-toggle-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  background: #f8f9fa;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 1000;
  color: var(--text-3);
  padding: 0;

  .toc-sidebar.is-collapsed & {
    position: static;
    margin: 0;
    top: auto;
    right: auto;
    width: 100%;
    max-width: 100%;
  }

  &:hover {
    background: #f1f3f5;
    border-color: #d1d5db;
    color: var(--text);
  }

  &:active {
    background: #e9ecef;
    transform: scale(0.95);
  }

  .el-icon {
    font-size: 16px;
    transition: transform 0.2s ease;
  }
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-img {
  width: auto;
  height: 32px;
  max-width: 100%;
  object-fit: contain;
}

// 新建对话按钮
.new-chat-section {
  padding: 16px;
  border-bottom: 1px solid var(--border);

  .toc-sidebar.is-collapsed & {
    padding: 8px;
    padding-top: 4px; // 折叠时减少顶部间距，与折叠按钮保持合适距离
  }
}

.new-chat-btn {
  width: 100%;
  background: transparent;
  color: var(--text);
  border: none;
  padding: 12px 16px 12px 12px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 6px;
  transition: all 0.2s ease;
  text-decoration: none;

  .toc-sidebar.is-collapsed & {
    padding: 12px;
    justify-content: center;
  }

  .el-icon {
    font-size: 18px;
    color: var(--text-3);
    flex-shrink: 0;
  }

  span {
    flex: 1;
  }

  &:hover {
    background: var(--hover-light);
  }

  &:active {
    transform: none;
  }

  &.active {
    background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
    color: var(--color-primary);
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(30, 58, 138, 0.08);

    .el-icon {
      color: var(--color-primary);
    }
  }
}

// 主要菜单
.main-menu {
  flex: 0 0 auto;
  overflow-y: auto;
  padding: 8px;
  margin-bottom: 0;
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;

  .toc-sidebar.is-collapsed & {
    padding: 12px;
    justify-content: center;
  }

  &:hover {
    background: var(--hover-light);
  }

  &.active {
    background: var(--hover);
    color: var(--color-primary);
    font-weight: 600;

    .menu-icon {
      color: var(--color-primary);
    }
  }
}

.menu-icon {
  font-size: 18px;
  color: var(--text-3);
  flex-shrink: 0;
}

.menu-text {
  flex: 1;
}

// 历史对话区域
.chat-history-section {
  padding: 8px;
  max-height: 300px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  flex: 0 0 auto;
  margin-top: 0;
}

.history-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  justify-content: center;
  margin-bottom: 4px;

  &:hover {
    background: var(--hover-light);
  }
}

.section-header {
  margin-bottom: 8px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-icon {
  font-size: 18px;
  color: var(--text-3);
  flex-shrink: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
}

.chat-history-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-history-item {
  display: flex;
  align-items: center;
  padding: 8px 12px 8px 46px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  gap: 8px;

  &:hover {
    background: var(--hover-light);
  }

  &.active {
    background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  }

  &.pinned {
    .chat-item-title {
      font-weight: 600;
    }
  }
}

.chat-item-content {
  flex: 1;
  min-width: 0;
}

.chat-item-title-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.chat-item-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  color: var(--text-3);
  transition: all 0.2s ease;
  padding: 0;
  flex-shrink: 0;
  opacity: 0;

  .el-icon {
    font-size: 16px;
  }

  &:hover {
    background: #e5e7eb;
    color: var(--text);
  }
}

.chat-history-item:hover .more-btn {
  opacity: 1;
}

.pin-icon {
  font-size: 14px;
  color: #fbbf24;
  flex-shrink: 0;
  margin-right: 6px;
}

.chat-item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  color: var(--text-3);
  transition: all 0.2s ease;
  padding: 0;

  .el-icon {
    font-size: 14px;
  }

  &:hover {
    background: #e5e7eb;
    color: var(--text);
  }

  &.delete-btn:hover {
    background: #fee2e2;
    color: #dc2626;
  }

  &.pinned-btn {
    color: #fbbf24;

    &:hover {
      background: #fef3c7;
      color: #f59e0b;
    }
  }
}

.chat-item-preview {
  font-size: 12px;
  color: var(--text-3);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item-time {
  font-size: 11px;
  color: #d1d5db;
}

.empty-history {
  font-size: 12px;
  color: var(--text-3);
  text-align: center;
  padding: 20px;
}

// 底部用户区域（固定在左下角）
.sidebar-footer {
  padding: 0;
  border-top: 1px solid #f3f4f6;
  background: var(--surface);
  margin-top: auto; // 推到底部
  flex-shrink: 0; // 防止被压缩
}

.login-prompt {
  width: 100%;
  padding: 16px;
}

.user-info-area {
  width: 100%;
  padding: 12px 16px 16px 16px;
}

.user-info-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 50px !important;
  transition: background 0.2s ease;
  flex: 1;
  box-sizing: border-box;
  background-color: var(--hover-light);

  .toc-sidebar.is-collapsed & {
    justify-content: center;
    padding: 8px;
  }

  &:hover {
    background-color: var(--hover);
  }
}

.language-switcher-inline {
  flex-shrink: 0;
  
  :deep(.language-switcher) {
    padding: 8px 12px;
    color: #374151 !important;
    border-radius: 50px !important;
    background-color: var(--hover-light);
    min-height: 48px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    
    .language-icon {
      color: #374151 !important;
    }
    
    &:hover {
      color: #374151 !important;
      background-color: var(--hover);
      
      .language-icon {
        color: #374151 !important;
      }
    }
  }
  
  :deep(.el-dropdown-menu__item.is-active) {
    color: var(--text) !important;
  }
}

/* 下拉菜单容器样式（参考Header.vue） */
:deep(.el-dropdown-menu) {
  background-color: var(--surface) !important;
  border: 1px solid var(--border) !important;
  border-radius: 8px !important;
  padding: 4px 0 !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
}

/* 下拉菜单项样式（参考Header.vue，保持清爽的字体样式） */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: "Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif !important;
  font-size: 15px !important;
  font-weight: 400 !important;
  color: var(--text-2) !important;
  letter-spacing: 0.01em !important;
  padding: 0 20px !important;
  height: 48px !important;
  line-height: 48px !important;
  margin: 2px 8px !important;
  border-radius: 6px !important;
  transition: all 0.2s ease !important;

  .el-icon {
    font-size: 16px !important;
    margin-right: 8px !important;
    color: var(--text-2) !important;
    transition: color 0.2s ease !important;
  }

  &:hover {
    background-color: var(--hover) !important;
    color: var(--color-primary) !important;

    .el-icon {
      color: var(--color-primary) !important;
    }
  }

  /* 分隔线样式 */
  &.is-divided {
    border-top: 1px solid var(--border) !important;
    margin-top: 4px !important;
    padding-top: 4px !important;
  }
}

.user-details {
  flex: 1;
  min-width: 0;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.dropdown-icon {
  font-size: 14px;
  color: var(--text-3);
  flex-shrink: 0;
  transition: color 0.2s ease;
}

.user-info:hover .dropdown-icon {
  color: var(--text);
}

// 系统设置嵌套下拉菜单样式（与其他菜单项对齐）
.system-menu-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text-2);
  font-size: 15px;
  font-weight: 400;
  font-family: "Inter", "SF Pro Display", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  letter-spacing: 0.01em;
  padding: 0 20px;
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
  border-top: 1px solid var(--border);
  margin-top: 4px;
  
  &:hover {
    background-color: var(--hover);
    color: var(--color-primary);
    
    .el-icon {
      color: var(--color-primary);
    }
  }
  
  .el-icon {
    font-size: 16px;
    margin-right: 8px;
    color: var(--text-2);
    transition: color 0.2s ease;
  }
  
  .system-menu-text {
    flex: 1;
  }
}

// 系统设置二级菜单的 popper 样式（稍微紧凑一点）
:deep(.system-submenu-popper) {
  margin-left: 4px !important;
  
  .el-dropdown-menu {
    padding: 2px 0 !important;
    margin: 0 !important;
  }
  
  .el-dropdown-menu__item {
    height: 44px !important;
    line-height: 44px !important;
    margin: 1px 6px !important;
    padding: 0 16px !important;
  }
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;

  .toc-sidebar.is-collapsed & {
    padding: 10px;
    justify-content: center;
  }

  &:hover {
    background: linear-gradient(135deg, #1e40af 0%, #2563eb 100%);
    transform: translateY(-1px);
  }
}

// 滚动条样式
.main-menu::-webkit-scrollbar {
  width: 6px;
}

.main-menu::-webkit-scrollbar-track {
  background: transparent;
}

.main-menu::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;

  &:hover {
    background: #9ca3af;
  }
}

// 编辑标题对话框样式（匹配原型图）
:deep(.edit-title-dialog) {
  border-radius: 8px !important;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  border: none !important;
  width: 400px !important;
}

:deep(.edit-title-dialog .el-message-box__header) {
  background: white !important;
  padding: 20px 24px 16px 24px !important;
  border-bottom: 1px solid #f0f0f0 !important;
  position: relative !important;
}

:deep(.edit-title-dialog .el-message-box__title) {
  color: #1e3a8a !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  line-height: 1.5 !important;
}

:deep(.edit-title-dialog .el-message-box__headerbtn) {
  top: 20px !important;
  right: 24px !important;
  width: 20px !important;
  height: 20px !important;
}

:deep(.edit-title-dialog .el-message-box__close) {
  color: #6b7280 !important;
  font-size: 18px !important;
}

:deep(.edit-title-dialog .el-message-box__close:hover) {
  color: #374151 !important;
}

:deep(.edit-title-dialog .el-message-box__content) {
  padding: 20px 24px !important;
  background: white !important;
  display: flex !important;
  flex-direction: column !important;
  align-items: flex-start !important;
}

:deep(.edit-title-dialog .el-message-box__message) {
  font-size: 14px !important;
  line-height: 1.5 !important;
  color: #6b7280 !important;
  margin: 0 0 16px 0 !important;
  padding: 0 !important;
  width: 100% !important;
  order: 1 !important;
}

:deep(.edit-title-dialog .el-message-box__input) {
  margin-top: 0 !important;
  padding-top: 0 !important;
  width: 100% !important;
  order: 2 !important;
}

:deep(.edit-title-dialog .el-input) {
  width: 100% !important;
}

:deep(.edit-title-dialog .el-input__wrapper) {
  border-radius: 6px !important;
  border: 1px solid #d1d5db !important;
  box-shadow: none !important;
  padding: 8px 12px !important;
  background-color: #ffffff !important;
}

:deep(.edit-title-dialog .el-input__wrapper:hover) {
  border-color: #9ca3af !important;
  box-shadow: none !important;
}

:deep(.edit-title-dialog .el-input__wrapper.is-focus) {
  border-color: #1e3a8a !important;
  box-shadow: none !important;
  outline: none !important;
}

:deep(.edit-title-dialog .el-input__wrapper.is-focus:hover) {
  box-shadow: none !important;
}

:deep(.edit-title-dialog .el-input__wrapper.is-focus .el-input__inner) {
  outline: none !important;
  box-shadow: none !important;
}

:deep(.edit-title-dialog .el-input__inner) {
  outline: none !important;
  box-shadow: none !important;
}

:deep(.edit-title-dialog .el-input__inner:focus) {
  outline: none !important;
  box-shadow: none !important;
}

:deep(.edit-title-dialog .el-input__inner) {
  font-size: 14px !important;
  color: #374151 !important;
  height: auto !important;
  line-height: 1.5 !important;
}

:deep(.edit-title-dialog .el-input__inner::placeholder) {
  color: #9ca3af !important;
}

:deep(.edit-title-dialog .el-message-box__btns) {
  padding: 16px 24px 20px 24px !important;
  background: white !important;
  display: flex !important;
  justify-content: flex-end !important;
  gap: 12px !important;
  border-top: none !important;
  margin-top: 0 !important;
}

:deep(.edit-title-dialog .el-message-box__btns .el-button) {
  padding: 8px 20px !important;
  border-radius: 6px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  border: 1px solid !important;
  transition: all 0.2s ease !important;
  min-width: 80px !important;
  height: 36px !important;
}

:deep(.edit-title-dialog .el-message-box__btns .el-button--default) {
  background: white !important;
  border-color: #d1d5db !important;
  color: #374151 !important;
}

:deep(.edit-title-dialog .el-message-box__btns .el-button--default:hover) {
  background: #f9fafb !important;
  border-color: #9ca3af !important;
  color: #374151 !important;
}

:deep(.edit-title-dialog .el-message-box__btns .el-button--primary) {
  background: #1e3a8a !important;
  border-color: #1e3a8a !important;
  color: white !important;
}

:deep(.edit-title-dialog .el-message-box__btns .el-button--primary:hover) {
  background: #1e40af !important;
  border-color: #1e40af !important;
  color: white !important;
}
</style>

