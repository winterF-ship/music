<script setup>
import { usePlayerStore } from '../stores/player'
import MediaCover from './MediaCover.vue'
import FavoriteButton from './FavoriteButton.vue'
import AddToPlaylistButton from './AddToPlaylistButton.vue'
import PlaybackIcon from './PlaybackIcon.vue'
const props = defineProps({ songs: { type: Array, default: () => [] }, emptyText: { type: String, default: '这里还没有歌曲' } })
const player = usePlayerStore()
</script>
<template>
  <div v-if="songs.length" class="song-list">
    <div v-for="(song, index) in songs" :key="song.id" class="song-row">
      <button class="song-main" type="button" :aria-label="`播放${song.title}`" @click="player.playSong(song, props.songs)">
        <span class="song-index">{{ String(index + 1).padStart(2, '0') }}</span>
        <MediaCover :src="song.coverUrl" :alt="`${song.title}封面`" :label="song.title" />
        <span class="song-copy"><strong>{{ song.title }}</strong><small>{{ song.singerName || '未知歌手' }}</small></span>
        <span class="song-play" aria-hidden="true"><PlaybackIcon /></span>
      </button>
      <div class="song-row-actions"><AddToPlaylistButton :song="song" /><FavoriteButton kind="song" :item="song" /></div>
    </div>
  </div>
  <el-empty v-else :description="emptyText" :image-size="90" />
</template>
