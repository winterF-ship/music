<script setup>
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { fetchPlaylists } from '../api/catalog'
import PlaylistCard from '../components/PlaylistCard.vue'
const records = ref([]); const page = ref(1); const total = ref(0); const loading = ref(false); const failed = ref(false)
async function load() { loading.value = true; failed.value = false; try { const data = await fetchPlaylists(page.value, 12); records.value = data.records; total.value = data.total } catch { failed.value = true } finally { loading.value = false } }
onMounted(load)
</script>
<template><div class="catalog-page"><header class="page-heading"><span class="eyebrow">PLAYLIST LIBRARY</span><h1>歌单广场</h1><p>从不同情绪与场景出发，找到下一段播放旅程。</p></header><div v-if="failed" class="api-error"><strong>歌单加载失败</strong><el-button :icon="Refresh" round @click="load">重试</el-button></div><div v-loading="loading" class="media-grid catalog-grid"><PlaylistCard v-for="item in records" :key="item.id" :item="item" /></div><el-empty v-if="!loading && !failed && !records.length" description="暂无歌单，可在后台创建" /><el-pagination v-if="total > 12" v-model:current-page="page" background layout="prev, pager, next" :page-size="12" :total="total" @current-change="load" /></div></template>
