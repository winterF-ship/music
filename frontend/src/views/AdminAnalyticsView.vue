<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { fetchAnalyticsSummary, fetchSongsBySinger } from '../api/admin'

const loading = ref(true)
const summary = ref({ users: 0, singers: 0, songs: 0, playlists: 0 })
const distribution = ref([])
const chartRef = ref(null)
const pieChartRef = ref(null)
let barChart = null
let pieChart = null

const totalSongs = computed(() => distribution.value.reduce((total, item) => total + Number(item.value || 0), 0))
const topSinger = computed(() => distribution.value[0]?.label || '暂无')
const topSingerShare = computed(() => {
  if (!totalSongs.value || !distribution.value[0]) return 0
  return Math.round((Number(distribution.value[0].value || 0) / totalSongs.value) * 100)
})

const chartColors = ['#6d4fe0', '#8b72ea', '#a892f0', '#b8a8f4', '#6d9de0', '#72bfc1', '#e2a85c', '#df7f96']

function renderChart() {
  if (!distribution.value.length) {
    barChart?.dispose()
    pieChart?.dispose()
    barChart = null
    pieChart = null
    return
  }

  if (chartRef.value) {
    barChart ??= echarts.init(chartRef.value)
    barChart.setOption({
      color: ['#6d4fe0'],
      grid: { top: 24, right: 28, bottom: 48, left: 54 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: distribution.value.map((item) => item.label), axisLabel: { color: '#766f8a', interval: 0, rotate: distribution.value.length > 5 ? 25 : 0 } },
      yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#766f8a' }, splitLine: { lineStyle: { color: '#eeeaf5' } } },
      series: [{ name: '歌曲数', type: 'bar', barMaxWidth: 42, data: distribution.value.map((item) => item.value), itemStyle: { borderRadius: [8, 8, 0, 0] } }],
    })
  }

  if (pieChartRef.value) {
    pieChart ??= echarts.init(pieChartRef.value)
    const compact = pieChartRef.value.clientWidth < 520
    pieChart.setOption({
      color: chartColors,
      aria: { enabled: true },
      tooltip: { trigger: 'item', formatter: '{b}<br/>{c} 首歌曲（{d}%）' },
      legend: {
        type: 'scroll',
        orient: compact ? 'horizontal' : 'vertical',
        left: compact ? 'center' : '60%',
        right: compact ? 'auto' : 8,
        top: compact ? 'auto' : 'center',
        bottom: compact ? 2 : 'auto',
        itemWidth: 10,
        itemHeight: 10,
        itemGap: compact ? 8 : 13,
        textStyle: { color: '#766f8a', fontSize: 12 },
        formatter: (name) => {
          const item = distribution.value.find((entry) => entry.label === name)
          return `${name}  ${item?.value ?? 0}`
        },
      },
      title: [
        { text: `${totalSongs.value}`, left: compact ? '50%' : '30%', top: compact ? '31%' : '38%', textAlign: 'center', textStyle: { color: '#303133', fontSize: 26, fontWeight: 700 } },
        { text: '收录歌曲', left: compact ? '50%' : '30%', top: compact ? '43%' : '50%', textAlign: 'center', textStyle: { color: '#9a93a9', fontSize: 11, fontWeight: 400 } },
      ],
      series: [{ name: '歌手热度参考', type: 'pie', radius: compact ? ['40%', '63%'] : ['48%', '72%'], center: compact ? ['50%', '39%'] : ['30%', '50%'], avoidLabelOverlap: true, itemStyle: { borderColor: '#fff', borderWidth: 3, borderRadius: 6 }, label: { show: false }, emphasis: { label: { show: true, color: '#303133', fontSize: 13, fontWeight: 700, formatter: '{b}\n{d}%' }, scaleSize: 5 } }],
    })
  }
}

async function load() {
  loading.value = true
  try {
    const [summaryResult, distributionResult] = await Promise.all([fetchAnalyticsSummary(), fetchSongsBySinger()])
    if (summaryResult.code !== 200) throw new Error(summaryResult.message)
    if (distributionResult.code !== 200) throw new Error(distributionResult.message)
    summary.value = summaryResult.data
    distribution.value = distributionResult.data
    await nextTick()
    renderChart()
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '统计数据加载失败') } finally { loading.value = false }
}

function resizeChart() { barChart?.resize(); pieChart?.resize() }
onMounted(() => { window.addEventListener('resize', resizeChart); void load() })
onBeforeUnmount(() => { window.removeEventListener('resize', resizeChart); barChart?.dispose(); pieChart?.dispose(); barChart = null; pieChart = null })
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading"><div><p class="eyebrow">SIGNAL REPORT</p><h1>数据统计</h1><p>用一眼能读懂的指标了解当前音乐内容库的规模和结构。</p></div><el-button :loading="loading" @click="load">刷新数据</el-button></div>
    <div v-loading="loading" class="analytics-stats"><el-card shadow="never" class="analytics-stat"><span>注册用户</span><strong>{{ summary.users }}</strong><small>可登录的用户账号</small></el-card><el-card shadow="never" class="analytics-stat"><span>歌手</span><strong>{{ summary.singers }}</strong><small>内容库中的音乐人</small></el-card><el-card shadow="never" class="analytics-stat"><span>歌曲</span><strong>{{ summary.songs }}</strong><small>已登记的音频资源</small></el-card><el-card shadow="never" class="analytics-stat"><span>歌单</span><strong>{{ summary.playlists }}</strong><small>正在维护的歌单集合</small></el-card></div>
    <div v-loading="loading" element-loading-text="正在读取统计数据" class="analytics-charts">
      <el-card shadow="never" class="admin-card analytics-chart-card"><template #header><div class="analytics-card-heading"><div><strong>歌曲歌手分布</strong><p>按歌手统计已收录歌曲数量</p></div><el-tag type="info">{{ distribution.length }} 位歌手</el-tag></div></template><div v-if="distribution.length" ref="chartRef" class="analytics-chart" aria-label="歌曲歌手分布柱状图"></div><el-empty v-else-if="!loading" description="暂无歌曲数据，添加歌曲后这里会生成图表" /></el-card>
      <el-card shadow="never" class="admin-card analytics-chart-card analytics-pie-card"><template #header><div class="analytics-card-heading"><div><strong>歌曲播放情况</strong><p>以歌手歌曲占比作为受欢迎程度参考</p></div><el-tag type="success">{{ topSinger }} · {{ topSingerShare }}%</el-tag></div></template><div v-if="distribution.length" ref="pieChartRef" class="analytics-chart analytics-pie-chart" aria-label="歌曲播放情况歌手占比饼状图"></div><el-empty v-else-if="!loading" description="暂无歌曲数据，添加歌曲后这里会生成图表" /></el-card>
    </div>
  </section>
</template>
