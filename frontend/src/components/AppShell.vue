<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Menu as MenuIcon } from '@element-plus/icons-vue'
import PlayerDock from './PlayerDock.vue'
import BrandMark from './BrandMark.vue'
import { assetUrl } from '../api/catalog'
import { useAuthStore } from '../stores/auth'
import { useFavoriteStore } from '../stores/favorites'
import { useUserPlaylistStore } from '../stores/playlists'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const favorites = useFavoriteStore()
const userPlaylists = useUserPlaylistStore()
const query = ref('')
const mobileOpen = ref(false)
const accountDropdown = ref()
const activePath = computed(() => route.path.startsWith('/playlists') ? '/playlists' : route.path.startsWith('/artists') ? '/artists' : '/')

watch(() => route.fullPath, () => {
  mobileOpen.value = false
  query.value = typeof route.query.q === 'string' ? route.query.q : ''
}, { immediate: true })

watch(() => auth.session?.id, (userId) => {
  if (userId) void favorites.load(true)
  else { favorites.reset(); userPlaylists.reset() }
}, { immediate: true })

watch(() => auth.isAuthenticated, (loggedIn) => {
  if (!loggedIn && route.meta.user) void router.replace({ name: 'login', query: { redirect: route.fullPath } })
})

function submitSearch() {
  const value = query.value.trim()
  if (value) router.push({ path: '/search', query: { q: value } })
}

async function handleAccountCommand(command) {
  if (command === 'logout') {
    favorites.reset()
    await router.replace('/')
    auth.logout()
    return
  }
  await router.push(command === 'my' ? '/me' : command === 'profile' ? '/profile' : '/favorites')
}

function openAccountMenu() { accountDropdown.value?.handleOpen() }
</script>

<template>
  <div class="public-shell">
    <header class="public-header"><div class="header-inner">
      <RouterLink to="/" class="public-brand" aria-label="回声唱片首页"><BrandMark /><span>回声唱片</span></RouterLink>
      <button class="mobile-menu-button" type="button" aria-label="切换导航" @click="mobileOpen = !mobileOpen"><el-icon><MenuIcon /></el-icon></button>
      <nav class="public-nav" :class="{ open: mobileOpen }" aria-label="主导航"><RouterLink to="/" :class="{ active: activePath === '/' }">发现</RouterLink><RouterLink to="/playlists" :class="{ active: activePath === '/playlists' }">歌单</RouterLink><RouterLink to="/artists" :class="{ active: activePath === '/artists' }">歌手</RouterLink><RouterLink to="/me" :class="{ active: route.path.startsWith('/me') }">我的</RouterLink></nav>
      <form class="public-search" role="search" novalidate @submit.prevent="submitSearch"><el-input v-model="query" clearable placeholder="搜索歌曲或歌手" aria-label="搜索歌曲或歌手" :prefix-icon="Search" /></form>
      <el-dropdown v-if="auth.isAuthenticated" ref="accountDropdown" trigger="click" @command="handleAccountCommand">
        <button class="user-menu-trigger" type="button" aria-label="打开用户菜单" @click="openAccountMenu">
          <el-avatar :size="34" :src="assetUrl(auth.profile?.avatar)">{{ (auth.profile?.nickname || auth.session?.nickname || '我').slice(0, 1) }}</el-avatar>
          <span>{{ auth.profile?.nickname || auth.session?.nickname }}</span>
        </button>
        <template #dropdown><el-dropdown-menu><el-dropdown-item command="my">我的页面</el-dropdown-item><el-dropdown-item command="profile">个人资料</el-dropdown-item><el-dropdown-item command="favorites">我的收藏</el-dropdown-item><el-dropdown-item divided command="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
      </el-dropdown>
      <RouterLink v-else to="/login" class="login-link">登录 / 注册</RouterLink>
      <RouterLink to="/admin/login" class="studio-link">管理后台</RouterLink>
    </div></header>
    <main class="public-main"><RouterView /></main>
    <PlayerDock />
  </div>
</template>
