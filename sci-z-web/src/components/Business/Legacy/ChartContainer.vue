<!--
/**
 * @description 图表容器组件
 * 用于展示各种图表，支持多种图表类型
 * 包含标题、工具栏、图例、数据展示等功能
 */
-->
<template>
  <BaseCard class="chart-container" :class="[customClass]">
    <!-- 图表头部 -->
    <div v-if="showHeader" class="chart-header">
      <div class="header-left">
        <h3 v-if="title" class="chart-title">
          <el-icon v-if="icon" class="title-icon">
            <component :is="icon" />
          </el-icon>
          {{ title }}
        </h3>
        <p v-if="description" class="chart-description">{{ description }}</p>
      </div>
      <div class="header-right">
        <slot name="header-actions">
          <el-dropdown
            v-if="showPeriodSelector"
            trigger="click"
            @command="handlePeriodChange"
          >
            <BaseButton type="text" size="small">
              {{ currentPeriod }}
              <el-icon class="el-icon--right">
                <ArrowDown />
              </el-icon>
            </BaseButton>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="period in periods"
                  :key="period.value"
                  :command="period.value"
                >
                  {{ period.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <el-dropdown
            v-if="showChartTypeSelector"
            trigger="click"
            @command="handleChartTypeChange"
          >
            <BaseButton type="text" size="small">
              <el-icon><TrendCharts /></el-icon>
            </BaseButton>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="type in chartTypes"
                  :key="type.value"
                  :command="type.value"
                >
                  <el-icon>
                    <component :is="type.icon" />
                  </el-icon>
                  {{ type.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <BaseButton
            v-if="showRefresh"
            type="text"
            size="small"
            :loading="loading"
            @click="handleRefresh"
          >
            <el-icon><Refresh /></el-icon>
          </BaseButton>
          
          <BaseButton
            v-if="showFullscreen"
            type="text"
            size="small"
            @click="toggleFullscreen"
          >
            <el-icon>
              <component :is="isFullscreen ? 'FullScreen' : 'Aim'" />
            </el-icon>
          </BaseButton>
        </slot>
      </div>
    </div>
    
    <!-- 图表内容 -->
    <div class="chart-content" :class="{ 'is-fullscreen': isFullscreen }">
      <!-- 图表区域 -->
      <div ref="chartRef" class="chart-area" :style="chartStyle"></div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="chart-loading">
        <el-icon class="loading-icon">
          <Loading />
        </el-icon>
        <span class="loading-text">加载中...</span>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="!chartData || chartData.length === 0" class="chart-empty">
        <el-empty :description="emptyText">
          <BaseButton v-if="showRetry" @click="handleRefresh">
            重新加载
          </BaseButton>
        </el-empty>
      </div>
    </div>
    
    <!-- 图表底部 -->
    <div v-if="showFooter" class="chart-footer">
      <slot name="footer">
        <div class="footer-content">
          <div class="footer-left">
            <slot name="footer-left">
              <div v-if="showLegend" class="chart-legend">
                <div
                  v-for="item in legendItems"
                  :key="item.name"
                  class="legend-item"
                >
                  <div
                    class="legend-color"
                    :style="{ backgroundColor: item.color }"
                  ></div>
                  <span class="legend-text">{{ item.name }}</span>
                </div>
              </div>
            </slot>
          </div>
          <div class="footer-right">
            <slot name="footer-right">
              <div v-if="showDataTable" class="data-summary">
                <span class="summary-text">{{ dataSummary }}</span>
              </div>
            </slot>
          </div>
        </div>
      </slot>
    </div>
  </BaseCard>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { BaseCard, BaseButton } from '@/components/Common'
import {
  ArrowDown,
  TrendCharts,
  Refresh,
  FullScreen,
  Aim,
  Loading
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  // 图表标题
  title: {
    type: String,
    default: ''
  },
  // 图表描述
  description: {
    type: String,
    default: ''
  },
  // 图表图标
  icon: {
    type: String,
    default: ''
  },
  // 图表类型
  chartType: {
    type: String,
    default: 'line',
    validator: (value) => ['line', 'bar', 'pie', 'area', 'scatter', 'radar'].includes(value)
  },
  // 图表数据
  chartData: {
    type: Array,
    default: () => []
  },
  // 图表配置
  chartOptions: {
    type: Object,
    default: () => ({})
  },
  // 图表高度
  height: {
    type: [String, Number],
    default: 300
  },
  // 是否显示头部
  showHeader: {
    type: Boolean,
    default: true
  },
  // 是否显示底部
  showFooter: {
    type: Boolean,
    default: true
  },
  // 是否显示图例
  showLegend: {
    type: Boolean,
    default: true
  },
  // 是否显示数据表格
  showDataTable: {
    type: Boolean,
    default: false
  },
  // 是否显示周期选择器
  showPeriodSelector: {
    type: Boolean,
    default: false
  },
  // 周期选项
  periods: {
    type: Array,
    default: () => [
      { label: '今天', value: 'today' },
      { label: '本周', value: 'week' },
      { label: '本月', value: 'month' },
      { label: '本年', value: 'year' }
    ]
  },
  // 当前周期
  currentPeriod: {
    type: String,
    default: 'today'
  },
  // 是否显示图表类型选择器
  showChartTypeSelector: {
    type: Boolean,
    default: false
  },
  // 图表类型选项
  chartTypes: {
    type: Array,
    default: () => [
      { label: '折线图', value: 'line', icon: 'TrendCharts' },
      { label: '柱状图', value: 'bar', icon: 'Histogram' },
      { label: '饼图', value: 'pie', icon: 'PieChart' },
      { label: '面积图', value: 'area', icon: 'DataLine' }
    ]
  },
  // 是否显示刷新按钮
  showRefresh: {
    type: Boolean,
    default: true
  },
  // 是否显示全屏按钮
  showFullscreen: {
    type: Boolean,
    default: false
  },
  // 是否显示重试按钮
  showRetry: {
    type: Boolean,
    default: true
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 空状态文本
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits([
  'refresh',
  'period-change',
  'chart-type-change',
  'fullscreen-change',
  'chart-ready'
])

// 响应式数据
const chartRef = ref()
const isFullscreen = ref(false)
let chartInstance = null

// 计算属性
const chartStyle = computed(() => {
  const height = typeof props.height === 'number' ? `${props.height}px` : props.height
  return {
    height,
    width: '100%'
  }
})

const legendItems = computed(() => {
  if (!props.chartData || props.chartData.length === 0) return []
  
  // 根据图表类型提取图例数据
  if (props.chartType === 'pie') {
    return props.chartData.map(item => ({
      name: item.name,
      color: item.color || '#409EFF'
    }))
  } else {
    // 其他图表类型的图例处理
    return []
  }
})

const dataSummary = computed(() => {
  if (!props.chartData || props.chartData.length === 0) return '暂无数据'
  
  if (props.chartType === 'pie') {
    const total = props.chartData.reduce((sum, item) => sum + (item.value || 0), 0)
    return `总计: ${total}`
  } else {
    return `数据点: ${props.chartData.length}`
  }
})

// 初始化图表
const initChart = async () => {
  if (!chartRef.value) return
  
  try {
    // 这里应该根据实际的图表库来初始化
    // 例如使用 ECharts、Chart.js 等
    console.log('初始化图表:', {
      type: props.chartType,
      data: props.chartData,
      options: props.chartOptions
    })
    
    // 模拟图表初始化
    await new Promise(resolve => setTimeout(resolve, 100))
    
    emit('chart-ready', chartInstance)
  } catch (error) {
    console.error('图表初始化失败:', error)
  }
}

// 更新图表
const updateChart = () => {
  if (!chartInstance) return
  
  // 更新图表数据
  console.log('更新图表数据:', props.chartData)
}

// 处理刷新
const handleRefresh = () => {
  emit('refresh')
}

// 处理周期变化
const handlePeriodChange = (period) => {
  emit('period-change', period)
}

// 处理图表类型变化
const handleChartTypeChange = (type) => {
  emit('chart-type-change', type)
}

// 切换全屏
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  emit('fullscreen-change', isFullscreen.value)
  
  // 重新调整图表大小
  nextTick(() => {
    if (chartInstance && chartInstance.resize) {
      chartInstance.resize()
    }
  })
}

// 监听数据变化
watch(() => props.chartData, updateChart, { deep: true })
watch(() => props.chartType, initChart)

// 生命周期
onMounted(() => {
  initChart()
})

onUnmounted(() => {
  if (chartInstance && chartInstance.dispose) {
    chartInstance.dispose()
  }
})
</script>

<style lang="scss" scoped>
.chart-container {
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--gap-lg);
    
    .header-left {
      flex: 1;
      
      .chart-title {
        display: flex;
        align-items: center;
        gap: var(--gap-sm);
        margin: 0 0 var(--gap-xs) 0;
        font-size: var(--font-size-lg);
        font-weight: 600;
        color: var(--text);
        
        .title-icon {
          color: var(--color-primary);
        }
      }
      
      .chart-description {
        margin: 0;
        font-size: var(--font-size-sm);
        color: var(--text-2);
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: var(--gap-sm);
    }
  }
  
  .chart-content {
    position: relative;
    min-height: 200px;
    
    &.is-fullscreen {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 9999;
      background: var(--surface);
      padding: var(--gap-lg);
    }
    
    .chart-area {
      width: 100%;
      background: var(--surface);
    }
    
    .chart-loading {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.8);
      
      .loading-icon {
        font-size: 32px;
        color: var(--color-primary);
        margin-bottom: var(--gap-md);
        animation: spin 1s linear infinite;
      }
      
      .loading-text {
        font-size: var(--font-size-sm);
        color: var(--text-2);
      }
    }
    
    .chart-empty {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 200px;
    }
  }
  
  .chart-footer {
    margin-top: var(--gap-lg);
    padding-top: var(--gap-md);
    border-top: 1px solid var(--border);
    
    .footer-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .chart-legend {
        display: flex;
        gap: var(--gap-md);
        flex-wrap: wrap;
        
        .legend-item {
          display: flex;
          align-items: center;
          gap: var(--gap-xs);
          
          .legend-color {
            width: 12px;
            height: 12px;
            border-radius: 2px;
          }
          
          .legend-text {
            font-size: var(--font-size-sm);
            color: var(--text-2);
          }
        }
      }
      
      .data-summary {
        .summary-text {
          font-size: var(--font-size-sm);
          color: var(--text-2);
        }
      }
    }
  }
}

// 动画
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .chart-container {
    .chart-header {
      flex-direction: column;
      gap: var(--gap-sm);
      align-items: stretch;
      
      .header-right {
        justify-content: flex-end;
      }
    }
    
    .chart-footer {
      .footer-content {
        flex-direction: column;
        gap: var(--gap-md);
        align-items: stretch;
        
        .chart-legend {
          justify-content: center;
        }
      }
    }
  }
}
</style>
