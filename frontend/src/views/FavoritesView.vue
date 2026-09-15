<script setup>
import { onMounted, ref } from 'vue'
import { useFavoriteStore } from '../stores/favorites'
import SongListPanel from '../components/SongListPanel.vue'
import PlaylistCard from '../components/PlaylistCard.vue'

const favorites = useFavoriteStore()
const activeTab = ref('songs')
onMounted(() => { void favorites.load(true) })
</script>

<template>
  <section class="account-page" v-loading="favorites.loading">
    <header class="page-heading"><span class="eyebrow">MY COLLECTION</span><h1>我的收藏</h1><p>把喜欢的歌曲和歌单留在这里，随时回来继续听。</p></header>
    <el-tabs v-model="activeTab" class="favorite-tabs">
      <el-tab-pane :label="`歌曲 ${favorites.songs.length}`" name="songs"><SongListPanel :songs="favorites.songs" empty-text="还没有收藏歌曲，去发现页挑一首吧" /></el-tab-pane>
      <el-tab-pane :label="`歌单 ${favorites.playlists.length}`" name="playlists"><div v-if="favorites.playlists.length" class="media-grid catalog-grid"><PlaylistCard v-for="item in favorites.playlists" :key="item.id" :item="item" /></div><el-empty v-else description="还没有收藏歌单，去歌单广场看看吧" /></el-tab-pane>
    </el-tabs>
  </section>
</template>
