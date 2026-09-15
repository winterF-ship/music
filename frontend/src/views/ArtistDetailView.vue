<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { VideoPlay, Back } from '@element-plus/icons-vue'
import { fetchSingerDetail } from '../api/catalog'
import { usePlayerStore } from '../stores/player'
import MediaCover from '../components/MediaCover.vue'
import SongListPanel from '../components/SongListPanel.vue'
const route = useRoute(); const player = usePlayerStore(); const detail = ref(); const loading = ref(false); const failed = ref(false)
async function load() { loading.value = true; failed.value = false; try { detail.value = await fetchSingerDetail(String(route.params.id)) } catch { failed.value = true } finally { loading.value = false } }
onMounted(load); watch(() => route.params.id, load)
</script>
<template><div class="detail-page" v-loading="loading"><RouterLink to="/artists" class="back-link"><el-icon><Back /></el-icon>返回歌手</RouterLink><div v-if="detail" class="detail-hero artist-detail"><MediaCover :src="detail.singer.avatarUrl" :alt="`${detail.singer.name}头像`" :label="detail.singer.name" round /><div><span class="eyebrow">ARTIST</span><h1>{{ detail.singer.name }}</h1><p>{{ detail.singer.introduction || '这位歌手还没有填写简介。' }}</p><el-button type="primary" size="large" round :disabled="!detail.songs.length" @click="player.playAll(detail.songs)"><el-icon><VideoPlay /></el-icon>播放热门歌曲</el-button></div></div><section v-if="detail" class="content-section"><div class="section-title"><div><span class="eyebrow">DISCOGRAPHY</span><h2>全部歌曲</h2></div></div><SongListPanel :songs="detail.songs" /></section><el-result v-else-if="failed" icon="error" title="歌手加载失败" sub-title="请检查歌手是否存在或后端是否运行"><template #extra><el-button @click="load">重新加载</el-button></template></el-result></div></template>
