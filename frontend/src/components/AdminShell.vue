<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { clearAdminAuth, getAdminAuth } from '../auth/adminAuth'
import BrandMark from './BrandMark.vue'

const route = useRoute()
const router = useRouter()
const auth = computed(() => getAdminAuth())

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
      <el-menu :default-active="route.path" router class="admin-menu">
        <el-menu-item index="/admin"><span>概览</span></el-menu-item>
        <el-menu-item index="/admin/users"><span>用户管理</span></el-menu-item>
        <el-menu-item index="/admin/artists"><span>歌手管理</span></el-menu-item>
        <el-menu-item index="/admin/songs"><span>歌曲管理</span></el-menu-item>
        <el-menu-item index="/admin/playlists"><span>歌单管理</span></el-menu-item>
        <el-menu-item index="/admin/banners"><span>轮播图管理</span></el-menu-item>
        <el-menu-item index="/admin/analytics"><span>数据统计</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div><span class="admin-kicker">CONTROL ROOM</span><strong>{{ route.meta.title ?? '后台管理' }}</strong></div>
        <div class="admin-account"><span>{{ auth?.nickname ?? '管理员' }}</span><el-button text @click="logout">退出</el-button></div>
      </el-header>
      <el-main class="admin-main"><RouterView /></el-main>
    </el-container>
  </el-container>
</template>
