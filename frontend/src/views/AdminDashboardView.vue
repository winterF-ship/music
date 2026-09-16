<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ChartNoAxesCombined, ChevronRight, CircleAlert, ImagePlus, ListMusic, Mic2, Music2, RefreshCw, UsersRound } from '@lucide/vue'
import { fetchDashboardSummary, fetchPlaylists, fetchSingers, fetchSongs, fetchUsers } from '../api/admin'
import MediaCover from '../components/MediaCover.vue'

const loading = ref(false)
const summaryError = ref('')
const lastUpdated = ref('')
const requestSerial = ref(0)
let disposed = false

const summary = ref({ users: 0, singers: 0, songs: 0, playlists: 0 })
const recentSongs = ref([])
const recentUsers = ref([])
const sectionStatus = reactive({ songs: 'idle', users: 'idle' })
const sectionErrors = reactive({ songs: '', users: '' })

const countFormatter = new Intl.NumberFormat('zh-CN')
const stats = [
  { key: 'users', label: '注册用户', to: '/admin/users' },
  { key: 'singers', label: '歌手', to: '/admin/artists' },
  { key: 'songs', label: '歌曲', to: '/admin/songs' },
  { key: 'playlists', label: '歌单', to: '/admin/playlists' },
]
const actions = [
  { label: '新增歌曲', icon: Music2, to: '/admin/songs?action=create' },
  { label: '新增歌手', icon: Mic2, to: '/admin/artists?action=create' },
  { label: '创建歌单', icon: ListMusic, to: '/admin/playlists?action=create' },
  { label: '管理轮播图', icon: ImagePlus, to: '/admin/banners' },
  { label: '查看数据统计', icon: ChartNoAxesCombined, to: '/admin/analytics' },
]

function formatCount(value) {
  return countFormatter.format(Number(value) || 0)
}

function formatError(error, fallback) {
  return error?.response?.data?.message || (error instanceof Error ? error.message : fallback)
}

function readResult(result, fallback) {
  if (result.status !== 'fulfilled') throw result.reason
  if (result.value.code !== 200) throw new Error(result.value.message || fallback)
  return result.value.data
}

function setSectionError(key, error, fallback) {
  sectionStatus[key] = 'error'
  sectionErrors[key] = formatError(error, fallback)
}

async function load() {
  const serial = ++requestSerial.value
  loading.value = true
  summaryError.value = ''
  sectionErrors.songs = ''
  sectionErrors.users = ''
  sectionStatus.songs = 'loading'
  sectionStatus.users = 'loading'

  const results = await Promise.allSettled([
    fetchDashboardSummary(),
    fetchSongs({ page: 1, size: 6 }),
    fetchUsers({ page: 1, size: 5 }),
    fetchSingers({ page: 1, size: 1 }),
    fetchPlaylists({ page: 1, size: 1 }),
  ])

  if (disposed || serial !== requestSerial.value) return

  try {
    const dashboard = readResult(results[0], '概览数据加载失败')
    summary.value = dashboard
  } catch (error) {
    summaryError.value = formatError(error, '概览数据加载失败')
  }

  try {
    const songsPage = readResult(results[1], '歌曲列表加载失败')
    recentSongs.value = songsPage.records ?? []
    sectionStatus.songs = 'ready'
  } catch (error) {
    setSectionError('songs', error, '歌曲列表加载失败')
  }

  try {
    const usersPage = readResult(results[2], '用户列表加载失败')
    recentUsers.value = usersPage.records ?? []
    sectionStatus.users = 'ready'
  } catch (error) {
    setSectionError('users', error, '用户列表加载失败')
  }

  // Keep the small management pages' totals in sync with the dashboard endpoint even
  // when an older backend has not added those fields to its dashboard response yet.
  try {
    const singersPage = readResult(results[3], '歌手列表加载失败')
    if (summary.value.singers === 0 && singersPage.total) summary.value = { ...summary.value, singers: singersPage.total }
  } catch {
    // The dashboard summary remains the source of truth when this compatibility read fails.
  }
  try {
    const playlistsPage = readResult(results[4], '歌单列表加载失败')
    if (summary.value.playlists === 0 && playlistsPage.total) summary.value = { ...summary.value, playlists: playlistsPage.total }
  } catch {
    // The dashboard summary remains the source of truth when this compatibility read fails.
  }

  lastUpdated.value = new Intl.DateTimeFormat('zh-CN', { hour: '2-digit', minute: '2-digit' }).format(new Date())
  loading.value = false
}

onMounted(() => { void load() })
onBeforeUnmount(() => { disposed = true })
</script>

<template>
  <section class="admin-page overview-page" aria-label="后台概览">
    <div class="overview-update">
      <span role="status">{{ lastUpdated ? `更新于 ${lastUpdated}` : '正在获取数据' }}</span>
      <el-button text :loading="loading" @click="load"><RefreshCw :size="14" />刷新数据</el-button>
    </div>
    <div v-if="summaryError" class="overview-error" role="alert">
      <CircleAlert :size="18" /><span>概览数据加载失败：{{ summaryError }}</span>
      <el-button text :disabled="loading" @click="load">重试</el-button>
    </div>
    <nav v-loading="loading" class="overview-stats" aria-label="内容概况">
      <RouterLink v-for="stat in stats" :key="stat.key" :to="stat.to" class="overview-stat">
        <span>{{ stat.label }}</span><strong>{{ summaryError ? '—' : formatCount(summary[stat.key]) }}</strong>
      </RouterLink>
    </nav>

    <section class="overview-panel overview-songs" aria-labelledby="recent-songs-title">
      <header class="overview-panel-heading"><h2 id="recent-songs-title">最近收录</h2><RouterLink to="/admin/songs">全部歌曲<ChevronRight :size="15" /></RouterLink></header>
      <div v-if="sectionStatus.songs === 'error'" class="overview-empty" role="alert"><CircleAlert :size="24" /><p>{{ sectionErrors.songs }}</p><el-button :disabled="loading" @click="load">重新加载</el-button></div>
      <div v-else v-loading="loading" class="overview-table-scroll" tabindex="0" aria-label="最近收录歌曲表格">
        <table class="overview-table">
          <thead><tr><th scope="col" class="overview-index">#</th><th scope="col" class="overview-cover-col">封面</th><th scope="col">歌曲名称</th><th scope="col">歌手</th><th scope="col">歌词</th><th scope="col" class="overview-operation">操作</th></tr></thead>
          <tbody><tr v-for="(song, index) in recentSongs" :key="song.id">
            <td class="overview-index">{{ index + 1 }}</td>
            <td><MediaCover :src="song.coverUrl" :alt="`${song.title}封面`" :label="song.title" /></td>
            <td class="overview-title"><span :title="song.title">{{ song.title }}</span></td>
            <td><span class="overview-ellipsis" :title="song.singerName">{{ song.singerName || '未设置歌手' }}</span></td>
            <td><span class="overview-status" :class="{ 'is-missing': !song.lyricUrl }">{{ song.lyricUrl ? '已上传' : '未上传' }}</span></td>
            <td><RouterLink :to="{ path: '/admin/songs', query: { edit: song.id } }" :aria-label="`编辑歌曲：${song.title}`">编辑<ChevronRight :size="14" /></RouterLink></td>
          </tr></tbody>
        </table>
        <div v-if="!recentSongs.length" class="overview-empty"><Music2 :size="26" /><p>{{ loading ? '正在加载歌曲…' : '还没有收录歌曲' }}</p><RouterLink v-if="!loading" to="/admin/songs?action=create">新增第一首歌曲</RouterLink></div>
      </div>
    </section>

    <div class="overview-bottom">
      <section class="overview-panel" aria-labelledby="recent-users-title">
        <header class="overview-panel-heading"><h2 id="recent-users-title">最近注册</h2><RouterLink to="/admin/users">全部用户<ChevronRight :size="15" /></RouterLink></header>
        <div v-if="sectionStatus.users === 'error'" class="overview-empty" role="alert"><CircleAlert :size="24" /><p>{{ sectionErrors.users }}</p><el-button :disabled="loading" @click="load">重新加载</el-button></div>
        <div v-else v-loading="loading" class="overview-table-scroll" tabindex="0" aria-label="最近注册用户表格">
          <table class="overview-table overview-user-table"><thead><tr><th scope="col" class="overview-index">#</th><th scope="col">用户名</th><th scope="col">状态</th></tr></thead>
            <tbody><tr v-for="(user, index) in recentUsers" :key="user.id"><td class="overview-index">{{ index + 1 }}</td><td><span class="overview-ellipsis" :title="user.username">{{ user.username }}</span></td><td><span class="overview-status" :class="{ 'is-missing': user.status !== 1 }">{{ user.status === 1 ? '已启用' : '已禁用' }}</span></td></tr></tbody>
          </table>
          <div v-if="!recentUsers.length" class="overview-empty"><UsersRound :size="24" /><p>{{ loading ? '正在加载用户…' : '还没有注册用户' }}</p></div>
        </div>
      </section>
      <section class="overview-panel" aria-labelledby="quick-actions-title">
        <header class="overview-panel-heading"><h2 id="quick-actions-title">常用操作</h2></header>
        <nav class="overview-actions" aria-label="常用操作"><RouterLink v-for="action in actions" :key="action.label" :to="action.to"><component :is="action.icon" :size="19" /><span>{{ action.label }}</span><ChevronRight :size="16" /></RouterLink></nav>
      </section>
    </div>
  </section>
</template>
