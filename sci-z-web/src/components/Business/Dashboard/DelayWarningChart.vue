<template>
  <div class="delay-warning-chart-wrap">
    <div ref="chartRef" class="delay-warning-chart"></div>
    <div v-if="isEmpty && !loading" class="chart-empty-hint">
      {{ emptyText }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  /** 数据格式：{ riskLevels: [{ level, count, name }, ...], upcomingDeadlines: [{ dayRange, count }, ...] } */
  data: {
    type: Object,
    default: null
  },
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  loading: {
    type: Boolean,
    default: false
  },
  /** 项目数量标签（i18n） */
  projectCountLabel: {
    type: String,
    default: '项目数量'
  },
  /** 风险等级标签映射（i18n） */
  riskLevelLabels: {
    type: Object,
    default: () => ({
      1: '已延期',
      2: '7天内到期',
      3: '30天内到期',
      4: '正常'
    })
  }
})

const chartRef = ref(null)
let chartInstance = null

const isEmpty = computed(() => {
  if (!props.data || !props.data.riskLevels) return true
  return props.data.riskLevels.length === 0 ||
         props.data.riskLevels.every(item => !item.count || item.count === 0)
})

const getOption = () => {
  const hasData = !isEmpty.value

  // 风险等级颜色映射
  const riskColors = {
    1: '#ef4444', // 红色 - 已延期
    2: '#f59e0b', // 橙色 - 7天内到期
    3: '#3b82f6', // 蓝色 - 30天内到期
    4: '#22c55e'  // 绿色 - 正常
  }

  // 准备数据 - 使用传入的 i18n 标签
  const riskLevelLabels = props.riskLevelLabels
  const riskData = hasData
    ? props.data.riskLevels.map(item => ({
        name: riskLevelLabels[item.level] || item.name,
        value: item.count,
        itemStyle: { color: riskColors[item.level] || '#6b7280' }
      }))
    : [
        { name: riskLevelLabels[1], value: 0, itemStyle: { color: '#ef4444' } },
        { name: riskLevelLabels[2], value: 0, itemStyle: { color: '#f59e0b' } },
        { name: riskLevelLabels[3], value: 0, itemStyle: { color: '#3b82f6' } },
        { name: riskLevelLabels[4], value: 0, itemStyle: { color: '#22c55e' } }
      ]

  // 横向条形图配置
  return {
    grid: {
      left: '3%',
      right: '15%',
      top: '5%',
      bottom: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 12,
        color: '#4b5563'
      },
      splitLine: {
        lineStyle: {
          color: '#e5e7eb',
          type: 'dashed'
        }
      }
    },
    yAxis: {
      type: 'category',
      data: riskData.map(item => item.name),
      axisLabel: {
        fontSize: 13,
        fontWeight: 500,
        color: '#374151',
        margin: 12
      },
      axisTick: {
        show: false
      },
      axisLine: {
        lineStyle: {
          color: '#d1d5db'
        }
      }
    },
    series: [
      {
        type: 'bar',
        data: riskData,
        barWidth: '50%',
        label: {
          show: true,
          position: 'right',
          fontSize: 14,
          fontWeight: 600,
          color: '#374151',
          formatter: '{c}'
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        }
      }
    ],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        const param = params[0]
        const countLabel = props.projectCountLabel
        return `<div style="font-weight:600;margin-bottom:4px">${param.name}</div>
                <div style="display:flex;align-items:center;gap:6px">
                  <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${param.color}"></span>
                  <span>${countLabel}：${param.value}</span>
                </div>`
      }
    }
  }
}

const initChart = () => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(getOption())
}

const updateChart = () => {
  if (!chartInstance) return
  chartInstance.setOption(getOption(), { notMerge: true })
}

const onResize = () => {
  chartInstance?.resize()
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

watch(() => props.data, () => updateChart(), { deep: true })
</script>

<style lang="scss" scoped>
.delay-warning-chart-wrap {
  position: relative;
  width: 100%;
  height: 220px;
}

.delay-warning-chart {
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
