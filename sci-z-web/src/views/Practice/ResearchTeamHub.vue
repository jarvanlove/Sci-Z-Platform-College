<template>
  <div class="hub-page">
    <div class="hub-hero">
      <h1 class="hub-title">{{ $t('practice.groupDecision') }}</h1>
      <p class="hub-subtitle">
        {{ $t('practice.industryEducationDesc') }}
      </p>
    </div>

    <div class="hub-grid">
      <div class="hub-card" @click="goTo('/practice/industry-education')">
        <h3 class="card-title">{{ $t('menu.industryEducation') }}</h3>
        <p class="card-desc">{{ $t('practice.industryEducationDesc') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goTo = (path) => {
  router.push(path)
}
</script>

<style lang="scss" scoped>
.hub-page {
  padding: 32px 48px 48px;
}

.hub-hero {
  margin-bottom: 24px;
}

.hub-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
}

.hub-subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--text-2);
}

.hub-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.hub-card {
  padding: 20px 22px;
  border-radius: 16px;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }
}

.card-title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
}

.card-desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-2);
}
</style>

