<script setup>
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { fetchSingers } from '../api/catalog'
import MediaCover from '../components/MediaCover.vue'
const records = ref([]); const page = ref(1); const total = ref(0); const loading = ref(false); const failed = ref(false)
async function load() { loading.value = true; failed.value = false; try { const data = await fetchSingers(page.value, 15); records.value = data.records; total.value = data.total } catch { failed.value = true } finally { loading.value = false } }
onMounted(load)
</script>
<template><div class="catalog-page"><header class="page-heading"><span class="eyebrow">ARTIST DIRECTORY</span><h1>歌手名录</h1><p>关注声音本身，也发现声音背后的故事。</p></header><div v-if="failed" class="api-error"><strong>歌手加载失败</strong><el-button :icon="Refresh" round @click="load">重试</el-button></div><div v-loading="loading" class="artist-grid"><RouterLink v-for="artist in records" :key="artist.id" :to="`/artists/${artist.id}`" class="artist-tile"><MediaCover :src="artist.avatarUrl" :alt="`${artist.name}头像`" :label="artist.name" round /><h3>{{ artist.name }}</h3><p>{{ artist.introduction || '音乐人' }}</p></RouterLink></div><el-empty v-if="!loading && !failed && !records.length" description="暂无歌手，可在后台添加" /><el-pagination v-if="total > 15" v-model:current-page="page" background layout="prev, pager, next" :page-size="15" :total="total" @current-change="load" /></div></template>
