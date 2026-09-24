<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { ListPlus } from '@lucide/vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useUserPlaylistStore } from '../stores/playlists'

const props = defineProps({ song: { type: Object, required: true } })
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const playlists = useUserPlaylistStore()
const dialogOpen = ref(false)
const selectedPlaylistId = ref()
const loadFailed = ref(false)
const saving = ref(false)
const playlistItems = computed(() => playlists.items)
const selectedPlaylist = computed(() => playlistItems.value.find((item) => String(item.id) === String(selectedPlaylistId.value)))
const actionLabel = computed(() => `将“${props.song.title}”添加到歌单`)

async function openDialog() {
  if (!auth.isAuthenticated) {
    await router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  selectedPlaylistId.value = undefined
  loadFailed.value = false
  dialogOpen.value = true
  try { await playlists.load() } catch { loadFailed.value = true }
}

async function retry() {
  loadFailed.value = false
  try { await playlists.load(true) } catch { loadFailed.value = true }
}

async function addSong() {
  if (!selectedPlaylist.value || saving.value) return
  saving.value = true
  try {
    await playlists.addSong(selectedPlaylist.value.id, props.song.id)
    ElMessage.success(`已将“${props.song.title}”加入“${selectedPlaylist.value.name}”`)
    dialogOpen.value = false
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '加入歌单失败，请稍后重试')
  } finally { saving.value = false }
}

function goCreatePlaylist() {
  dialogOpen.value = false
  router.push({ name: 'my' })
}
</script>

<template>
  <button class="add-to-playlist-button" type="button" :aria-label="actionLabel" :title="actionLabel" @click.stop="openDialog">
    <ListPlus aria-hidden="true" :size="18" :stroke-width="2.1" />
  </button>
  <el-dialog v-model="dialogOpen" class="add-to-playlist-dialog" title="添加到歌单" width="440px">
    <p class="playlist-dialog-copy">选择一张自己的歌单，把这首歌收进去。</p>
    <div v-loading="playlists.loading" class="playlist-picker-body">
      <el-alert v-if="loadFailed" title="歌单加载失败，请重试" type="error" show-icon :closable="false"><template #default><el-button link type="danger" @click="retry">重新加载</el-button></template></el-alert>
      <template v-else-if="playlistItems.length">
        <label id="playlist-picker-label" class="playlist-picker-label">选择歌单</label>
        <el-select v-model="selectedPlaylistId" aria-labelledby="playlist-picker-label" filterable clearable placeholder="请选择歌单" style="width:100%">
          <el-option v-for="playlist in playlistItems" :key="playlist.id" :label="playlist.name" :value="playlist.id" />
        </el-select>
      </template>
      <el-empty v-else-if="!playlists.loading" description="还没有自己的歌单"><el-button type="primary" round :icon="Plus" @click="goCreatePlaylist">去创建歌单</el-button></el-empty>
    </div>
    <template #footer><el-button @click="dialogOpen = false">取消</el-button><el-button v-if="playlistItems.length" type="primary" :disabled="!selectedPlaylist" :loading="saving" @click="addSong">添加到歌单</el-button><el-button v-else-if="!loadFailed" type="primary" @click="goCreatePlaylist">去创建歌单</el-button></template>
  </el-dialog>
</template>
