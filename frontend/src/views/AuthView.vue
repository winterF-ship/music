<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BrandMark from '../components/BrandMark.vue'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const mode = ref(route.query.mode === 'register' ? 'register' : 'login')
const formRef = ref()
const serverError = ref('')
const form = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })
const title = computed(() => mode.value === 'login' ? '欢迎回来' : '创建你的音乐账号')
const submitLabel = computed(() => mode.value === 'login' ? '登录' : '注册并登录')
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 4, max: 50, message: '用户名长度为 4 到 50 个字符', trigger: 'blur' }],
  nickname: [{ max: 50, message: '昵称不能超过 50 个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 50, message: '密码长度为 6 到 50 个字符', trigger: 'blur' }],
  confirmPassword: [{ validator: (_rule, value, callback) => value === form.password ? callback() : callback(new Error('两次输入的密码不一致')), trigger: 'blur' }],
}

function targetPath() {
  const value = route.query.redirect
  return typeof value === 'string' && value.startsWith('/') && !value.startsWith('//') ? value : '/'
}

async function submit() {
  serverError.value = ''
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (mode.value === 'login') await auth.signIn({ username: form.username.trim(), password: form.password })
    else await auth.signUp({ username: form.username.trim(), nickname: form.nickname.trim(), password: form.password, confirmPassword: form.confirmPassword })
    ElMessage.success(mode.value === 'login' ? '登录成功' : '注册成功')
    await router.replace(targetPath())
  } catch (error) {
    serverError.value = error.response?.data?.message || '登录信息有误或服务暂时不可用，请检查后重试'
    form.password = ''
    form.confirmPassword = ''
  }
}

function changeMode(next) {
  mode.value = next
  serverError.value = ''
  form.password = ''
  form.confirmPassword = ''
  formRef.value?.clearValidate()
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-card">
      <div class="auth-brand"><BrandMark :size="46" /><div><span class="eyebrow">ECHO ACCOUNT</span><strong>{{ title }}</strong></div></div>
      <el-tabs :model-value="mode" stretch @update:model-value="changeMode">
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>
      <el-alert v-if="serverError" :title="serverError" type="error" show-icon :closable="false" />
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" novalidate @submit.prevent="submit">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" maxlength="50" autocomplete="username" placeholder="4–50 个字符" /></el-form-item>
        <el-form-item v-if="mode === 'register'" label="昵称" prop="nickname"><el-input v-model="form.nickname" maxlength="50" autocomplete="nickname" placeholder="不填写则使用用户名" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password maxlength="50" :autocomplete="mode === 'login' ? 'current-password' : 'new-password'" placeholder="至少 6 个字符" /></el-form-item>
        <el-form-item v-if="mode === 'register'" label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" show-password maxlength="50" autocomplete="new-password" placeholder="再次输入密码" /></el-form-item>
        <el-button class="auth-submit" type="primary" native-type="submit" :loading="auth.loading">{{ submitLabel }}</el-button>
      </el-form>
    </div>
  </section>
</template>
