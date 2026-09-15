<script setup>
import MediaCover from './MediaCover.vue'
import FavoriteButton from './FavoriteButton.vue'

defineProps({ item: { type: Object, required: true } })
</script>

<template>
  <article class="media-card playlist-card">
    <RouterLink :to="`/playlists/${item.id}`" class="playlist-card-link">
      <MediaCover :src="item.coverUrl" :alt="`${item.name}封面`" :label="item.name" />
      <div><h3>{{ item.name }}</h3><p>{{ item.description || '等待你来发现的音乐集合' }}</p></div>
    </RouterLink>
    <FavoriteButton kind="playlist" :item="item" />
  </article>
</template>
