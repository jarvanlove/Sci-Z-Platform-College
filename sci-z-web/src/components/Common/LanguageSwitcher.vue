<!--
/**
 * @description 语言切换组件
 * 支持中文、英文、韩文、日文切换
 */
-->
<template>
  <el-dropdown @command="handleLanguageChange" trigger="click">
    <el-button type="text" class="language-switcher">
      <el-icon class="language-icon">
        <SwitchFilled />
      </el-icon>
      <span>{{ currentLocaleName }}</span>
      <el-icon class="el-icon--right"><ArrowDown /></el-icon>
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
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { SwitchFilled, ArrowDown } from '@element-plus/icons-vue'
import { supportedLocales, setLocale, getCurrentLocale } from '@/locales'

const { t } = useI18n()

// 当前语言
const currentLocale = computed(() => getCurrentLocale())

// 当前语言显示名称
const currentLocaleName = computed(() => {
  const locale = supportedLocales.find(l => l.code === currentLocale.value)
  return locale ? locale.name : currentLocale.value
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
    color: var(--color-primary);
    background-color: var(--hover);
  }
}

.language-icon {
  font-size: 16px;
  color: var(--color-primary);
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
    color: var(--color-primary);
    background-color: var(--hover);
  }
  
  &:hover {
    background-color: var(--hover);
  }
}
</style>
