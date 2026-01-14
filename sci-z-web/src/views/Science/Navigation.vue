<!--
/**
 * @description 科学导航页面
 * 科学领域导航和资源分类浏览
 */
-->
<template>
  <div class="science-navigation-container">
    <div class="navigation-header">
      <h1 class="navigation-title">{{ $t('menu.scienceNavigation') }}</h1>
      <p class="navigation-subtitle">{{ $t('scienceNavigation.subtitle') }}</p>
    </div>

    <div class="navigation-content">
      <div class="coming-soon">
        <div class="coming-soon-icon">🔬</div>
        <h2 class="coming-soon-title">{{ $t('scienceNavigation.comingSoon') }}</h2>
        <p class="coming-soon-desc">{{ $t('scienceNavigation.comingSoonDesc') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<style lang="scss" scoped>
.science-navigation-container {
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.navigation-header {
  margin-bottom: 40px;
}

.navigation-title {
  font-size: 32px;
  font-weight: 600;
  color: #1e3a8a;
  margin-bottom: 12px;
}

.navigation-subtitle {
  font-size: 16px;
  color: #6b7280;
}

.navigation-content {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.coming-soon {
  text-align: center;
  padding: 60px 40px;
}

.coming-soon-icon {
  font-size: 64px;
  margin-bottom: 24px;
}

.coming-soon-title {
  font-size: 24px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}

.coming-soon-desc {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.6;
}
</style>

