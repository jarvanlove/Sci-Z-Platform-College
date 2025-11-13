<template>
  <nav class="navigation">
    <div class="nav-container">
      <div class="nav-brand">
        <router-link to="/" class="brand-link">
          <span class="brand-icon">🧠</span>
          <span class="brand-text">Dify API 演示</span>
        </router-link>
      </div>
      
      <div class="nav-menu" :class="{ 'nav-menu-open': isMenuOpen }">
        <router-link 
          v-for="item in navItems" 
          :key="item.name"
          :to="item.path" 
          class="nav-link"
          @click="closeMenu"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-text">{{ item.text }}</span>
        </router-link>
      </div>
      
      <div class="nav-toggle" @click="toggleMenu">
        <span class="hamburger" :class="{ 'hamburger-open': isMenuOpen }">
          <span></span>
          <span></span>
          <span></span>
        </span>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 导航菜单状态
const isMenuOpen = ref(false)

// 导航菜单项
const navItems = [
  {
    name: 'Home',
    path: '/',
    text: '首页',
    icon: '🏠'
  },
  {
    name: 'Demo',
    path: '/demo',
    text: '演示',
    icon: '🎮'
  },
  {
    name: 'Datasets',
    path: '/datasets',
    text: '数据集',
    icon: '📊'
  },
  {
    name: 'Query',
    path: '/query',
    text: '查询',
    icon: '🔍'
  },
  {
    name: 'DocumentUpload',
    path: '/upload',
    text: '上传',
    icon: '📤'
  },
  {
    name: 'CreateDataset',
    path: '/create-dataset',
    text: '创建知识库',
    icon: '➕'
  },
  {
    name: 'About',
    path: '/about',
    text: '关于',
    icon: 'ℹ️'
  }
]

// 切换菜单
const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

// 关闭菜单
const closeMenu = () => {
  isMenuOpen.value = false
}
</script>

<style scoped>
.navigation {
  background: white;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.nav-brand {
  flex-shrink: 0;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #2c3e50;
  font-weight: 700;
  font-size: 1.2rem;
  transition: color 0.2s;
}

.brand-link:hover {
  color: #667eea;
}

.brand-icon {
  font-size: 1.5rem;
}

.brand-text {
  white-space: nowrap;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 0;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  text-decoration: none;
  color: #7f8c8d;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.2s;
  white-space: nowrap;
}

.nav-link:hover {
  color: #667eea;
  background: #f8f9fa;
}

.nav-link.router-link-active {
  color: #667eea;
  background: #e8f0fe;
  font-weight: 600;
}

.nav-icon {
  font-size: 1.1rem;
}

.nav-text {
  font-size: 14px;
}

.nav-toggle {
  display: none;
  cursor: pointer;
  padding: 8px;
}

.hamburger {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 24px;
  height: 18px;
  position: relative;
}

.hamburger span {
  width: 100%;
  height: 2px;
  background: #2c3e50;
  border-radius: 1px;
  transition: all 0.3s ease;
}

.hamburger-open span:nth-child(1) {
  transform: rotate(45deg) translate(5px, 5px);
}

.hamburger-open span:nth-child(2) {
  opacity: 0;
}

.hamburger-open span:nth-child(3) {
  transform: rotate(-45deg) translate(7px, -6px);
}

@media (max-width: 768px) {
  .nav-container {
    padding: 0 16px;
  }
  
  .nav-toggle {
    display: block;
  }
  
  .nav-menu {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: white;
    box-shadow: 0 4px 20px rgba(0,0,0,0.1);
    flex-direction: column;
    align-items: stretch;
    padding: 20px;
    transform: translateY(-100%);
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
  }
  
  .nav-menu-open {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }
  
  .nav-link {
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 8px;
    justify-content: flex-start;
  }
  
  .nav-link:last-child {
    margin-bottom: 0;
  }
}

@media (max-width: 480px) {
  .brand-text {
    display: none;
  }
  
  .nav-text {
    font-size: 16px;
  }
}
</style>
