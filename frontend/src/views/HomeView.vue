<script setup>
import { onMounted, ref } from 'vue'
import { ArrowRight, VideoPlay, Refresh } from '@element-plus/icons-vue'
import { fetchBanners, fetchPlaylists, fetchSongs, assetUrl } from '../api/catalog'
import { usePlayerStore } from '../stores/player'
import PlaylistCard from '../components/PlaylistCard.vue'
import SongListPanel from '../components/SongListPanel.vue'

const banners = ref([])
const playlists = ref([])
const latestSongs = ref([])
const loading = ref(true)
const failed = ref(false)
const player = usePlayerStore()

async function load() {
  loading.value = true; failed.value = false
  try {
    const [bannerData, playlistData, songData] = await Promise.all([fetchBanners(), fetchPlaylists(1, 8), fetchSongs(1, 6)])
    banners.value = bannerData
    playlists.value = playlistData.records
    latestSongs.value = songData.records
  } catch { failed.value = true } finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <div class="home-page" v-loading="loading">
    <section class="home-hero">
      <el-carousel v-if="banners.length" height="430px" indicator-position="outside" arrow="always" :autoplay="true" :pause-on-hover="false" :interval="3000">
        <el-carousel-item v-for="banner in banners" :key="banner.id"><div class="hero-slide" :style="banner.imageUrl ? { backgroundImage: `linear-gradient(90deg, rgba(8,11,24,.9), rgba(8,11,24,.2)), url(${assetUrl(banner.imageUrl)})` } : {}"><div><span class="eyebrow">FEATURED NOW</span><h1>{{ banner.title }}</h1><p>让声音接管此刻，用一段旋律打开今天。</p><el-button type="primary" size="large" round @click="$router.push(banner.targetUrl || '/playlists')">立即探索<el-icon><ArrowRight /></el-icon></el-button></div></div></el-carousel-item>
      </el-carousel>
      <div v-else class="hero-slide hero-default"><div><span class="eyebrow">ECHO MUSIC</span><h1>让此刻，<br /><em>有自己的声音。</em></h1><p>发现新的歌单与熟悉的歌手。内容会随后台上架实时更新。</p><el-button v-if="latestSongs.length" type="primary" size="large" round @click="player.playAll(latestSongs)"><el-icon><VideoPlay /></el-icon>播放最新歌曲</el-button><el-button v-else type="primary" size="large" round @click="$router.push('/playlists')">浏览歌单</el-button></div><div class="hero-orbit" aria-hidden="true"><span>♪</span></div></div>
    </section>

    <div v-if="failed" class="api-error"><div><strong>暂时无法连接音乐服务</strong><p>请确认后端已在 IDEA 中运行（默认端口 8081）。</p></div><el-button :icon="Refresh" round @click="load">重试</el-button></div>

    <section class="content-section"><div class="section-title"><div><span class="eyebrow">CURATED FOR YOU</span><h2>推荐歌单</h2></div><RouterLink to="/playlists">查看全部 <el-icon><ArrowRight /></el-icon></RouterLink></div><div v-if="playlists.length" class="media-grid"><PlaylistCard v-for="item in playlists" :key="item.id" :item="item" /></div><el-empty v-else-if="!loading && !failed" description="暂无推荐歌单，可在后台创建" :image-size="90" /></section>
    <section class="content-section"><div class="section-title"><div><span class="eyebrow">JUST ARRIVED</span><h2>最近上架</h2></div></div><SongListPanel :songs="latestSongs" empty-text="暂无歌曲，可在后台上传" /></section>
  </div>
</template>
