<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowUpRight, Check, ChevronRight, CircleAlert, ImagePlus, ListMusic, Mic2, Music2, Plus, RefreshCw, UsersRound } from '@lucide/vue'
import { fetchBanners, fetchDashboardSummary, fetchPlaylists, fetchSingers, fetchSongs, fetchUsers } from '../api/admin'
import MediaCover from '../components/MediaCover.vue'

const loading = ref(false)
const router = useRouter()
const summaryError = ref('')
const lastUpdated = ref('')
const requestSerial = ref(0)
let disposed = false

const summary = ref({ users: 0, singers: 0, songs: 0, playlists: 0 })
const recentSongs = ref([])
const recentUsers = ref([])
const bannerTotal = ref(0)
const sectionStatus = reactive({ songs: 'idle', users: 'idle', banners: 'idle' })
const sectionErrors = reactive({ songs: '', users: '', banners: '' })

const countFormatter = new Intl.NumberFormat('zh-CN')
const todayLabel = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' })

const stats = computed(() => [
  { key: 'users', label: '注册用户', description: '可登录的用户账号', icon: UsersRound, accent: 'mint', to: '/admin/users' },
  { key: 'singers', label: '歌手', description: '内容库中的音乐人', icon: Mic2, accent: 'lilac', to: '/admin/artists' },
  { key: 'songs', label: '歌曲', description: '已登记的音频资源', icon: Music2, accent: 'peach', to: '/admin/songs' },
  { key: 'playlists', label: '歌单', description: '正在维护的歌单集合', icon: ListMusic, accent: 'gold', to: '/admin/playlists' },
])

const healthItems = computed(() => [
  {
    label: '歌曲资源',
    value: formatCount(summary.value.songs),
    note: summary.value.songs ? '音频目录已建立' : '还没有歌曲，先录入第一首',
    tone: summary.value.songs ? 'ready' : 'attention',
    to: '/admin/songs',
  },
  {
    label: '歌手资料',
    value: formatCount(summary.value.singers),
    note: summary.value.singers ? '歌手归属可正常维护' : '歌曲需要先关联歌手',
    tone: summary.value.singers ? 'ready' : 'attention',
    to: '/admin/artists',
  },
  {
    label: '首页轮播',
    value: formatCount(bannerTotal.value),
    note: bannerTotal.value ? '首页内容位已配置' : '暂无首页视觉内容',
    tone: bannerTotal.value ? 'ready' : 'attention',
    to: '/admin/banners',
  },
])

const bannerSummary = computed(() => {
  if (sectionStatus.banners === 'error') return '轮播数据同步失败'
  if (!bannerTotal.value) return '还没有配置首页轮播位'
  return `共 ${formatCount(bannerTotal.value)} 个轮播条目已配置`
})

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
  sectionErrors.banners = ''
  sectionStatus.songs = 'loading'
  sectionStatus.users = 'loading'
  sectionStatus.banners = 'loading'

  const results = await Promise.allSettled([
    fetchDashboardSummary(),
    fetchSongs({ page: 1, size: 6 }),
    fetchUsers({ page: 1, size: 5 }),
    fetchBanners({ page: 1, size: 6 }),
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

  try {
    const bannersPage = readResult(results[3], '轮播列表加载失败')
    bannerTotal.value = bannersPage.total ?? bannersPage.records?.length ?? 0
    sectionStatus.banners = 'ready'
  } catch (error) {
    setSectionError('banners', error, '轮播列表加载失败')
  }

  // Keep the small management pages' totals in sync with the dashboard endpoint even
  // when an older backend has not added those fields to its dashboard response yet.
  try {
    const singersPage = readResult(results[4], '歌手列表加载失败')
    if (summary.value.singers === 0 && singersPage.total) summary.value = { ...summary.value, singers: singersPage.total }
  } catch {
    // The dashboard summary remains the source of truth when this compatibility read fails.
  }
  try {
    const playlistsPage = readResult(results[5], '歌单列表加载失败')
    if (summary.value.playlists === 0 && playlistsPage.total) summary.value = { ...summary.value, playlists: playlistsPage.total }
  } catch {
    // The dashboard summary remains the source of truth when this compatibility read fails.
  }

  lastUpdated.value = new Intl.DateTimeFormat('zh-CN', { hour: '2-digit', minute: '2-digit' }).format(new Date())
  loading.value = false
}

function goTo(path) {
  void router.push(path)
}

onMounted(() => { void load() })
onBeforeUnmount(() => { disposed = true })
</script>

<template>
  <section class="admin-page dashboard-page">
    <div class="dashboard-hero">
      <div class="dashboard-hero-copy">
        <p class="dashboard-eyebrow">CONTROL ROOM / {{ todayLabel.format(new Date()).toUpperCase() }}</p>
        <h1>让每一首声音，都有位置。</h1>
        <p class="dashboard-hero-description">这里是回声唱片的内容控制室。录入音乐、整理歌手与歌单，让前台每一次播放都有可靠的内容可抵达。</p>
        <div class="dashboard-hero-actions">
          <el-button type="primary" class="dashboard-primary-action" @click="goTo('/admin/songs')"><Plus :size="16" />录入新歌曲</el-button>
          <el-button class="dashboard-secondary-action" @click="goTo('/admin/analytics')">查看数据 <ArrowUpRight :size="16" /></el-button>
        </div>
      </div>
      <div class="dashboard-hero-art" aria-hidden="true">
        <div class="dashboard-orbit dashboard-orbit-outer"><div class="dashboard-orbit dashboard-orbit-inner"><Music2 :size="38" stroke-width="1.6" /></div></div>
        <div class="dashboard-live-badge"><span></span> LIVE CATALOG</div>
        <div class="dashboard-hero-count"><strong>{{ loading ? '—' : formatCount(summary.songs) }}</strong><span>首歌曲在声场中</span></div>
      </div>
    </div>

    <div v-if="summaryError" class="dashboard-error" role="alert">
      <div><strong>概览数据暂时不可用</strong><p>{{ summaryError }}</p></div>
      <el-button type="danger" plain :loading="loading" @click="load">重新加载</el-button>
    </div>

    <div class="dashboard-section-heading">
      <div><p class="eyebrow">LIBRARY PULSE</p><h2>内容库心电图</h2><p>掌握当前内容规模，再决定今天先补哪一块。</p></div>
      <div class="dashboard-refresh"><span v-if="lastUpdated">更新于 {{ lastUpdated }}</span><el-button text :loading="loading" @click="load"><RefreshCw :size="15" />刷新数据</el-button></div>
    </div>

    <div v-loading="loading" class="dashboard-stats">
      <el-card v-for="stat in stats" :key="stat.key" shadow="never" class="dashboard-stat-card">
        <div class="dashboard-stat-top"><div class="dashboard-stat-icon" :class="`is-${stat.accent}`"><component :is="stat.icon" :size="19" /></div><span>{{ stat.label }}</span></div>
        <strong>{{ formatCount(summary[stat.key]) }}</strong>
        <div class="dashboard-stat-foot"><small>{{ stat.description }}</small><RouterLink :to="stat.to">管理 <ChevronRight :size="14" /></RouterLink></div>
      </el-card>
    </div>

    <div class="dashboard-main-grid">
      <el-card shadow="never" class="dashboard-card dashboard-recent-card">
        <template #header><div class="dashboard-card-heading"><div><p class="eyebrow">RECENTLY ADDED</p><h3>最近收录</h3></div><RouterLink to="/admin/songs">全部歌曲 <ArrowUpRight :size="15" /></RouterLink></div></template>
        <div v-if="sectionStatus.songs === 'error'" class="dashboard-inline-error"><CircleAlert :size="18" /><span>{{ sectionErrors.songs }}</span></div>
        <div v-else-if="!recentSongs.length && !loading" class="dashboard-empty"><Music2 :size="26" /><strong>还没有歌曲</strong><p>录入第一首歌曲后，它会出现在这里。</p><RouterLink to="/admin/songs">去录入歌曲</RouterLink></div>
        <div v-else class="dashboard-song-list">
          <div v-for="(song, index) in recentSongs" :key="song.id" class="dashboard-song-row">
            <span class="dashboard-song-index">{{ String(index + 1).padStart(2, '0') }}</span>
            <MediaCover :src="song.coverUrl" :alt="`${song.title}封面`" :label="song.title" />
            <div class="dashboard-song-copy"><strong>{{ song.title }}</strong><small>{{ song.singerName || '未命名歌手' }}</small></div>
            <span class="dashboard-song-status" :class="song.lyricUrl ? 'is-ready' : 'is-muted'"><Check v-if="song.lyricUrl" :size="13" />{{ song.lyricUrl ? '歌词已配' : '未配歌词' }}</span>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="dashboard-card dashboard-actions-card">
        <template #header><div class="dashboard-card-heading"><div><p class="eyebrow">NEXT MOVES</p><h3>快捷入口</h3></div><span class="dashboard-heading-note">常用操作</span></div></template>
        <nav class="dashboard-action-list" aria-label="后台快捷入口">
          <RouterLink to="/admin/songs" class="dashboard-action-link"><span class="dashboard-action-icon is-peach"><Music2 :size="18" /></span><span><strong>录入新歌曲</strong><small>补齐音频、封面与歌词</small></span><ArrowUpRight :size="16" /></RouterLink>
          <RouterLink to="/admin/artists" class="dashboard-action-link"><span class="dashboard-action-icon is-lilac"><Mic2 :size="18" /></span><span><strong>维护歌手资料</strong><small>整理音乐人的归属信息</small></span><ArrowUpRight :size="16" /></RouterLink>
          <RouterLink to="/admin/playlists" class="dashboard-action-link"><span class="dashboard-action-icon is-gold"><ListMusic :size="18" /></span><span><strong>编排歌单</strong><small>组合前台可播放的内容集合</small></span><ArrowUpRight :size="16" /></RouterLink>
          <RouterLink to="/admin/banners" class="dashboard-action-link"><span class="dashboard-action-icon is-mint"><ImagePlus :size="18" /></span><span><strong>更新首页轮播</strong><small>管理首页第一眼的视觉内容</small></span><ArrowUpRight :size="16" /></RouterLink>
        </nav>
      </el-card>
    </div>

    <div class="dashboard-bottom-grid">
      <el-card shadow="never" class="dashboard-card dashboard-users-card">
        <template #header><div class="dashboard-card-heading"><div><p class="eyebrow">ACCOUNT WATCH</p><h3>最近注册</h3></div><RouterLink to="/admin/users">用户管理 <ArrowUpRight :size="15" /></RouterLink></div></template>
        <div v-if="sectionStatus.users === 'error'" class="dashboard-inline-error"><CircleAlert :size="18" /><span>{{ sectionErrors.users }}</span></div>
        <div v-else-if="!recentUsers.length && !loading" class="dashboard-empty dashboard-empty-compact"><UsersRound :size="24" /><strong>还没有用户</strong><p>公开注册的账号会显示在这里。</p></div>
        <div v-else class="dashboard-user-list">
          <div v-for="user in recentUsers" :key="user.id" class="dashboard-user-row"><div class="dashboard-user-avatar">{{ (user.nickname || user.username || '用').slice(0, 1) }}</div><div class="dashboard-user-copy"><strong>{{ user.nickname || '未设置昵称' }}</strong><small>@{{ user.username }}</small></div><el-tag size="small" :type="user.status === 1 ? 'success' : 'info'">{{ user.status === 1 ? '已启用' : '已禁用' }}</el-tag></div>
        </div>
      </el-card>

      <el-card shadow="never" class="dashboard-card dashboard-health-card">
        <template #header><div class="dashboard-card-heading"><div><p class="eyebrow">SURFACE CHECK</p><h3>前台内容状态</h3></div><span class="dashboard-heading-note">实时检查</span></div></template>
        <div class="dashboard-health-list">
          <RouterLink v-for="item in healthItems" :key="item.label" :to="item.to" class="dashboard-health-row"><span class="dashboard-health-dot" :class="`is-${item.tone}`"><Check v-if="item.tone === 'ready'" :size="12" /><CircleAlert v-else :size="13" /></span><span class="dashboard-health-copy"><strong>{{ item.label }}</strong><small>{{ item.note }}</small></span><span class="dashboard-health-value">{{ item.value }}</span><ChevronRight :size="15" /></RouterLink>
        </div>
        <div class="dashboard-banner-summary"><span class="dashboard-banner-signal"></span><span>{{ bannerSummary }}</span><RouterLink to="/admin/banners">查看轮播</RouterLink></div>
      </el-card>
    </div>
  </section>
</template>
