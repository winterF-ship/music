<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import MediaCover from '../components/MediaCover.vue'

const auth = useAuthStore()
const formRef = ref()
const saving = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const form = reactive({ nickname: '', profile: '' })
const rules = { nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }, { max: 50, message: '昵称不能超过 50 个字符', trigger: 'blur' }] }

function syncForm() {
  form.nickname = auth.profile?.nickname || auth.session?.nickname || ''
  form.profile = auth.profile?.profile || ''
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await auth.saveProfile({ nickname: form.nickname.trim(), profile: form.profile.trim() })
    ElMessage.success('资料已保存')
  } catch (error) { ElMessage.error(error.response?.data?.message || '资料保存失败') } finally { saving.value = false }
}

function validateAvatar(file) {
  if (!file.type.startsWith('image/')) { ElMessage.warning('请选择 JPG、PNG、GIF 或 WebP 图片'); return false }
  if (file.size > 10 * 1024 * 1024) { ElMessage.warning('头像图片不能超过 10MB'); return false }
  return true
}

async function upload(options) {
  if (!validateAvatar(options.file)) { options.onError(new Error('头像文件不符合要求')); return }
  uploading.value = true
  uploadProgress.value = 0
  try {
    await auth.saveAvatar(options.file, (value) => { uploadProgress.value = value })
    options.onSuccess(auth.profile)
    ElMessage.success('头像已更新')
  } catch (error) {
    options.onError(error)
    ElMessage.error(error.response?.data?.message || '头像上传失败')
  } finally { uploading.value = false }
}

onMounted(async () => { if (!auth.profile) await auth.refreshProfile(); syncForm() })
</script>

<template>
  <section class="account-page">
    <header class="page-heading"><span class="eyebrow">YOUR PROFILE</span><h1>个人资料</h1><p>设置你在回声唱片中的头像、昵称和个人介绍。</p></header>
    <div class="profile-layout">
      <aside class="profile-avatar-card">
        <MediaCover :src="auth.profile?.avatar" :alt="`${auth.profile?.nickname || '用户'}头像`" :label="auth.profile?.nickname || '我'" round />
        <strong>{{ auth.profile?.nickname || auth.session?.nickname }}</strong><small>@{{ auth.session?.username }}</small>
        <el-upload :http-request="upload" :show-file-list="false" accept="image/jpeg,image/png,image/gif,image/webp"><el-button :icon="UploadFilled" :loading="uploading">上传新头像</el-button></el-upload>
        <el-progress v-if="uploading" :percentage="uploadProgress" :show-text="false" />
        <p>支持 JPG、PNG、GIF、WebP，最大 10MB。</p>
      </aside>
      <div class="profile-form-card">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" novalidate @submit.prevent="save">
          <el-form-item label="用户名"><el-input :model-value="auth.session?.username" disabled /></el-form-item>
          <el-form-item label="昵称" prop="nickname"><el-input v-model="form.nickname" maxlength="50" show-word-limit /></el-form-item>
          <el-form-item label="个人简介"><el-input v-model="form.profile" type="textarea" :rows="6" maxlength="500" show-word-limit placeholder="写一点关于你和音乐的故事" /></el-form-item>
          <el-button type="primary" native-type="submit" :loading="saving">保存资料</el-button>
        </el-form>
      </div>
    </div>
  </section>
</template>
