<script setup>
import { computed, ref } from 'vue'
import { Picture, UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadImage } from '../api/admin'
import { api } from '../api/http'
import { IMAGE_ACCEPT, resolveAssetUrl, validateImageFile } from '../utils/imageUpload'

const props = defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, default: '图片' },
  round: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue'])

const uploading = ref(false)
const progress = ref(0)
const errorMessage = ref('')
const previewUrl = computed(() => resolveAssetUrl(props.modelValue, api.defaults.baseURL))

async function uploadFile(options) {
  const validationError = validateImageFile(options.file)
  if (validationError) {
    errorMessage.value = validationError
    options.onError(new Error(validationError))
    return
  }

  uploading.value = true
  progress.value = 0
  errorMessage.value = ''
  try {
    const result = await uploadImage(options.file, (value) => { progress.value = value })
    if (result.code !== 200) throw new Error(result.message)
    emit('update:modelValue', result.data.url)
    progress.value = 100
    options.onSuccess(result.data)
    ElMessage.success(`${props.label}上传完成`)
  } catch (error) {
    const message = error.response?.data?.message || (error instanceof Error ? error.message : `${props.label}上传失败`)
    errorMessage.value = `${message}，请重新选择图片`
    options.onError(new Error(message))
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="admin-image-upload" :class="{ 'is-round': round }">
    <div class="admin-image-preview" aria-live="polite">
      <el-image v-if="previewUrl" :src="previewUrl" fit="cover" :alt="`${label}预览`" />
      <el-icon v-else aria-hidden="true"><Picture /></el-icon>
    </div>
    <div class="admin-image-actions">
      <el-upload
        :http-request="uploadFile"
        :show-file-list="false"
        :accept="IMAGE_ACCEPT"
        :disabled="uploading"
      >
        <el-button :icon="UploadFilled" :loading="uploading">
          {{ modelValue ? `重新上传${label}` : `选择${label}` }}
        </el-button>
      </el-upload>
      <small class="upload-hint">JPG / PNG / GIF / WebP · 不超过 10MB</small>
      <el-progress v-if="uploading" :percentage="progress" :show-text="false" :stroke-width="5" />
      <p v-if="errorMessage" class="upload-error" role="alert">{{ errorMessage }}</p>
    </div>
  </div>
</template>
