<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeftBold, ArrowRightBold, Delete, FullScreen, Microphone, Mute, Tickets } from '@element-plus/icons-vue'
import { Heart, Repeat2, Shuffle } from '@lucide/vue'
import { createPlaybackReporter } from '../utils/playbackReporter'
import { createListenReporter } from '../utils/listenReporter'
import { assetUrl } from '../api/catalog'
import { useAuthStore } from '../stores/auth'
import { useFavoriteStore } from '../stores/favorites'
import { usePlayerStore } from '../stores/player'
import MediaCover from './MediaCover.vue'
import PlaybackIcon from './PlaybackIcon.vue'
import FullscreenPlayer from './FullscreenPlayer.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const favorites = useFavoriteStore()
const player = usePlayerStore()
const favoriteActive = computed(() => Boolean(player.currentSong) && favorites.songIds.has(player.currentSong.id))
const favoriteBusy = computed(() => Boolean(player.currentSong) && favorites.isBusy('song', player.currentSong.id))
const favoriteLabel = computed(() => player.currentSong ? `${favoriteActive.value ? '取消收藏' : '收藏'}歌曲“${player.currentSong.title}”` : '收藏歌曲')
async function toggleFavorite() {
  if (!player.currentSong) return
  if (!auth.isAuthenticated) {
    await router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const next = await favorites.toggleSong(player.currentSong)
    ElMessage.success(next ? '已加入我的收藏' : '已取消收藏')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '收藏操作失败')
  }
}
const playbackReporter = createPlaybackReporter()
// 播放次数（song_play_event）与收听时长（user_listen_record）是两套指标：前者匿名计数，后者只统计登录用户。
const listenReporter = createListenReporter()
let lastReportAttempt = 0
let lastListenTick = 0
function reportPlaying() { lastReportAttempt = Date.now(); void playbackReporter.playing() }
// 只统计登录用户的收听时长，避免把访客时长记到账号上。
function startListenSession() {
  listenReporter.start(auth.isAuthenticated ? player.currentSong?.id : null)
  lastListenTick = Date.now()
}
// 按墙钟时间累积真实播放秒数；暂停、拖动进度和长时间挂起的时间跳变不计入。
function trackListenTime() {
  const now = Date.now()
  const elapsed = (now - lastListenTick) / 1000
  lastListenTick = now
  if (audio.paused || audio.seeking || !player.currentSong) return
  if (elapsed > 0 && elapsed < 2) listenReporter.addSeconds(elapsed)
}
const audio = new Audio()
const audioError = ref(false)
const queueOpen = ref(false)
const volumeOpen = ref(false)
const fullscreenOpen = ref(false)
const previousVolume = ref(player.volume || 0.75)
const progress = computed({ get: () => player.currentTime, set: (value) => { audio.currentTime = value; player.currentTime = value } })
function syncTime() { player.currentTime = audio.currentTime || 0; trackListenTime(); if (!audio.paused && !audio.seeking && audio.currentTime > 0 && Date.now() - lastReportAttempt > 10000) reportPlaying() }
function syncDuration() { player.duration = Number.isFinite(audio.duration) ? audio.duration : 0 }
function fail() { audioError.value = true; player.isPlaying = false }
async function attemptPlay() {
  try { await audio.play() }
  catch (error) {
    if (error?.name === 'NotAllowedError') player.isPlaying = false
    else fail()
  }
}
audio.addEventListener('playing', reportPlaying)
audio.addEventListener('timeupdate', syncTime)
audio.addEventListener('loadedmetadata', syncDuration)
audio.addEventListener('ended', player.handleEnded)
audio.addEventListener('error', fail)

watch(() => [player.currentSong?.id, player.playbackVersion], () => {
  playbackReporter.start(player.currentSong?.id)
  startListenSession()
  audioError.value = false
  const source = assetUrl(player.currentSong?.audioUrl)
  if (!source) {
    audio.pause()
    audio.removeAttribute('src')
    player.isPlaying = false
    return
  }
  audio.src = source
  audio.load()
  if (player.isPlaying && audio.src) void attemptPlay()
}, { immediate: true })
watch(() => player.isPlaying, (playing) => {
  if (audio.src) playing ? void attemptPlay() : audio.pause()
  if (!playing) void listenReporter.flush()
})
// 播放中登录或退出登录时重建会话，登录前的访客时长不并入账号。
watch(() => auth.isAuthenticated, () => startListenSession())
watch(() => player.volume, (value) => { audio.volume = value }, { immediate: true })
watch(() => player.seekVersion, () => {
  if (!audio.src) return
  audio.currentTime = player.currentTime
})
onBeforeUnmount(() => { audio.pause(); void listenReporter.flush(); audio.removeEventListener('playing', reportPlaying); audio.removeEventListener('timeupdate', syncTime); audio.removeEventListener('loadedmetadata', syncDuration); audio.removeEventListener('ended', player.handleEnded); audio.removeEventListener('error', fail) })
function formatTime(value) { if (!Number.isFinite(value)) return '0:00'; return `${Math.floor(value / 60)}:${String(Math.floor(value % 60)).padStart(2, '0')}` }
function toggleMute() {
  if (player.volume > 0) { previousVolume.value = player.volume; player.setVolume(0) }
  else player.setVolume(previousVolume.value || 0.75)
}
</script>

<template>
  <aside class="player-dock" :class="{ 'is-playing': player.isPlaying && player.currentSong && !audioError }" aria-label="音乐播放器">
    <div class="player-track"><MediaCover :src="player.currentSong?.coverUrl" :alt="player.currentSong ? `${player.currentSong.title}封面` : ''" :label="player.currentSong?.title || '回'" /><div class="player-copy"><strong>{{ player.currentSong?.title || '挑一首喜欢的歌' }}</strong><small>{{ audioError ? '音频加载失败，请检查文件地址' : (player.currentSong?.singerName || '播放器已就绪') }}</small></div></div>
    <div class="player-controls">
      <button class="player-favorite-button" :class="{ active: favoriteActive }" type="button" :aria-label="favoriteLabel" :title="favoriteLabel" :aria-pressed="favoriteActive" :disabled="!player.currentSong || favoriteBusy" @click="toggleFavorite"><Heart aria-hidden="true" /></button>
      <button type="button" class="player-mode-button player-shuffle-button" :class="{ active: player.shuffleEnabled }" :aria-label="player.shuffleEnabled ? '关闭随机播放' : '开启随机播放'" :aria-pressed="player.shuffleEnabled" :disabled="player.queue.length < 2" @click="player.toggleShuffle"><Shuffle aria-hidden="true" /></button>
      <button type="button" class="player-skip-button" aria-label="上一首" :disabled="!player.currentSong" @click="player.previous"><el-icon><ArrowLeftBold /></el-icon></button>
      <button type="button" class="player-primary" :aria-label="player.isPlaying ? '暂停' : '播放'" :disabled="!player.currentSong" @click="player.toggle"><PlaybackIcon :playing="player.isPlaying" /></button>
      <button type="button" class="player-skip-button" aria-label="下一首" :disabled="!player.currentSong" @click="player.next"><el-icon><ArrowRightBold /></el-icon></button>
      <button type="button" class="player-mode-button" :class="{ active: player.repeatEnabled }" :aria-label="player.repeatEnabled ? '关闭循环播放' : '开启循环播放'" :aria-pressed="player.repeatEnabled" :disabled="!player.currentSong" @click="player.toggleRepeat"><Repeat2 aria-hidden="true" /></button>
    </div>
    <div class="player-right">
      <div class="player-progress-line"><span>{{ formatTime(player.currentTime) }}</span><el-slider v-model="progress" :min="0" :max="player.duration || 1" :show-tooltip="false" :disabled="!player.currentSong" aria-label="播放进度" /><span>{{ formatTime(player.duration) }}</span></div>
      <div class="player-tools">
        <el-popover v-model:visible="volumeOpen" placement="top" trigger="manual" :width="190">
          <template #reference><button class="player-tool-button" type="button" :aria-label="player.volume === 0 ? '打开音量控制，当前静音' : `打开音量控制，当前${Math.round(player.volume * 100)}%`" @click="volumeOpen = !volumeOpen"><el-icon><Mute v-if="player.volume === 0" /><Microphone v-else /></el-icon></button></template>
          <div class="volume-popover"><button type="button" :aria-label="player.volume === 0 ? '取消静音' : '静音'" @click="toggleMute"><el-icon><Mute v-if="player.volume === 0" /><Microphone v-else /></el-icon></button><el-slider v-model="player.volume" :min="0" :max="1" :step=".01" :show-tooltip="false" aria-label="音量" /><span>{{ Math.round(player.volume * 100) }}%</span></div>
        </el-popover>
        <button class="player-tool-button" type="button" aria-label="打开全屏播放器" :disabled="!player.currentSong" @click="fullscreenOpen = true"><el-icon><FullScreen /></el-icon></button>
        <button class="player-tool-button" type="button" aria-label="打开播放队列" @click="queueOpen = true"><el-icon><Tickets /></el-icon><span v-if="player.queue.length" class="queue-count">{{ player.queue.length }}</span></button>
      </div>
    </div>
  </aside>
  <el-drawer v-model="queueOpen" title="播放队列" size="360px" append-to-body class="queue-drawer">
    <template #header><div class="queue-heading"><div><strong>播放队列</strong><small>{{ player.queue.length }} 首歌曲</small></div><el-button v-if="player.queue.length" link @click="player.clearQueue">清空</el-button></div></template>
    <div v-if="player.queue.length" class="queue-list">
      <div v-for="(song, index) in player.queue" :key="`${song.id}-${index}`" class="queue-row" :class="{ active: index === player.currentIndex }">
        <button type="button" class="queue-song" :aria-label="`播放${song.title}`" @click="player.playAt(index)"><MediaCover :src="song.coverUrl" :alt="`${song.title}封面`" :label="song.title" /><span><strong>{{ song.title }}</strong><small>{{ song.singerName || '未知歌手' }}</small></span></button>
        <button type="button" class="queue-remove" :aria-label="`从队列移除${song.title}`" @click="player.removeFromQueue(index)"><el-icon><Delete /></el-icon></button>
      </div>
    </div>
    <el-empty v-else description="播放队列还是空的，去挑一首歌吧" />
  </el-drawer>
  <FullscreenPlayer v-model="fullscreenOpen" />
</template>
