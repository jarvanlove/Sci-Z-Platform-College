<!--
/**
 * @description 404错误页面
 * 页面不存在时的错误提示页面
 */
-->
<template>
  <div class="error-404-page">
    <h1>404 - 页面不存在</h1>
    <p>抱歉，您访问的页面不存在</p>
    <BaseButton type="primary" @click="goHome">返回首页</BaseButton>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { BaseButton } from '@/components/Common'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style lang="scss" scoped>
.error-404-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: var(--gap-lg);
  text-align: center;
  
  h1 {
    font-size: var(--font-size-3xl);
    color: var(--color-error);
    margin-bottom: var(--gap-md);
  }
  
  p {
    font-size: var(--font-size-lg);
    color: var(--text-2);
    margin-bottom: var(--gap-xl);
  }
}
</style>