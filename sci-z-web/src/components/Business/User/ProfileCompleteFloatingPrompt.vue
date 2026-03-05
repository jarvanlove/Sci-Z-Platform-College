<!--
/**
 * @description 完善个人信息悬浮飘窗
 * 当用户资料未完善时在 ToC 布局主内容区显示，点击跳转到 /user/profile；
 * 仅在用户完成并保存个人信息后消失。
 */
-->
<template>
  <Transition name="profile-prompt-fade">
    <div
      v-if="visible"
      class="profile-complete-prompt"
      role="button"
      tabindex="0"
      @click="goToProfile"
      @keydown.enter="goToProfile"
      @keydown.space.prevent="goToProfile"
    >
      <div class="prompt-icon">
        <el-icon :size="24"><User /></el-icon>
      </div>
      <div class="prompt-body">
        <p class="prompt-text">{{ t('user.profile.completeProfilePrompt') }}</p>
        <span class="prompt-action">{{ t('user.profile.goToComplete') }}</span>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { User } from '@element-plus/icons-vue'

defineOptions({
  name: 'ProfileCompleteFloatingPrompt'
})

const props = defineProps({
  /** 是否显示飘窗 */
  visible: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const { t } = useI18n()

const goToProfile = () => {
  router.push('/user/profile').catch(() => {})
}
</script>

<style lang="scss" scoped>
.profile-complete-prompt {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  max-width: 260px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(30, 58, 138, 0.12);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(30, 58, 138, 0.18);
  }

  &:focus-visible {
    outline: 2px solid var(--color-primary);
    outline-offset: 2px;
  }
}

.prompt-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, rgba(30, 58, 138, 0.12) 0%, rgba(59, 130, 246, 0.12) 100%);
  border-radius: 10px;
  color: var(--color-primary);
}

.prompt-body {
  flex: 1;
  min-width: 0;
}

.prompt-text {
  margin: 0 0 4px;
  font-size: 14px;
  line-height: 1.4;
  color: var(--text-2);
}

.prompt-action {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary);
}

.profile-prompt-fade-enter-active,
.profile-prompt-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.profile-prompt-fade-enter-from,
.profile-prompt-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
