<template>
  <div class="home">
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">Dify API 演示平台</h1>
        <p class="hero-subtitle">使用 Vue 3 + TypeScript + Axios 构建的知识库查询系统</p>
        <div class="hero-actions">
          <router-link to="/demo" class="btn-primary">开始演示</router-link>
          <router-link to="/datasets" class="btn-secondary">数据集管理</router-link>
        </div>
      </div>
    </div>

    <div class="features-section">
      <div class="container">
        <h2 class="section-title">功能特性</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">📊</div>
            <h3>数据集管理</h3>
            <p>查看和管理所有可用的知识库数据集，支持分类筛选和详细信息展示。</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🔍</div>
            <h3>智能检索</h3>
            <p>支持语义检索、关键字检索、全文检索和混合检索等多种检索方式。</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">⚙️</div>
            <h3>参数配置</h3>
            <p>灵活配置检索参数，包括返回数量、相关性阈值、重排序等高级选项。</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📱</div>
            <h3>响应式设计</h3>
            <p>现代化的用户界面，完美适配桌面端和移动端设备。</p>
          </div>
        </div>
      </div>
    </div>

    <div class="stats-section">
      <div class="container">
        <h2 class="section-title">平台统计</h2>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-number">{{ totalDatasets }}</div>
            <div class="stat-label">数据集</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ totalDocuments }}</div>
            <div class="stat-label">文档</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ totalQueries }}</div>
            <div class="stat-label">查询次数</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ successRate }}%</div>
            <div class="stat-label">成功率</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { difyApiService } from '../services/difyApi'

// 统计数据
const totalDatasets = ref(0)
const totalDocuments = ref(0)
const totalQueries = ref(0)
const successRate = ref(95)

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await difyApiService.getDatasets()
    totalDatasets.value = response.data.length
    
    // 计算总文档数
    let docs = 0
    for (const dataset of response.data) {
      docs += dataset.document_count
    }
    totalDocuments.value = docs
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
}

.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 80px 0;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 1.25rem;
  margin-bottom: 40px;
  opacity: 0.9;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-primary, .btn-secondary {
  padding: 15px 30px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  font-size: 1.1rem;
  transition: all 0.3s ease;
  display: inline-block;
}

.btn-primary {
  background: white;
  color: #667eea;
  border: 2px solid white;
}

.btn-primary:hover {
  background: transparent;
  color: white;
  transform: translateY(-2px);
}

.btn-secondary {
  background: transparent;
  color: white;
  border: 2px solid white;
}

.btn-secondary:hover {
  background: white;
  color: #667eea;
  transform: translateY(-2px);
}

.features-section {
  padding: 80px 0;
  background: #f8f9fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 60px;
  color: #2c3e50;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
}

.feature-card {
  background: white;
  padding: 40px 30px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 20px;
}

.feature-card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 15px;
  color: #2c3e50;
}

.feature-card p {
  color: #7f8c8d;
  line-height: 1.6;
}

.stats-section {
  padding: 80px 0;
  background: white;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 3rem;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 1.1rem;
  color: #7f8c8d;
  font-weight: 500;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-subtitle {
    font-size: 1.1rem;
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .btn-primary, .btn-secondary {
    width: 200px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
