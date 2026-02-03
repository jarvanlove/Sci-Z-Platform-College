<!--
/**
 * @description 语言切换组件
 * 支持中文、英文、韩文、日文切换
 */
-->
<template>
  <el-dropdown @command="handleLanguageChange" @visible-change="handleVisibleChange" trigger="click">
    <el-button type="text" class="language-switcher" :class="{ 'compact': compact }">
      <el-icon class="language-icon">
        <svg viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg" class="lang-icon-svg" shape-rendering="geometricPrecision">
          <!-- 文：汉字同字号下视觉较大，单独设 16 -->
          <text class="lang-char-wen" x="1" y="14" font-family="sans-serif" font-size="16" font-weight="600" fill="currentColor">文</text>
          <!-- A：字母同字号下视觉偏小，单独放大以保证与文视觉一致 -->
          <text class="lang-char-a" x="15" y="22" font-family="sans-serif" font-size="20" font-weight="600" fill="currentColor">A</text>
        </svg>
      </el-icon>
      <span v-if="!compact">{{ currentLocaleName }}</span>
      <span v-else class="compact-text">{{ currentLocaleShortName }}</span>
      <el-icon class="el-icon--right">
        <ArrowUp v-if="isDropdownOpen" />
        <ArrowDown v-else />
      </el-icon>
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="locale in supportedLocales"
          :key="locale.code"
          :command="locale.code"
          :class="{ 'is-active': currentLocale === locale.code }"
        >
          <span class="locale-flag">{{ locale.flag }}</span>
          <span class="locale-name">{{ locale.name }}</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { supportedLocales, setLocale, getCurrentLocale } from '@/locales'

// 定义 props
const props = defineProps({
  // 紧凑模式：只显示缩写或图标
  compact: {
    type: Boolean,
    default: false
  }
})

const { t } = useI18n()

// 下拉菜单打开状态
const isDropdownOpen = ref(false)

// 处理下拉菜单显示状态变化
const handleVisibleChange = (visible) => {
  isDropdownOpen.value = visible
}

// 当前语言
const currentLocale = computed(() => getCurrentLocale())

// 当前语言显示名称
const currentLocaleName = computed(() => {
  const locale = supportedLocales.find(l => l.code === currentLocale.value)
  return locale ? locale.name : currentLocale.value
})

// 当前语言简短名称（用于紧凑模式）
const currentLocaleShortName = computed(() => {
  const locale = supportedLocales.find(l => l.code === currentLocale.value)
  if (!locale) return currentLocale.value
  
  // 根据语言代码返回简短名称
  const shortNames = {
    'zh-CN': '中文',
    'en-US': 'EN',
    // 暂时注释掉韩语和日语
    // 'ko-KR': 'KR',
    // 'ja-JP': 'JP'
  }
  return shortNames[locale.code] || locale.name.substring(0, 2)
})

// 处理语言切换
const handleLanguageChange = (locale) => {
  setLocale(locale)
  // 可以在这里添加其他逻辑，比如重新加载页面等
}
</script>

<style lang="scss" scoped>
.language-switcher {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  color: var(--text);
  border-radius: 999px;
  background-color: transparent;
  transition: all 0.2s ease;
  
  &:hover {
    color: var(--text);
    background-color: var(--hover);
  }
  
  // 紧凑模式样式
  &.compact {
    padding: 8px 10px;
    gap: 4px;
    min-width: auto;
    
    .compact-text {
      font-size: 12px;
      font-weight: 500;
    }
    
    .language-icon {
      font-size: 14px;
    }
    
    .language-icon .lang-icon-svg {
      width: 24px;
      height: 24px;
    }
    
    .el-icon--right {
      font-size: 12px;
    }
  }
}

.language-icon {
  font-size: 16px;
  color: var(--text);
}

.language-icon .lang-icon-svg {
  width: 28px;
  height: 28px;
  display: block;
}

.locale-flag {
  margin-right: 8px;
  font-size: 16px;
}

.locale-name {
  font-size: 14px;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  
  &.is-active {
    color: var(--text);
    background-color: var(--hover);
  }
  
  &:hover {
    background-color: var(--hover);
  }
}
</style>
