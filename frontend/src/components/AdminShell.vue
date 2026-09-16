<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { clearAdminAuth, getAdminAuth } from '../auth/adminAuth'
import BrandMark from './BrandMark.vue'
import { House, UsersRound, Mic2, Music2, ListMusic, Images, ChartNoAxesCombined, CircleUserRound, Plus } from '@lucide/vue'

const route = useRoute()
const router = useRouter()
const auth = computed(() => getAdminAuth())
const navigation = [
  { path: '/admin', label: '概览', icon: House },
  { path: '/admin/users', label: '用户管理', icon: UsersRound },
  { path: '/admin/artists', label: '歌手管理', icon: Mic2 },
  { path: '/admin/songs', label: '歌曲管理', icon: Music2 },
  { path: '/admin/playlists', label: '歌单管理', icon: ListMusic },
  { path: '/admin/banners', label: '轮播图管理', icon: Images },
  { path: '/admin/analytics', label: '数据统计', icon: ChartNoAxesCombined },
]

function logout() {
  clearAdminAuth()
  ElMessage.success('已退出管理员账号')
  router.replace('/admin/login')
}
</script>

<template>
  <el-container class="admin-shell">
    <el-aside width="232px" class="admin-aside">
      <div class="admin-brand"><BrandMark :size="32" /><span>回声唱片 · 后台</span></div>
      <nav class="admin-navigation" aria-label="后台导航">
        <RouterLink v-for="item in navigation" :key="item.path" :to="item.path" :class="{ 'is-active': route.path === item.path }" :aria-current="route.path === item.path ? 'page' : undefined" :aria-label="item.label" :title="item.label"><component :is="item.icon" :size="19" /><span>{{ item.label }}</span></RouterLink>
      </nav>
      <div class="admin-sidebar-footer">回声唱片 © {{ new Date().getFullYear() }}</div>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <h1 v-if="route.path === '/admin'">后台概览</h1>
        <span v-else class="admin-breadcrumb">后台 / {{ route.meta.title ?? '后台管理' }}</span>
        <div class="admin-header-actions">
          <RouterLink v-if="route.path === '/admin'" to="/admin/songs?action=create" class="admin-create-link"><Plus :size="17" />新增歌曲</RouterLink>
          <div class="admin-account"><CircleUserRound :size="22" /><span>{{ auth?.nickname ?? '管理员' }}</span><el-button text @click="logout">退出</el-button></div>
        </div>
      </el-header>
      <el-main class="admin-main"><RouterView /></el-main>
    </el-container>
  </el-container>
</template>
