<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { useFavoriteStore } from '../stores/favorites'

const props = defineProps({
  kind: { type: String, required: true, validator: (value) => ['song', 'playlist'].includes(value) },
  item: { type: Object, required: true },
  labelVisible: { type: Boolean, default: false },
})

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const favorites = useFavoriteStore()
const active = computed(() => props.kind === 'song' ? favorites.songIds.has(props.item.id) : favorites.playlistIds.has(props.item.id))
const busy = computed(() => favorites.isBusy(props.kind, props.item.id))
const targetName = computed(() => props.item.title || props.item.name || '')
const actionLabel = computed(() => `${active.value ? '取消收藏' : '收藏'}${props.kind === 'song' ? '歌曲' : '歌单'}${targetName.value ? `“${targetName.value}”` : ''}`)

async function toggle() {
  if (!auth.isAuthenticated) {
    await router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const next = props.kind === 'song' ? await favorites.toggleSong(props.item) : await favorites.togglePlaylist(props.item)
    ElMessage.success(next ? '已加入我的收藏' : '已取消收藏')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '收藏操作失败')
  }
}
</script>

<template>
  <button class="favorite-button" :class="{ active, 'with-label': labelVisible }" type="button" :aria-label="actionLabel" :title="actionLabel" :disabled="busy" @click.stop="toggle">
    <el-icon><StarFilled v-if="active" /><Star v-else /></el-icon>
    <span v-if="labelVisible">{{ active ? '已收藏' : '收藏' }}</span>
  </button>
</template>
