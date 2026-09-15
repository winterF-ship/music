<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const title = computed(() => String(route.meta.title ?? '页面'))
</script>

<template>
  <el-card shadow="never" class="placeholder-page"><el-result icon="info" :title="`${title}页面正在搭建`" sub-title="页面路由已准备好，后续将接入对应的列表、详情和真实接口数据。"><template #extra><el-button type="primary" @click="router.push('/')">返回首页</el-button></template></el-result></el-card>
</template>
