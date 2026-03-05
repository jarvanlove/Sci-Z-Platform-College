<!--
/**
 * @description 实践页面 - AI 智能体工作台
 * 现代化设计，突出人工智能 Agent 主题
 */
-->
<template>
  <div class="practice-page">
    <!-- 顶部区域 -->
    <div class="practice-hero">
      <div class="hero-bg">
        <div class="hero-gradient" />
        <div class="hero-pattern" />
      </div>
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot" />
          {{ $t('practice.pageBadge') }}
        </div>
        <h1 class="hero-title">{{ $t('practice.pageTitle') }}</h1>
        <p class="hero-subtitle">{{ $t('practice.pageSubtitle') }}</p>
      </div>
    </div>

    <!-- 顶部分组卡片 + 智能体卡片：3 个标签 + 原来的卡片布局 -->
    <div class="practice-accordion-wrap">
      <!-- 顶部三大分组卡片，作为选项卡切换 -->
      <div class="group-card-grid">
        <div
          v-for="group in agentGroups"
          :key="group.name"
          class="group-card"
          :class="{ active: activeGroup === group.name }"
          @click="activeGroup = group.name"
        >
          <div class="group-card-inner">
            <div class="group-card-title">
              {{ $t(group.nameKey) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 下方：当前分组下的智能体卡片，沿用之前的显示方式 -->
      <div class="practice-grid">
        <div
          v-for="agent in currentAgents"
          :key="agent.key"
          class="agent-card"
          :class="`agent-${agent.key}`"
          @click="goTo(agent.path)"
        >
          <div class="card-glow" />
          <div class="card-inner">
            <div class="card-icon-wrap">
              <div class="icon-bg" />
              <component :is="agent.icon" class="card-icon" />
            </div>
            <div class="card-body">
              <h3 class="card-title">{{ agent.title }}</h3>
              <p class="card-desc">{{ agent.desc }}</p>
              <span class="card-action">
                <span class="action-text">{{ $t('practice.startUse') }}</span>
                <el-icon class="action-arrow"><ArrowRight /></el-icon>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ArrowRight,
  MagicStick,
  Notebook,
  Odometer,
  School,
  TrendCharts
} from '@element-plus/icons-vue'

const router = useRouter()
const { t } = useI18n()

// 当前激活的分组（选项卡），默认选中项目管理
const activeGroup = ref('project')

// 所有智能体配置，保持原来的卡片内容
const allAgents = computed(() => [
  {
    key: 'declaration',
    title: t('menu.declaration'),
    desc: t('practice.declarationDesc'),
    path: '/declaration/list',
    icon: MagicStick
  },
  {
    key: 'project',
    title: t('menu.project'),
    desc: t('practice.projectDesc'),
    path: '/project/list',
    icon: TrendCharts
  },
  {
    key: 'report',
    title: t('menu.report'),
    desc: t('practice.reportDesc'),
    path: '/report/list',
    icon: Notebook
  },
  {
    key: 'industry-education',
    title: t('menu.industryEducation'),
    desc: t('practice.industryEducationDesc'),
    path: '/practice/industry-education',
    icon: School
  },
  {
    key: 'dashboard',
    title: t('menu.dashboard'),
    desc: t('practice.dashboardDesc'),
    path: '/dashboard',
    icon: Odometer
  }
])

// 分组配置：与之前手风琴分组保持一致
const agentGroups = computed(() => {
  const agents = allAgents.value
  const byKey = (k) => agents.find((a) => a.key === k) || null
  return [
    {
      name: 'project',
      nameKey: 'practice.groupProject',
      agents: [byKey('declaration'), byKey('project'), byKey('report')].filter(Boolean)
    },
    {
      name: 'dashboard',
      nameKey: 'practice.groupDashboard',
      agents: [byKey('dashboard')].filter(Boolean)
    },
    {
      name: 'decision',
      nameKey: 'practice.groupDecision',
      agents: [byKey('industry-education')].filter(Boolean)
    }
  ]
})

// 当前激活分组下的智能体列表
const currentAgents = computed(() => {
  const group = agentGroups.value.find((g) => g.name === activeGroup.value)
  return group ? group.agents : []
})

const goTo = (path) => {
  router.push(path)
}
</script>

<style lang="scss" scoped>
.practice-page {
  min-height: 100%;
  padding: 0 0 48px;
  background: var(--bg);
}

// 顶部 Hero 区域
.practice-hero {
  position: relative;
  padding: 40px 48px 48px;
  margin-bottom: 32px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(30, 58, 138, 0.08) 0%,
    rgba(59, 130, 246, 0.06) 40%,
    rgba(139, 92, 246, 0.05) 100%
  );
}

.hero-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.4;
  background-image: radial-gradient(
    circle at 1px 1px,
    var(--color-primary) 1px,
    transparent 0
  );
  background-size: 24px 24px;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  margin-bottom: 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-primary);
  background: rgba(59, 130, 246, 0.12);
  border: 1px solid rgba(59, 130, 246, 0.25);
  border-radius: 999px;
}

.badge-dot {
  width: 6px;
  height: 6px;
  background: var(--color-primary);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.1); }
}

.hero-title {
  margin: 0 0 12px;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-1);
  letter-spacing: -0.02em;
}

.hero-subtitle {
  margin: 0;
  font-size: 15px;
  color: var(--text-2);
  line-height: 1.5;
}

// 手风琴容器
.practice-accordion-wrap {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 48px;
}

/* 顶部分组卡片布局 */
.group-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--gap-lg);
  margin-bottom: 24px;
}

.group-card {
  position: relative;
  background: var(--surface);
  border-radius: 16px;
  padding: 18px 20px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  justify-content: space-between;

  &:hover {
    box-shadow: var(--shadow-md);
    border-color: var(--color-primary);
    transform: translateY(-2px);
  }

  &.active {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.25), var(--shadow-md);
    background: linear-gradient(
      135deg,
      rgba(59, 130, 246, 0.06) 0%,
      rgba(139, 92, 246, 0.04) 100%
    );
  }
}

.group-card-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  gap: 12px;
}

.group-card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-1);
  text-align: center;
  width: 100%;
}

.practice-collapse {
  border: none;
  background: transparent;

  :deep(.el-collapse-item__header) {
    height: 52px;
    padding: 0 20px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-1);
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 12px;
    margin-bottom: 12px;
  }

  :deep(.el-collapse-item__wrap) {
    border: none;
    background: transparent;
    overflow: visible;
  }

  :deep(.el-collapse-item__content) {
    padding: 20px 0 20px 0;
    background: transparent;
    overflow: visible;
  }

  :deep(.el-collapse-item__header.is-active) {
    border-color: rgba(59, 130, 246, 0.4);
    background: rgba(59, 130, 246, 0.06);
  }
}

.collapse-title {
  letter-spacing: 0.02em;
}

// 卡片网格（手风琴内）：顶部留足间距，避免悬浮 translateY 时卡片顶部被手风琴标题遮挡
.practice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  padding-top: 4px;
  overflow: visible;
}

.agent-card {
  position: relative;
  cursor: pointer;
  border-radius: 16px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-4px);

    .card-glow {
      opacity: 1;
    }

    .card-action {
      opacity: 1;
      transform: translateX(0);
    }

    .card-icon {
      transform: scale(1.05);
    }
  }
}

.card-glow {
  position: absolute;
  inset: -1px;
  border-radius: 17px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.card-inner {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 28px;
  height: 100%;
  min-height: 180px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  transition: all 0.3s ease;
  overflow: hidden;

  .agent-card:hover & {
    border-color: rgba(59, 130, 246, 0.35);
    box-shadow: 0 12px 40px -12px rgba(30, 58, 138, 0.2);
  }
}

// 不同智能体的配色
.agent-declaration .card-glow {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.15), transparent);
}
.agent-declaration:hover .card-inner {
  border-color: rgba(245, 158, 11, 0.4);
  box-shadow: 0 12px 40px -12px rgba(245, 158, 11, 0.15);
}
.agent-declaration .icon-bg {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
}
.agent-declaration .card-icon {
  color: #b45309;
}

.agent-project .card-glow {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), transparent);
}
.agent-project:hover .card-inner {
  border-color: rgba(59, 130, 246, 0.4);
  box-shadow: 0 12px 40px -12px rgba(59, 130, 246, 0.2);
}
.agent-project .icon-bg {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
}
.agent-project .card-icon {
  color: #1d4ed8;
}

.agent-report .card-glow {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.15), transparent);
}
.agent-report:hover .card-inner {
  border-color: rgba(139, 92, 246, 0.4);
  box-shadow: 0 12px 40px -12px rgba(139, 92, 246, 0.2);
}
.agent-report .icon-bg {
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
}
.agent-report .card-icon {
  color: #6d28d9;
}

.agent-dashboard .card-glow {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.15), transparent);
}
.agent-dashboard:hover .card-inner {
  border-color: rgba(16, 185, 129, 0.4);
  box-shadow: 0 12px 40px -12px rgba(16, 185, 129, 0.2);
}
.agent-dashboard .icon-bg {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
}
.agent-dashboard .card-icon {
  color: #047857;
}

// 产教研智能体：玫红/rose 主题（与申报同款交互：常规主题 + 悬浮加深），与仪表板绿色区分
.agent-industry-education .card-glow {
  background: linear-gradient(135deg, rgba(190, 18, 60, 0.08), transparent);
}
.agent-industry-education .card-inner {
  border-color: rgba(190, 18, 60, 0.2);
}
.agent-industry-education:hover .card-inner {
  border-color: rgba(190, 18, 60, 0.4);
  box-shadow: 0 12px 40px -12px rgba(190, 18, 60, 0.2);
}
.agent-industry-education .icon-bg {
  background: linear-gradient(135deg, #fce7f3, #fbcfe8);
}
.agent-industry-education .card-icon {
  color: #9d174d;
}
// 常规态「开始使用」与其余四张卡片一致（灰底灰字），悬浮时再变为玫红
.agent-industry-education:hover .card-action {
  color: #be123c;
  background: #fce7f3;
  .action-text,
  .action-arrow {
    color: #be123c;
  }
}

.card-icon-wrap {
  position: relative;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-bg {
  position: absolute;
  inset: 0;
  border-radius: 14px;
}

.card-icon {
  position: relative;
  width: 28px;
  height: 28px;
  transition: transform 0.3s ease;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.card-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-1);
}

.card-desc {
  flex: 1;
  margin: 0 0 16px;
  font-size: 14px;
  line-height: 1.55;
  color: var(--text-2);
}

.card-action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.02em;
  color: var(--text-2);
  background: var(--bg-secondary, #f3f4f6);
  border-radius: 8px;
  transition: all 0.25s ease;

  .action-text {
    color: inherit;
  }

  .action-arrow {
    font-size: 14px;
    color: var(--text-2);
    transition: transform 0.25s ease;
  }

  .agent-card:hover & .action-arrow {
    transform: translateX(3px);
  }
}

// 各卡片悬停时使用对应主题色
.agent-declaration:hover .card-action {
  color: #b45309;
  background: rgba(245, 158, 11, 0.08);
  .action-text, .action-arrow { color: #b45309; }
}
.agent-project:hover .card-action {
  color: #1d4ed8;
  background: rgba(59, 130, 246, 0.08);
  .action-text, .action-arrow { color: #1d4ed8; }
}
.agent-report:hover .card-action {
  color: #6d28d9;
  background: rgba(139, 92, 246, 0.08);
  .action-text, .action-arrow { color: #6d28d9; }
}
.agent-dashboard:hover .card-action {
  color: #047857;
  background: rgba(16, 185, 129, 0.08);
  .action-text, .action-arrow { color: #047857; }
}

// 暗色主题
[data-theme='dark'] .practice-hero .hero-gradient,
.dark .practice-hero .hero-gradient {
  background: linear-gradient(
    135deg,
    rgba(59, 130, 246, 0.12) 0%,
    rgba(139, 92, 246, 0.08) 100%
  );
}

[data-theme='dark'] .hero-pattern,
.dark .hero-pattern {
  opacity: 0.2;
}

// 响应式
@media (max-width: 768px) {
  .practice-hero {
    padding: 28px 24px 36px;
  }

  .hero-title {
    font-size: 24px;
  }

  .practice-accordion-wrap {
    padding: 0 24px;
  }

  .practice-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .card-inner {
    min-height: 160px;
    padding: 24px;
  }
}
</style>
