<template>
  <div class="about-page">
    <div class="page-header">
      <h1>关于项目</h1>
      <p>Dify API 演示平台 - 基于 Vue 3 的知识库查询系统</p>
    </div>

    <div class="about-content">
      <div class="content-section">
        <h2>项目介绍</h2>
        <p>
          这是一个基于 Vue 3 + TypeScript + Axios 构建的 Dify API 演示平台。
          该平台提供了完整的知识库管理功能，包括数据集查看、智能检索、参数配置等核心功能。
        </p>
        <p>
          通过现代化的用户界面和强大的后端API集成，用户可以轻松地管理和查询知识库数据，
          体验先进的人工智能检索技术。
        </p>
      </div>

      <div class="content-section">
        <h2>技术栈</h2>
        <div class="tech-grid">
          <div class="tech-card">
            <div class="tech-icon">⚡</div>
            <h3>Vue 3</h3>
            <p>现代化的前端框架，提供响应式数据绑定和组合式API</p>
          </div>
          <div class="tech-card">
            <div class="tech-icon">🔷</div>
            <h3>TypeScript</h3>
            <p>类型安全的JavaScript超集，提供更好的开发体验</p>
          </div>
          <div class="tech-card">
            <div class="tech-icon">🌐</div>
            <h3>Axios</h3>
            <p>强大的HTTP客户端，支持请求拦截和响应处理</p>
          </div>
          <div class="tech-card">
            <div class="tech-icon">🎨</div>
            <h3>CSS3</h3>
            <p>现代化的样式设计，支持响应式布局和动画效果</p>
          </div>
          <div class="tech-card">
            <div class="tech-icon">🚀</div>
            <h3>Vite</h3>
            <p>快速的构建工具，提供热重载和优化的开发体验</p>
          </div>
          <div class="tech-card">
            <div class="tech-icon">🧭</div>
            <h3>Vue Router</h3>
            <p>官方路由管理器，支持单页应用导航</p>
          </div>
        </div>
      </div>

      <div class="content-section">
        <h2>功能特性</h2>
        <div class="features-list">
          <div class="feature-item">
            <div class="feature-icon">📊</div>
            <div class="feature-content">
              <h3>数据集管理</h3>
              <p>查看和管理所有可用的知识库数据集，支持分类筛选、搜索和详细信息展示</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🔍</div>
            <div class="feature-content">
              <h3>智能检索</h3>
              <p>支持语义检索、关键字检索、全文检索和混合检索等多种检索方式</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">⚙️</div>
            <div class="feature-content">
              <h3>参数配置</h3>
              <p>灵活配置检索参数，包括返回数量、相关性阈值、重排序等高级选项</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📱</div>
            <div class="feature-content">
              <h3>响应式设计</h3>
              <p>现代化的用户界面，完美适配桌面端和移动端设备</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🔒</div>
            <div class="feature-content">
              <h3>安全认证</h3>
              <p>使用Bearer Token进行API认证，确保数据安全</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📈</div>
            <div class="feature-content">
              <h3>实时统计</h3>
              <p>提供实时的数据集统计信息，包括文档数量、字数统计等</p>
            </div>
          </div>
        </div>
      </div>

      <div class="content-section">
        <h2>API 接口</h2>
        <div class="api-list">
          <div class="api-item">
            <div class="api-method get">GET</div>
            <div class="api-path">/v1/datasets</div>
            <div class="api-desc">获取数据集列表</div>
          </div>
          <div class="api-item">
            <div class="api-method get">GET</div>
            <div class="api-path">/v1/datasets/{id}</div>
            <div class="api-desc">获取特定数据集详情</div>
          </div>
          <div class="api-item">
            <div class="api-method post">POST</div>
            <div class="api-path">/v1/datasets/{id}/retrieve</div>
            <div class="api-desc">知识库检索查询</div>
          </div>
          <div class="api-item">
            <div class="api-method get">GET</div>
            <div class="api-path">/v1/datasets/{id}/documents</div>
            <div class="api-desc">获取数据集文档列表</div>
          </div>
          <div class="api-item">
            <div class="api-method get">GET</div>
            <div class="api-path">/v1/datasets/{id}/segments</div>
            <div class="api-desc">获取数据集段落列表</div>
          </div>
        </div>
      </div>

      <div class="content-section">
        <h2>开发信息</h2>
        <div class="dev-info">
          <div class="info-item">
            <strong>项目版本:</strong> 1.0.0
          </div>
          <div class="info-item">
            <strong>构建工具:</strong> Vite 7.1.14
          </div>
          <div class="info-item">
            <strong>Node.js:</strong> 推荐使用 18+ 版本
          </div>
          <div class="info-item">
            <strong>浏览器支持:</strong> Chrome 88+, Firefox 78+, Safari 14+
          </div>
          <div class="info-item">
            <strong>开发环境:</strong> 支持热重载和TypeScript检查
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 关于页面组件
</script>

<style scoped>
.about-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 1.1rem;
  color: #7f8c8d;
}

.content-section {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.content-section h2 {
  color: #2c3e50;
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 20px;
  border-bottom: 2px solid #667eea;
  padding-bottom: 10px;
}

.content-section p {
  color: #34495e;
  line-height: 1.6;
  margin-bottom: 15px;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.tech-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  transition: transform 0.2s;
}

.tech-card:hover {
  transform: translateY(-2px);
}

.tech-icon {
  font-size: 2.5rem;
  margin-bottom: 15px;
}

.tech-card h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 1.2rem;
}

.tech-card p {
  color: #7f8c8d;
  font-size: 14px;
  line-height: 1.5;
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.feature-item:hover {
  background: #e9ecef;
}

.feature-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.feature-content h3 {
  color: #2c3e50;
  margin-bottom: 8px;
  font-size: 1.1rem;
}

.feature-content p {
  color: #7f8c8d;
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
}

.api-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.api-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #667eea;
}

.api-method {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  min-width: 50px;
  text-align: center;
}

.api-method.get {
  background: #e8f5e8;
  color: #27ae60;
}

.api-method.post {
  background: #fff3cd;
  color: #856404;
}

.api-path {
  font-family: 'Courier New', monospace;
  background: #2c3e50;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}

.api-desc {
  color: #7f8c8d;
  font-size: 14px;
}

.dev-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 15px;
}

.info-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 14px;
}

.info-item strong {
  color: #2c3e50;
  margin-right: 8px;
}

@media (max-width: 768px) {
  .about-page {
    padding: 16px;
  }
  
  .content-section {
    padding: 20px;
  }
  
  .tech-grid {
    grid-template-columns: 1fr;
  }
  
  .feature-item {
    flex-direction: column;
    text-align: center;
  }
  
  .api-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .dev-info {
    grid-template-columns: 1fr;
  }
}
</style>
