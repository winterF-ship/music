<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '../api/admin'
import { saveAdminAuth } from '../auth/adminAuth'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })

async function submit() {
  if (!form.username.trim() || !form.password) {
    ElMessage.warning('请输入管理员账号和密码')
    return
  }
  loading.value = true
  try {
    const result = await adminLogin(form.username.trim(), form.password)
    if (result.code !== 200) throw new Error(result.message)
    saveAdminAuth(result.data)
    ElMessage.success('管理员登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin'
    await router.replace(redirect)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="admin-login-page">
    <section class="admin-login-card">
      <p class="eyebrow">ECHO RECORDS / ADMIN</p>
      <h1>进入后台控制室</h1>
      <p class="admin-login-copy">维护用户与音乐资料，所有操作都会经过管理员权限校验。</p>
      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="管理员账号"><el-input v-model="form.username" autocomplete="username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password autocomplete="current-password" @keyup.enter="submit" /></el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="admin-login-button" @click="submit">登录后台</el-button>
      </el-form>
    </section>
  </main>
</template>
