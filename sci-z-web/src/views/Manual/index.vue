<template>
  <div class="manual-container">
    <div class="manual-header">
      <div class="header-content">
        <h1 class="header-title">{{ $t('manual.title') }}</h1>
      </div>
    </div>
    
    <div class="manual-content">
      <!-- 系统介绍 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><InfoFilled /></el-icon>
            <span class="card-title">{{ $t('manual.systemIntroduction.title') }}</span>
          </div>
        </template>
        <div class="card-content">
          <p class="intro-text">{{ $t('manual.systemIntroduction.content') }}</p>
          <div class="feature-list">
            <div class="feature-item" v-for="(feature, index) in features" :key="index">
              <el-icon class="feature-icon"><Check /></el-icon>
              <span>{{ feature }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 快速开始 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Promotion /></el-icon>
            <span class="card-title">{{ $t('manual.quickStart.title') }}</span>
          </div>
        </template>
        <div class="card-content">
          <div class="step-list">
            <div class="step-item" v-for="(step, index) in quickStartSteps" :key="index">
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-content">
                <h3 class="step-title">{{ step.title }}</h3>
                <p class="step-description">{{ step.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 功能模块 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Menu /></el-icon>
            <span class="card-title">{{ $t('manual.modules.title') }}</span>
          </div>
        </template>
        <div class="card-content">
          <div class="module-grid">
            <div class="module-item" v-for="(module, index) in modules" :key="index">
              <el-icon class="module-icon"><component :is="module.icon" /></el-icon>
              <h3 class="module-title">{{ module.title }}</h3>
              <p class="module-description">{{ module.description }}</p>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 使用指南 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Reading /></el-icon>
            <span class="card-title">{{ $t('manual.guide.title') }}</span>
          </div>
        </template>
        <div class="card-content">
          <el-collapse v-model="activeGuide" accordion>
            <el-collapse-item 
              v-for="(guide, index) in guides" 
              :key="index"
              :name="index"
              :title="guide.title"
            >
              <div class="guide-content">
                <p v-for="(paragraph, pIndex) in guide.content" :key="pIndex" class="guide-paragraph">
                  {{ paragraph }}
                </p>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-card>

      <!-- 常见问题 -->
      <el-card class="section-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><QuestionFilled /></el-icon>
            <span class="card-title">{{ $t('manual.faq.title') }}</span>
          </div>
        </template>
        <div class="card-content">
          <el-collapse v-model="activeFaq" accordion>
            <el-collapse-item 
              v-for="(faq, index) in faqs" 
              :key="index"
              :name="index"
              :title="faq.question"
            >
              <div class="faq-answer">
                <p>{{ faq.answer }}</p>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/store/modules/auth'
import { 
  Document, 
  InfoFilled, 
  Check, 
  Promotion, 
  Menu, 
  Reading, 
  QuestionFilled,
  House,
  Search,
  ChatDotRound,
  FolderOpened,
  Document as DocumentIcon,
  User,
  Setting,
  Checked
} from '@element-plus/icons-vue'

const { t } = useI18n()
const authStore = useAuthStore()

// 折叠面板激活项
const activeGuide = ref(0)
const activeFaq = ref(0)

// 系统特性
const features = computed(() => [
  t('manual.systemIntroduction.feature1'),
  t('manual.systemIntroduction.feature2'),
  t('manual.systemIntroduction.feature3'),
  t('manual.systemIntroduction.feature4'),
  t('manual.systemIntroduction.feature5')
])

// 快速开始步骤
const quickStartSteps = computed(() => [
  {
    title: t('manual.quickStart.step1.title'),
    description: t('manual.quickStart.step1.description')
  },
  {
    title: t('manual.quickStart.step2.title'),
    description: t('manual.quickStart.step2.description')
  },
  {
    title: t('manual.quickStart.step3.title'),
    description: t('manual.quickStart.step3.description')
  },
  {
    title: t('manual.quickStart.step4.title'),
    description: t('manual.quickStart.step4.description')
  },
  {
    title: t('manual.quickStart.step5.title'),
    description: t('manual.quickStart.step5.description')
  },
  {
    title: t('manual.quickStart.step6.title'),
    description: t('manual.quickStart.step6.description')
  },
  {
    title: t('manual.quickStart.step7.title'),
    description: t('manual.quickStart.step7.description')
  }
])

// 功能模块
const modules = computed(() => {
  const allModules = [
    {
      icon: House,
      title: t('manual.modules.dashboard.title'),
      description: t('manual.modules.dashboard.description')
    },
    {
      icon: Search,
      title: t('manual.modules.literature.title'),
      description: t('manual.modules.literature.description')
    },
    {
      icon: ChatDotRound,
      title: t('manual.modules.ai.title'),
      description: t('manual.modules.ai.description')
    },
    {
      icon: DocumentIcon,
      title: t('manual.modules.declaration.title'),
      description: t('manual.modules.declaration.description')
    },
    {
      icon: FolderOpened,
      title: t('manual.modules.project.title'),
      description: t('manual.modules.project.description')
    },
    {
      icon: Checked,
      title: t('manual.modules.acceptance.title'),
      description: t('manual.modules.acceptance.description')
    },
    {
      icon: Setting,
      title: t('manual.modules.system.title'),
      description: t('manual.modules.system.description'),
      requiresSystemMenu: true // 标记需要系统管理菜单权限
    },
    {
      icon: User,
      title: t('manual.modules.user.title'),
      description: t('manual.modules.user.description')
    }
  ]
  
  // 过滤模块：如果模块需要系统管理菜单权限，则检查用户是否有该权限
  return allModules.filter(module => {
    if (module.requiresSystemMenu) {
      // 方法1: 检查是否有系统管理菜单路径（递归检查）
      const hasSystemMenuPath = checkMenuPathRecursive(authStore.menus, '/system')
      
      // 方法2: 检查是否有系统管理相关权限
      const hasSystemPermission = authStore.hasPermission('menu:system') || 
        authStore.permissions.some(perm => perm.startsWith('menu:system:'))
      
      const shouldShow = hasSystemMenuPath || hasSystemPermission
      
      // 开发环境调试日志
      if (import.meta.env.DEV && !shouldShow) {
        console.log('[Manual] 系统管理模块权限检查:', {
          hasSystemMenuPath,
          hasSystemPermission,
          menus: authStore.menus,
          permissions: authStore.permissions,
          hasMenuPermission: authStore.hasMenuPermission('/system')
        })
      }
      
      return shouldShow
    }
    return true
  })
})

// 递归检查菜单路径是否存在（包括子菜单）
function checkMenuPathRecursive(menus, targetPath) {
  if (!menus || !Array.isArray(menus)) return false
  
  for (const menu of menus) {
    // 检查当前菜单路径是否匹配
    if (menu.path === targetPath) {
      return true
    }
    
    // 检查是否有子菜单，递归查找
    if (menu.children && menu.children.length > 0) {
      if (checkMenuPathRecursive(menu.children, targetPath)) {
        return true
      }
    }
  }
  
  return false
}

// 使用指南
const guides = computed(() => [
  {
    title: t('manual.guide.item1.title'),
    content: [
      t('manual.guide.item1.content1'),
      t('manual.guide.item1.content2'),
      t('manual.guide.item1.content3')
    ]
  },
  {
    title: t('manual.guide.item2.title'),
    content: [
      t('manual.guide.item2.content1'),
      t('manual.guide.item2.content2'),
      t('manual.guide.item2.content3')
    ]
  },
  {
    title: t('manual.guide.item3.title'),
    content: [
      t('manual.guide.item3.content1'),
      t('manual.guide.item3.content2'),
      t('manual.guide.item3.content3')
    ]
  }
])

// 常见问题
const faqs = computed(() => [
  {
    question: t('manual.faq.item1.question'),
    answer: t('manual.faq.item1.answer')
  },
  {
    question: t('manual.faq.item2.question'),
    answer: t('manual.faq.item2.answer')
  },
  {
    question: t('manual.faq.item3.question'),
    answer: t('manual.faq.item3.answer')
  },
  {
    question: t('manual.faq.item4.question'),
    answer: t('manual.faq.item4.answer')
  }
])
</script>

<style lang="scss" scoped>
.manual-container {
  min-height: calc(100vh - 60px);
  background: var(--bg-color, #f5f7fa);
  padding: 20px 8px;
}

.manual-header {
  margin-bottom: 20px;
  
  .header-content {
    display: flex;
    align-items: center;
    
    .header-title {
      font-size: 24px;
      font-weight: 600;
      color: var(--color-primary);
      margin: 0;
    }
  }
}

.manual-content {
  max-width: 1600px;
  margin: 0 auto;
}

.section-card {
  margin-bottom: 20px;
  border-radius: 8px;
  
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid var(--border-color, #ebeef5);
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon {
      font-size: 18px;
      color: var(--color-primary, #409eff);
    }
    
    .card-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--color-primary);
    }
  }
  
  .card-content {
    color: var(--text-3, #6b7280);
    line-height: 1.8;
  }
}

.intro-text {
  font-size: 14px;
  margin-bottom: 20px;
  color: var(--text-3, #6b7280);
  font-weight: 400;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .feature-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    color: var(--text-3, #6b7280);
    font-weight: 400;
    
    .feature-icon {
      color: var(--color-success, #67c23a);
      font-size: 18px;
    }
  }
}

.step-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  
  .step-item {
    display: flex;
    gap: 16px;
    
    .step-number {
      flex-shrink: 0;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: var(--color-primary, #409eff);
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      font-weight: 600;
    }
    
    .step-content {
      flex: 1;
      
      .step-title {
        font-size: 14px;
        font-weight: 600;
        color: var(--text-2, #4b5563);
        margin: 0 0 8px 0;
      }
      
      .step-description {
        font-size: 14px;
        color: var(--text-3, #6b7280);
        font-weight: 400;
        margin: 0;
        line-height: 1.8;
      }
    }
  }
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  
  .module-item {
    padding: 20px;
    border: 1px solid var(--border-color, #ebeef5);
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: var(--color-primary, #409eff);
      box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
      transform: translateY(-2px);
    }
    
    .module-icon {
      font-size: 32px;
      color: var(--color-primary, #409eff);
      margin-bottom: 12px;
    }
    
    .module-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-2, #4b5563);
      margin: 0 0 8px 0;
    }
    
    .module-description {
      font-size: 14px;
      color: var(--text-3, #6b7280);
      font-weight: 400;
      margin: 0;
      line-height: 1.6;
    }
  }
}

.guide-content {
  .guide-paragraph {
    margin: 0 0 12px 0;
    font-size: 14px;
    line-height: 1.8;
    color: var(--text-3, #6b7280);
    font-weight: 400;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
}

.faq-answer {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-3, #6b7280);
  font-weight: 400;
  
  p {
    margin: 0;
  }
}

:deep(.el-collapse) {
  border: none;
  
  .el-collapse-item {
    border-bottom: 1px solid var(--border-color, #ebeef5);
    
    &:last-child {
      border-bottom: none;
    }
    
    .el-collapse-item__header {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-2, #4b5563);
      padding: 16px 0;
      
      &:hover {
        color: var(--color-primary, #409eff);
      }
    }
    
    .el-collapse-item__content {
      padding-bottom: 16px;
    }
  }
}
</style>

