<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppShell from './components/AppShell.vue'
import AdminShell from './components/AdminShell.vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const route = useRoute()
const isAdminSurface = computed(() => route.path.startsWith('/admin') && route.path !== '/admin/login')
</script>

<template>
  <el-config-provider :locale="zhCn">
    <AdminShell v-if="isAdminSurface" />
    <AppShell v-else />
  </el-config-provider>
</template>
