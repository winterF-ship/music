<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchSongs } from '../api/catalog'
import SongListPanel from '../components/SongListPanel.vue'
const route = useRoute(); const songs = ref([]); const loading = ref(false); const failed = ref(false)
async function load() { const q = typeof route.query.q === 'string' ? route.query.q.trim() : ''; if (!q) { songs.value = []; return } loading.value = true; failed.value = false; try { songs.value = (await fetchSongs(1, 50, q)).records } catch { failed.value = true } finally { loading.value = false } }
watch(() => route.query.q, load, { immediate: true })
</script>
<template><div class="catalog-page" v-loading="loading"><header class="page-heading"><span class="eyebrow">SEARCH</span><h1>“{{ route.query.q }}” 的搜索结果</h1><p v-if="!loading && !failed">找到 {{ songs.length }} 首歌曲</p></header><el-alert v-if="failed" title="搜索失败，请确认后端已运行" type="error" show-icon :closable="false" /><SongListPanel v-else :songs="songs" empty-text="没有找到匹配的歌曲" /></div></template>
