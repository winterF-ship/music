<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ArrowDown, ArrowLeftBold, ArrowRightBold, Loading } from '@element-plus/icons-vue'
import { assetUrl } from '../api/catalog'
import { usePlayerStore } from '../stores/player'
import { activeLyricIndex, parseLrc } from '../utils/lyrics'
import MediaCover from './MediaCover.vue'
import PlaybackIcon from './PlaybackIcon.vue'

const open = defineModel({ type: Boolean, default: false })
const player = usePlayerStore()
const lyrics = ref([])
const lyricState = ref('idle')
const lyricItems = ref([])
const lyricScroller = ref()
let lyricRequest

const activeIndex = computed(() => activeLyricIndex(lyrics.value, player.currentTime))
const progress = computed({ get: () => player.currentTime, set: (value) => player.seek(value) })
const coverBackground = computed(() => {
  const url = assetUrl(player.currentSong?.coverUrl)
  return url ? { backgroundImage: `url("${url.replaceAll('"', '\\"')}")` } : {}
})

watch(() => player.currentSong?.lyricUrl, async (lyricUrl) => {
  lyricRequest?.abort()
  lyrics.value = []
  lyricState.value = lyricUrl ? 'loading' : 'empty'
  if (!lyricUrl) return
  lyricRequest = new AbortController()
  try {
    const response = await fetch(assetUrl(lyricUrl), { signal: lyricRequest.signal })
    if (!response.ok) throw new Error('歌词加载失败')
    lyrics.value = parseLrc(await response.text())
    lyricState.value = lyrics.value.length ? 'ready' : 'empty'
  } catch (error) {
    if (error?.name !== 'AbortError') lyricState.value = 'error'
  }
}, { immediate: true })

watch(activeIndex, async (index) => {
  if (index < 0 || !open.value) return
  await nextTick()
  const reduceMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
  scrollToLyric(index, reduceMotion ? 'auto' : 'smooth')
})

watch(open, async (visible) => {
  if (!visible || activeIndex.value < 0) return
  await nextTick()
  scrollToLyric(activeIndex.value, 'auto')
})

onBeforeUnmount(() => lyricRequest?.abort())

function formatTime(value) {
  if (!Number.isFinite(value)) return '0:00'
  return `${Math.floor(value / 60)}:${String(Math.floor(value % 60)).padStart(2, '0')}`
}

function scrollToLyric(index, behavior) {
  const scroller = lyricScroller.value
  const item = lyricItems.value[index]
  if (!scroller || !item) return
  const top = item.offsetTop - scroller.clientHeight / 2 + item.offsetHeight / 2
  scroller.scrollTo({ top: Math.max(0, top), behavior })
}
</script>

<template>
  <el-dialog v-model="open" fullscreen :show-close="false" append-to-body class="fullscreen-player-dialog" aria-label="全屏音乐播放器">
    <div class="fullscreen-player">
      <div class="fullscreen-player-backdrop" :style="coverBackground" aria-hidden="true" />
      <header class="fullscreen-player-header">
        <button type="button" class="fullscreen-round-button" aria-label="收起全屏播放器" @click="open = false"><el-icon><ArrowDown /></el-icon></button>
        <div><small>正在播放</small><strong>{{ player.currentSong?.title || '暂未选择歌曲' }}</strong></div>
        <span class="fullscreen-player-mark" aria-hidden="true">ECHO</span>
      </header>

      <div v-if="player.currentSong" class="fullscreen-player-stage">
        <section class="fullscreen-album" aria-label="当前歌曲">
          <div class="fullscreen-record" :class="{ spinning: player.isPlaying }">
            <MediaCover :src="player.currentSong.coverUrl" :alt="`${player.currentSong.title}封面`" :label="player.currentSong.title" />
          </div>
          <div class="turntable-arm" :class="{ lowered: player.isPlaying }" aria-hidden="true">
            <span class="turntable-arm-base" />
            <span class="turntable-arm-bar"><span class="turntable-arm-head" /></span>
          </div>
          <div class="fullscreen-song-copy"><h2>{{ player.currentSong.title }}</h2><p>{{ player.currentSong.singerName || '未知歌手' }}</p></div>
        </section>

        <section ref="lyricScroller" class="fullscreen-lyrics" aria-label="实时歌词，可滚动浏览并点击跳转" aria-live="polite" tabindex="0">
          <div v-if="lyricState === 'loading'" class="lyrics-state"><el-icon class="is-loading"><Loading /></el-icon><span>正在载入歌词</span></div>
          <div v-else-if="lyricState === 'error'" class="lyrics-state"><strong>歌词暂时无法加载</strong><span>音乐仍可继续播放</span></div>
          <div v-else-if="lyricState === 'empty'" class="lyrics-state"><strong>这首歌还没有实时歌词</strong><span>可在歌曲管理中上传 LRC 文件</span></div>
          <div v-else class="lyrics-track">
            <button v-for="(line, index) in lyrics" :key="`${line.time}-${index}`" :ref="(element) => { lyricItems[index] = element }" type="button" class="lyric-line" :class="{ active: index === activeIndex, passed: index < activeIndex }" :aria-current="index === activeIndex ? 'true' : undefined" @click="player.seek(line.time)">{{ line.text }}</button>
          </div>
        </section>
      </div>
      <div v-else class="fullscreen-empty"><strong>先挑一首歌</strong><span>播放后就能在这里查看实时歌词</span></div>

      <footer class="fullscreen-player-controls">
        <div class="fullscreen-progress"><span>{{ formatTime(player.currentTime) }}</span><el-slider v-model="progress" :min="0" :max="player.duration || 1" :show-tooltip="false" :disabled="!player.currentSong" aria-label="播放进度" /><span>{{ formatTime(player.duration) }}</span></div>
        <div class="fullscreen-actions">
          <button type="button" aria-label="上一首" :disabled="!player.currentSong" @click="player.previous"><el-icon><ArrowLeftBold /></el-icon></button>
          <button type="button" class="fullscreen-play-button" :aria-label="player.isPlaying ? '暂停' : '播放'" :disabled="!player.currentSong" @click="player.toggle"><PlaybackIcon :playing="player.isPlaying" /></button>
          <button type="button" aria-label="下一首" :disabled="!player.currentSong" @click="player.next"><el-icon><ArrowRightBold /></el-icon></button>
        </div>
      </footer>
    </div>
  </el-dialog>
</template>
