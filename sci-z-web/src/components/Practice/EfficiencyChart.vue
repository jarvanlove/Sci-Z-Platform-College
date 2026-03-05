<!--
  产教研 - 效率图表（柱状/折线）
  无数据时展示占位图表，便于汇报展示
-->
<template>
  <div class="efficiency-chart-wrap">
    <div ref="chartRef" class="efficiency-chart"></div>
    <div v-if="isEmpty && !loading" class="chart-empty-hint">{{ emptyText }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  /** 图表数据：
   * 单条线：{ x: string[], y: number[] }
   * 多条线：{ x: string[], series: [{ name: string, data: number[], color?: string }, ...] }
   * 或 null 使用占位
   */
  data: { type: Object, default: null },
  /** 无数据时占位文案 */
  emptyText: { type: String, default: '暂无效率数据' },
  /** 加载中 */
  loading: { type: Boolean, default: false },
  /** 图表类型：bar=项目维度柱状图，line=综合维度折线图，便于领导多维度对比 */
  chartType: { type: String, default: 'bar' },
  /** X轴名称（i18n） */
  xAxisName: { type: String, default: '月份' },
  /** Y轴名称（i18n） */
  yAxisName: { type: String, default: '数量' },
  /** Tooltip 数量标签（i18n） */
  tooltipQuantityLabel: { type: String, default: '数量' }
})

const chartRef = ref(null)
let chartInstance = null

const isEmpty = computed(() => {
  // 兼容旧格式 { x, y } 和新格式 { x, series: [{ name, data }, ...] }
  if (!props.data || !props.data.x) return true
  if (props.data.x.length === 0) return true

  // 新格式：检查 series 数据
  if (props.data.series && Array.isArray(props.data.series)) {
    return props.data.series.every(s => !s.data || s.data.length === 0 || s.data.every(v => v == null || v === 0))
  }

  // 旧格式：检查 y 数据
  if (props.data.y) {
    return props.data.y.every((v) => v == null || v === 0)
  }

  return true
})

function getOption() {
  const hasData = !isEmpty.value && props.data && props.data.x && props.data.x.length
  const x = hasData ? props.data.x : ['10', '11', '12', '01', '02', '03']
  const isLine = props.chartType === 'line'

  // 使用传入的 i18n 坐标轴名称
  const xAxisName = props.xAxisName
  const yAxisName = props.yAxisName

  // 默认颜色
  const defaultColors = ['#1e3a8a', '#16a34a']

  // 判断数据格式：单条线（y数组）还是多条线（series数组）
  const hasSeries = hasData && props.data.series && Array.isArray(props.data.series)

  // 构建 series 数据
  let seriesData = []
  if (isLine) {
    if (hasSeries) {
      // 多条线模式
      seriesData = props.data.series.map((s, index) => ({
        type: 'line',
        name: s.name,
        data: s.data || [],
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: s.color || defaultColors[index % defaultColors.length], width: 2 },
        itemStyle: { color: s.color || defaultColors[index % defaultColors.length] }
      }))
    } else {
      // 单条线模式（兼容旧数据）
      const y = hasData ? (props.data.y || []) : [0, 0, 0, 0, 0, 0]
      seriesData = [{
        type: 'line',
        name: '申报/项目数量',
        data: y,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: defaultColors[0], width: 2 },
        itemStyle: { color: defaultColors[0] },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(30, 58, 138, 0.2)' }, { offset: 1, color: 'rgba(30, 58, 138, 0)' }]) }
      }]
    }
  } else {
    // 柱状图模式
    const y = hasData ? (props.data.y || []) : [0, 0, 0, 0]
    seriesData = [{
      type: 'bar',
      name: '数量',
      data: y,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#3b82f6' },
          { offset: 1, color: '#60a5fa' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '50%'
    }]
  }

  // 柱状图需要更大的底部边距来显示长标签
  const isBar = props.chartType === 'bar'
  const bottomMargin = isBar ? '28%' : '18%'

  return {
    grid: { left: '12%', right: '8%', top: '12%', bottom: bottomMargin },
    xAxis: {
      type: 'category',
      data: x,
      name: xAxisName,
      nameLocation: 'middle',
      nameGap: isBar ? 45 : 35,
      axisLabel: {
        fontSize: 12,
        fontWeight: 500,
        color: '#374151',
        interval: 0,
        margin: 14,
        // 柱状图标签旋转15度，避免文字重叠和截断
        rotate: isBar ? 15 : 0,
        // 不截断标签，完整显示
        formatter: function(value) {
          return value
        }
      },
      nameTextStyle: { fontSize: 13, fontWeight: 600, color: '#4b5563' }
    },
    yAxis: {
      type: 'value',
      name: yAxisName,
      nameLocation: 'middle',
      nameGap: 35,
      axisLabel: {
        fontSize: 13,
        fontWeight: 500,
        color: '#374151'
      },
      nameTextStyle: { fontSize: 13, fontWeight: 600, color: '#4b5563' },
      splitLine: { lineStyle: { opacity: 0.2 } }
    },
    legend: isLine && hasSeries ? {
      data: seriesData.map(s => s.name),
      top: 8,
      textStyle: { fontSize: 12, color: '#4b5563' }
    } : undefined,
    series: seriesData,
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const quantityLabel = props.tooltipQuantityLabel
        let html = `<div style="font-weight:600;margin-bottom:6px">${params[0].name}</div>`
        params.forEach(param => {
          const seriesName = param.seriesName || quantityLabel
          html += `<div style="display:flex;align-items:center;gap:6px;margin-bottom:3px">
            <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${param.color}"></span>
            <span>${seriesName}：${param.value}</span>
          </div>`
        })
        return html
      }
    }
  }
}

function initChart() {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(getOption())
}

function updateChart() {
  if (!chartInstance) return
  chartInstance.setOption(getOption(), { notMerge: true })
}

function onResize() {
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

watch(() => [props.data, props.chartType], () => updateChart(), { deep: true })
</script>

<style lang="scss" scoped>
.efficiency-chart-wrap {
  position: relative;
  width: 100%;
  height: 260px;
  background: linear-gradient(180deg, rgba(59, 130, 246, 0.04) 0%, transparent 100%);
  border-radius: 10px;
}

.efficiency-chart {
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
