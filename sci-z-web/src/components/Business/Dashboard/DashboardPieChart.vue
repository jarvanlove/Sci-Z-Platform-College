<template>
  <div class="dashboard-pie-chart-wrap">
    <div ref="chartRef" class="dashboard-pie-chart"></div>
    <div v-if="isEmpty && !loading" class="chart-empty-hint">
      {{ emptyText }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  /** 数据格式：[{ name: '已提交', value: 10 }, ...] */
  data: {
    type: Array,
    default: () => []
  },
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const chartRef = ref(null)
let chartInstance = null

const isEmpty = computed(() => {
  if (!props.data || props.data.length === 0) return true
  // 如果所有值都是0，也视为空数据（不显示饼图）
  return props.data.every(item => !item.value || item.value === 0)
})

const getOption = () => {
  const hasData = !isEmpty.value
  const seriesData = hasData
    ? props.data
    : [
        { name: '示例一', value: 40 },
        { name: '示例二', value: 30 },
        { name: '示例三', value: 30 }
      ]

  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 0,
      top: 'center',
      textStyle: {
        fontSize: 12,
        color: '#4b5563'
      }
    },
    series: [
      {
        name: '',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        data: seriesData
      }
    ]
  }
}

const initChart = () => {
  if (!chartRef.value) return
  // 避免重复绑定同一个 DOM 导致 echarts 内部 assert 报错
  if (chartInstance) {
    try {
      chartInstance.dispose()
    } catch (e) {
      // ignore
    }
    chartInstance = null
  }
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(getOption())
}

const updateChart = () => {
  if (!chartInstance || !chartRef.value) return
  const { offsetWidth, offsetHeight } = chartRef.value
  if (!offsetWidth || !offsetHeight) return
  try {
    chartInstance.setOption(getOption(), { notMerge: true })
  } catch (e) {
    // 避免因异常数据导致页面直接崩溃
    console.error('[DashboardPieChart] update error', e)
  }
}

const onResize = () => {
  if (!chartInstance || !chartRef.value) return
  const { offsetWidth, offsetHeight } = chartRef.value
  if (!offsetWidth || !offsetHeight) return
  try {
    chartInstance.resize()
  } catch (e) {
    console.error('[DashboardPieChart] resize error', e)
  }
}

onMounted(() => {
  nextTick(() => {
    initChart()
    chartInstance?.resize()
  })
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chartInstance?.dispose()
  chartInstance = null
})

watch(
  () => props.data,
  () => updateChart(),
  { deep: true }
)
</script>

<style lang="scss" scoped>
.dashboard-pie-chart-wrap {
  position: relative;
  width: 100%;
  height: 260px;
}

.dashboard-pie-chart {
  width: 100%;
  height: 100%;
}

.chart-empty-hint {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 12px;
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>

