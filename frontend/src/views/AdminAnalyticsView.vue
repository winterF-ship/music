<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { fetchAnalyticsSummary, fetchSongsBySinger } from '../api/admin'

const loading = ref(true)
const summary = ref({ users: 0, singers: 0, songs: 0, playlists: 0 })
const distribution = ref([])
const chartRef = ref()
let chart = null

function renderChart() {
  if (!chartRef.value) return
  chart ??= echarts.init(chartRef.value)
  chart.setOption({
    color: ['#6d4fe0'],
    grid: { top: 24, right: 28, bottom: 48, left: 54 },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'category', data: distribution.value.map((item) => item.label), axisLabel: { color: '#766f8a', interval: 0, rotate: distribution.value.length > 5 ? 25 : 0 } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#766f8a' }, splitLine: { lineStyle: { color: '#eeeaf5' } } },
    series: [{ name: '歌曲数', type: 'bar', barMaxWidth: 42, data: distribution.value.map((item) => item.value), itemStyle: { borderRadius: [8, 8, 0, 0] } }],
  })
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

function resizeChart() { chart?.resize() }
onMounted(() => { window.addEventListener('resize', resizeChart); void load() })
onBeforeUnmount(() => { window.removeEventListener('resize', resizeChart); chart?.dispose(); chart = null })
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading"><div><p class="eyebrow">SIGNAL REPORT</p><h1>数据统计</h1><p>用一眼能读懂的指标了解当前音乐内容库的规模和结构。</p></div><el-button :loading="loading" @click="load">刷新数据</el-button></div>
    <div v-loading="loading" class="analytics-stats"><el-card shadow="never" class="analytics-stat"><span>注册用户</span><strong>{{ summary.users }}</strong><small>可登录的用户账号</small></el-card><el-card shadow="never" class="analytics-stat"><span>歌手</span><strong>{{ summary.singers }}</strong><small>内容库中的音乐人</small></el-card><el-card shadow="never" class="analytics-stat"><span>歌曲</span><strong>{{ summary.songs }}</strong><small>已登记的音频资源</small></el-card><el-card shadow="never" class="analytics-stat"><span>歌单</span><strong>{{ summary.playlists }}</strong><small>正在维护的歌单集合</small></el-card></div>
    <el-card shadow="never" class="admin-card analytics-chart-card"><template #header><div class="analytics-card-heading"><div><strong>歌曲歌手分布</strong><p>按歌手统计已收录歌曲数量</p></div><el-tag type="info">{{ distribution.length }} 位歌手</el-tag></div></template><div ref="chartRef" class="analytics-chart" aria-label="歌曲歌手分布柱状图"></div><el-empty v-if="!loading && distribution.length === 0" description="暂无歌曲数据，添加歌曲后这里会生成图表" /></el-card>
  </section>
</template>
