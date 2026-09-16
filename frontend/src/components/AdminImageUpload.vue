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
  uploadRequest: { type: Function, default: uploadImage },
})
const emit = defineEmits(['update:modelValue'])

const uploading = ref(false)
const progress = ref(0)
const errorMessage = ref('')
const abortController = ref(null)
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
  const controller = new AbortController()
  abortController.value = controller
  try {
    const result = await props.uploadRequest(options.file, (value) => { progress.value = value }, controller.signal)
    const imageUrl = result?.url || result?.data?.url
    if (!imageUrl) throw new Error(result?.message || '上传接口没有返回图片地址')
    emit('update:modelValue', imageUrl)
    progress.value = 100
    options.onSuccess(result?.data || result)
    ElMessage.success(`${props.label}上传完成`)
  } catch (error) {
    if (controller.signal.aborted || error?.code === 'ERR_CANCELED') {
      errorMessage.value = '上传已取消，可重新选择图片'
      options.onError(error)
    } else {
      const message = error.response?.data?.message || (error instanceof Error ? error.message : `${props.label}上传失败`)
      errorMessage.value = `${message}，可重新选择图片`
      options.onError(new Error(message))
    }
  } finally {
    if (abortController.value === controller) abortController.value = null
    uploading.value = false
  }
}

function cancelUpload() {
  abortController.value?.abort()
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
      <div v-if="uploading" class="upload-progress-row">
        <el-progress :percentage="progress" :show-text="false" :stroke-width="5" />
        <el-button link type="info" size="small" @click="cancelUpload">取消上传</el-button>
      </div>
      <p v-if="errorMessage" class="upload-error" role="alert">{{ errorMessage }}</p>
    </div>
  </div>
</template>
