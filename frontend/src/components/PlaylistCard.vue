<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, Loading } from '@element-plus/icons-vue'
import MediaCover from './MediaCover.vue'
import PlaybackIcon from './PlaybackIcon.vue'
import { fetchPlaylistDetail } from '../api/catalog'
import { usePlayerStore } from '../stores/player'
import { playlistCoverGradient } from '../utils/playlistCover'
import { beginPlaylistPlaybackRequest, isLatestPlaylistPlaybackRequest, shufflePlaylistSongs } from '../utils/playlistPlayback'

const props = defineProps({
  item: { type: Object, required: true },
  editable: { type: Boolean, default: false },
})
const emit = defineEmits(['edit'])
const player = usePlayerStore()
const loading = ref(false)
const coverGradient = computed(() => playlistCoverGradient(props.item.id ?? props.item.name))

async function playShuffled() {
  if (loading.value) return

  const requestId = beginPlaylistPlaybackRequest()
  loading.value = true
  try {
    const detail = await fetchPlaylistDetail(props.item.id)
    if (!isLatestPlaylistPlaybackRequest(requestId)) return

    const songs = shufflePlaylistSongs(detail.songs ?? [])
    if (!songs.length) {
      ElMessage.info(`歌单“${props.item.name}”还没有歌曲`)
      return
    }
    player.playAll(songs)
  } catch (error) {
    if (isLatestPlaylistPlaybackRequest(requestId)) {
      ElMessage.error(error instanceof Error ? error.message : '歌单加载失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <article class="media-card playlist-card">
    <div class="playlist-card-cover">
      <RouterLink :to="`/playlists/${item.id}`" class="playlist-card-cover-link" :aria-label="`查看歌单“${item.name}”`">
        <MediaCover :src="item.coverUrl" :alt="`${item.name}封面`" :label="item.name" :style="{ '--cover-gradient': coverGradient }" />
      </RouterLink>
      <button class="playlist-play-button" type="button" :aria-label="`随机播放歌单“${item.name}”`" :title="`随机播放“${item.name}”`" :aria-busy="loading" :disabled="loading" @click.stop="playShuffled">
        <el-icon v-if="loading" class="is-loading"><Loading /></el-icon>
        <PlaybackIcon v-else />
      </button>
    </div>
    <div class="playlist-card-footer">
      <RouterLink :to="`/playlists/${item.id}`" class="playlist-card-copy">
        <div><h3>{{ item.name }}</h3><p>{{ item.description || '等待你来发现的音乐集合' }}</p></div>
      </RouterLink>
      <button v-if="editable" class="playlist-edit-button" type="button" :aria-label="`编辑歌单“${item.name}”`" title="编辑歌单" @click.stop="emit('edit', item)">
        <el-icon aria-hidden="true"><EditPen /></el-icon><span>编辑</span>
      </button>
    </div>
  </article>
</template>
