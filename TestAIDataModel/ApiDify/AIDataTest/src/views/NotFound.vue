<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1>页面未找到</h1>
      <p>抱歉，您访问的页面不存在或已被移除。</p>
      <div class="actions">
        <router-link to="/" class="btn-primary">返回首页</router-link>
        <router-link to="/demo" class="btn-secondary">查看演示</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 404页面组件
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
}

.not-found-content {
  max-width: 500px;
  padding: 40px 20px;
}

.error-code {
  font-size: 8rem;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 20px;
  opacity: 0.8;
}

.not-found h1 {
  font-size: 2.5rem;
  margin-bottom: 20px;
  font-weight: 600;
}

.not-found p {
  font-size: 1.1rem;
  margin-bottom: 40px;
  opacity: 0.9;
  line-height: 1.6;
}

.actions {
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

@media (max-width: 768px) {
  .error-code {
    font-size: 6rem;
  }
  
  .not-found h1 {
    font-size: 2rem;
  }
  
  .actions {
    flex-direction: column;
    align-items: center;
  }
  
  .btn-primary, .btn-secondary {
    width: 200px;
  }
}
</style>
