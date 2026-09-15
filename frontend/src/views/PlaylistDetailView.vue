<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Back, Plus, VideoPlay } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { fetchPlaylistDetail, fetchSongs } from '../api/catalog'
import { useAuthStore } from '../stores/auth'
import { usePlayerStore } from '../stores/player'
import { useUserPlaylistStore } from '../stores/playlists'
import MediaCover from '../components/MediaCover.vue'
import SongListPanel from '../components/SongListPanel.vue'
import FavoriteButton from '../components/FavoriteButton.vue'

const route = useRoute()
const auth = useAuthStore()
const player = usePlayerStore()
const userPlaylists = useUserPlaylistStore()
const detail = ref()
const loading = ref(false)
const failed = ref(false)
const addDialogOpen = ref(false)
const songsLoading = ref(false)
const songsFailed = ref(false)
const savingSongs = ref(false)
const availableSongs = ref([])
const selectedSongIds = ref([])
const ownsPlaylist = computed(() => Boolean(auth.isAuthenticated && detail.value?.playlist && userPlaylists.items.some((item) => item.id === detail.value.playlist.id)))
const existingSongIds = computed(() => new Set((detail.value?.songs || []).map((song) => song.id)))
const selectableSongs = computed(() => availableSongs.value.filter((song) => !existingSongIds.value.has(song.id)))

async function load() {
  loading.value = true
  failed.value = false
  try {
    detail.value = await fetchPlaylistDetail(String(route.params.id))
    if (auth.isAuthenticated) { try { await userPlaylists.load() } catch { /* detail remains usable if private state refresh fails */ } }
  } catch { failed.value = true } finally { loading.value = false }
}

async function loadSongOptions() {
  songsLoading.value = true
  songsFailed.value = false
  try {
    const result = await fetchSongs(1, 100)
    availableSongs.value = result.records || []
    selectedSongIds.value = []
  } catch { songsFailed.value = true } finally { songsLoading.value = false }
}

async function openAddDialog() {
  if (!ownsPlaylist.value) return
  addDialogOpen.value = true
  await loadSongOptions()
}

async function saveSongs() {
  if (!detail.value || !selectedSongIds.value.length || savingSongs.value) return
  savingSongs.value = true
  let added = 0
  const remaining = [...selectedSongIds.value]
  try {
    for (const songId of [...selectedSongIds.value]) {
      const next = await userPlaylists.addSong(detail.value.playlist.id, songId)
      if (!next) continue
      detail.value = next
      remaining.splice(remaining.indexOf(songId), 1)
      added += 1
    }
    selectedSongIds.value = remaining
    if (!remaining.length) {
      addDialogOpen.value = false
      ElMessage.success(`已添加 ${added} 首歌曲`)
    }
  } catch (error) {
    selectedSongIds.value = remaining
    ElMessage.error(added ? `已添加 ${added} 首歌曲，剩余歌曲添加失败，请重试` : (error.response?.data?.message || error.message || '添加歌曲失败，请稍后重试'))
  } finally { savingSongs.value = false }
}

onMounted(load)
watch(() => route.params.id, load)
</script>

<template>
  <div class="detail-page" v-loading="loading">
    <RouterLink to="/playlists" class="back-link"><el-icon><Back /></el-icon>返回歌单</RouterLink>
    <div v-if="detail" class="detail-hero">
      <MediaCover :src="detail.playlist.coverUrl" :alt="`${detail.playlist.name}封面`" :label="detail.playlist.name" />
      <div><span class="eyebrow">PLAYLIST</span><h1>{{ detail.playlist.name }}</h1><p>{{ detail.playlist.description || '这个歌单还没有简介。' }}</p><div class="detail-actions"><el-button type="primary" size="large" round :disabled="!detail.songs.length" @click="player.playAll(detail.songs)"><el-icon><VideoPlay /></el-icon>播放全部</el-button><el-button v-if="ownsPlaylist" size="large" round :icon="Plus" @click="openAddDialog">添加歌曲</el-button><FavoriteButton kind="playlist" :item="detail.playlist" label-visible /></div></div>
    </div>
    <section v-if="detail" class="content-section"><div class="section-title"><div><span class="eyebrow">TRACKS</span><h2>{{ detail.songs.length }} 首歌曲</h2></div></div><SongListPanel :songs="detail.songs" /></section>
    <el-result v-else-if="failed" icon="error" title="歌单加载失败" sub-title="请检查歌单是否存在或后端是否运行"><template #extra><el-button @click="load">重新加载</el-button></template></el-result>
    <el-dialog v-model="addDialogOpen" class="playlist-song-dialog" title="添加歌曲" width="520px">
      <p class="playlist-dialog-copy">从已有歌曲中选择，添加到“{{ detail?.playlist?.name }}”。</p>
      <div v-loading="songsLoading" class="playlist-song-picker">
        <el-alert v-if="songsFailed" title="歌曲加载失败，请重试" type="error" show-icon :closable="false"><template #default><el-button link type="danger" @click="loadSongOptions">重新加载</el-button></template></el-alert>
        <template v-else-if="selectableSongs.length">
          <label id="playlist-song-picker-label" class="playlist-picker-label">选择歌曲</label>
          <el-select v-model="selectedSongIds" aria-labelledby="playlist-song-picker-label" multiple filterable clearable collapse-tags collapse-tags-tooltip placeholder="搜索并选择歌曲" style="width:100%">
            <el-option v-for="song in selectableSongs" :key="song.id" :label="`${song.title} · ${song.singerName || '未知歌手'}`" :value="song.id" />
          </el-select>
          <small class="upload-hint">当前展示最近 100 首歌曲，已在歌单中的歌曲不会重复显示。</small>
        </template>
        <el-empty v-else-if="!songsLoading" description="没有可添加的歌曲" />
      </div>
      <template #footer><el-button @click="addDialogOpen = false">取消</el-button><el-button v-if="selectableSongs.length" type="primary" :disabled="!selectedSongIds.length" :loading="savingSongs" @click="saveSongs">添加{{ selectedSongIds.length ? ` ${selectedSongIds.length} 首` : '' }}歌曲</el-button></template>
    </el-dialog>
  </div>
</template>
