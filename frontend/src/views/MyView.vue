<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, User } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useFavoriteStore } from '../stores/favorites'
import { useUserPlaylistStore } from '../stores/playlists'
import MediaCover from '../components/MediaCover.vue'
import PlaylistCard from '../components/PlaylistCard.vue'

const router = useRouter()
const auth = useAuthStore()
const favorites = useFavoriteStore()
const userPlaylists = useUserPlaylistStore()
const failed = ref(false)
const playlists = computed(() => userPlaylists.items)
const playlistsLoading = computed(() => userPlaylists.loading)
const playlistsFailed = computed(() => Boolean(userPlaylists.lastError))
const playlistDialogOpen = ref(false)
const playlistSaving = ref(false)
const playlistFormRef = ref()
const playlistForm = reactive({ name: '', description: '' })
const displayName = computed(() => auth.profile?.nickname || auth.session?.nickname || '音乐朋友')
const playlistRules = {
  name: [{ required: true, message: '请输入歌单名称', trigger: 'blur' }, { max: 150, message: '歌单名称不能超过 150 个字符', trigger: 'blur' }],
  description: [{ max: 500, message: '简介不能超过 500 个字符', trigger: 'blur' }],
}

async function load() {
  failed.value = false
  try { await favorites.load(true) } catch { failed.value = true }
}

async function loadUserPlaylists() {
  try { await userPlaylists.load(true) } catch { /* inline error state is rendered below */ }
}

function openPlaylistDialog() {
  Object.assign(playlistForm, { name: '', description: '' })
  playlistDialogOpen.value = true
}

async function savePlaylist() {
  const valid = await playlistFormRef.value?.validate().catch(() => false)
  if (!valid || playlistSaving.value) return
  playlistSaving.value = true
  try {
    await userPlaylists.create({ name: playlistForm.name.trim(), description: playlistForm.description.trim() })
    playlistDialogOpen.value = false
    ElMessage.success('歌单已创建')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '歌单创建失败，请稍后重试')
  } finally { playlistSaving.value = false }
}

onMounted(() => { void Promise.all([load(), loadUserPlaylists()]) })
</script>

<template>
  <section class="account-page my-page" v-loading="favorites.loading">
    <header class="my-hero">
      <div class="my-identity">
        <MediaCover :src="auth.profile?.avatar" :alt="`${displayName}头像`" :label="displayName" round />
        <div>
          <span class="eyebrow">YOUR SPACE</span>
          <h1>{{ displayName }}，欢迎回来</h1>
          <p>{{ auth.profile?.profile || '把喜欢的声音收进自己的歌单，随时回来继续听。' }}</p>
        </div>
      </div>
      <button class="my-profile-link" type="button" @click="router.push('/profile')"><el-icon><User /></el-icon>编辑资料</button>
    </header>

    <div class="my-stats" aria-label="我的收藏概览">
      <div><strong>{{ favorites.playlists.length }}</strong><span>收藏歌单</span></div>
      <div><strong>{{ favorites.songs.length }}</strong><span>收藏歌曲</span></div>
    </div>

    <section class="my-collection-section">
      <div class="section-title"><div><span class="eyebrow">MY PLAYLISTS</span><h2>我创建的歌单</h2></div><el-button class="create-playlist-button" type="primary" round :icon="Plus" @click="openPlaylistDialog">添加歌单</el-button></div>
      <div v-loading="playlistsLoading" class="playlist-section-body">
        <div v-if="playlistsFailed" class="api-error"><div><strong>我的歌单加载失败</strong><p>请检查网络连接后重试。</p></div><el-button :icon="Refresh" round @click="loadUserPlaylists">重试</el-button></div>
        <div v-else-if="playlists.length" class="media-grid catalog-grid"><PlaylistCard v-for="item in playlists" :key="item.id" :item="item" /></div>
        <el-empty v-else-if="!playlistsLoading" description="还没有创建歌单，先收集一份属于自己的声音吧"><el-button type="primary" round :icon="Plus" @click="openPlaylistDialog">创建第一张歌单</el-button></el-empty>
      </div>
    </section>

    <section class="my-collection-section">
      <div class="section-title"><div><span class="eyebrow">SAVED PLAYLISTS</span><h2>我收藏的歌单</h2></div><RouterLink to="/playlists">发现更多 <span aria-hidden="true">↗</span></RouterLink></div>
      <div v-if="failed" class="api-error"><div><strong>收藏歌单加载失败</strong><p>请检查网络连接后重试。</p></div><el-button :icon="Refresh" round @click="load">重试</el-button></div>
      <div v-else-if="favorites.playlists.length" class="media-grid catalog-grid"><PlaylistCard v-for="item in favorites.playlists" :key="item.id" :item="item" /></div>
      <el-empty v-else-if="!favorites.loading" description="还没有收藏歌单，去歌单广场看看吧"><el-button type="primary" round @click="router.push('/playlists')">浏览歌单</el-button></el-empty>
    </section>

    <el-dialog v-model="playlistDialogOpen" class="my-playlist-dialog" title="添加歌单" width="500px" @closed="playlistFormRef?.clearValidate()">
      <p class="playlist-dialog-copy">给这段声音起个名字，之后可以在这里继续收集喜欢的歌曲。</p>
      <el-form ref="playlistFormRef" :model="playlistForm" :rules="playlistRules" label-position="top" novalidate @submit.prevent="savePlaylist">
        <el-form-item label="歌单名称" prop="name"><el-input v-model="playlistForm.name" maxlength="150" show-word-limit autofocus placeholder="例如：夜晚散步听什么" /></el-form-item>
        <el-form-item label="歌单简介" prop="description"><el-input v-model="playlistForm.description" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="写下这张歌单想留住的情绪（可选）" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="playlistDialogOpen = false">取消</el-button><el-button type="primary" native-type="submit" :loading="playlistSaving" @click="savePlaylist">创建歌单</el-button></template>
    </el-dialog>
  </section>
</template>
