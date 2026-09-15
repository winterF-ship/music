<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createBanner, deleteBanner, fetchBanners, updateBanner, uploadBannerImage } from '../api/admin'
import ImageCropper from '../components/ImageCropper.vue'

const apiOrigin = 'http://127.0.0.1:8081'
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const rows = ref([])
const dialogOpen = ref(false)
const cropOpen = ref(false)
const cropSource = ref('')
const bannerInput = ref()
const editingId = ref(null)
const form = reactive({ title: '', imageUrl: '', targetUrl: '', sortNo: 0, status: 1 })

function imageSrc(url) { return url ? (url.startsWith('http') ? url : `${apiOrigin}${url}`) : '' }

async function load() {
  loading.value = true
  try {
    const result = await fetchBanners({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    if (result.code !== 200) throw new Error(result.message)
    rows.value = result.data.records
    total.value = result.data.total
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '轮播图列表加载失败')
  } finally { loading.value = false }
}

function search() { page.value = 1; void load() }
function handleSizeChange(value) { page.value = 1; size.value = value; void load() }
function openCreate() { editingId.value = null; Object.assign(form, { title: '', imageUrl: '', targetUrl: '', sortNo: 0, status: 1 }); dialogOpen.value = true }
function openEdit(row) { editingId.value = row.id; Object.assign(form, { title: row.title, imageUrl: row.imageUrl, targetUrl: row.targetUrl ?? '', sortNo: row.sortNo, status: row.status }); dialogOpen.value = true }

function chooseImage() { bannerInput.value?.click() }
function handleImageChange(event) {
  const file = event.target?.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) { ElMessage.warning('请选择图片文件'); return }
  const reader = new FileReader()
  reader.onload = () => { cropSource.value = String(reader.result); cropOpen.value = true }
  reader.readAsDataURL(file)
  if (bannerInput.value) bannerInput.value.value = ''
}
async function uploadCroppedImage(blob) {
  uploading.value = true
  try {
    const result = await uploadBannerImage(blob)
    if (result.code !== 200) throw new Error(result.message)
    form.imageUrl = result.data.url
    cropOpen.value = false
    ElMessage.success('图片已裁剪并上传')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || (error instanceof Error ? error.message : '图片上传失败'))
  } finally { uploading.value = false }
}

async function save() {
  if (!form.title.trim() || !form.imageUrl) { ElMessage.warning('请填写标题并上传轮播图'); return }
  saving.value = true
  try {
    const payload = { title: form.title.trim(), imageUrl: form.imageUrl, targetUrl: form.targetUrl.trim(), sortNo: form.sortNo, status: form.status }
    const result = editingId.value === null ? await createBanner(payload) : await updateBanner(editingId.value, payload)
    if (result.code !== 200) throw new Error(result.message)
    ElMessage.success(editingId.value === null ? '轮播图已创建' : '轮播图已保存')
    dialogOpen.value = false
    await load()
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '保存失败') } finally { saving.value = false }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`删除轮播图“${row.title}”？`, '确认删除', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    const result = await deleteBanner(row.id)
    if (result.code !== 200) throw new Error(result.message)
    ElMessage.success('轮播图已删除')
    await load()
  } catch (error) { if (error === 'cancel' || error === 'close') return; ElMessage.error(error instanceof Error ? error.message : '删除失败') }
}

onMounted(() => { void load() })
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading"><div><p class="eyebrow">BANNER CURATION</p><h1>轮播图管理</h1><p>上传首页视觉素材，控制跳转地址、排序和展示状态。</p></div><el-button type="primary" @click="openCreate">新增轮播图</el-button></div>
    <el-card shadow="never" class="admin-card">
      <div class="admin-toolbar"><el-input v-model="keyword" clearable placeholder="搜索轮播图标题" class="admin-search" @keyup.enter="search" @clear="search" /><el-button type="primary" @click="search">查询</el-button></div>
      <el-table v-loading="loading" :data="rows" row-key="id" empty-text="暂无轮播图数据" class="admin-table">
        <el-table-column label="预览" width="112"><template #default="{ row }"><el-image :src="imageSrc(row.imageUrl)" fit="cover" style="width:76px;height:44px;border-radius:8px" :preview-src-list="[imageSrc(row.imageUrl)]" preview-teleported /></template></el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="targetUrl" label="跳转地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sortNo" label="排序" width="90" />
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '展示中' : '已下线' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="150" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
      </el-table>
      <div class="admin-pagination"><el-pagination v-model:current-page="page" v-model:page-size="size" background layout="total, sizes, prev, pager, next" :total="total" :page-sizes="[10, 20, 50]" @current-change="load" @size-change="handleSizeChange" /></div>
    </el-card>
    <el-dialog v-model="dialogOpen" :title="editingId === null ? '新增轮播图' : '编辑轮播图'" width="560px">
      <el-form label-position="top"><el-form-item label="标题" required><el-input v-model="form.title" maxlength="150" show-word-limit /></el-form-item><el-form-item label="轮播图图片" required><input ref="bannerInput" type="file" accept="image/jpeg,image/png,image/gif,image/webp" hidden @change="handleImageChange" /><el-button :loading="uploading" @click="chooseImage">{{ form.imageUrl ? '重新选择并裁剪' : '选择图片并裁剪' }}</el-button><small class="upload-hint">建议使用横向图片，裁剪框比例为 12:5，可拖动和缩放。</small><div v-if="form.imageUrl" class="banner-upload-preview"><el-image :src="imageSrc(form.imageUrl)" fit="cover" style="width:240px;height:100px;border-radius:10px" /><small class="upload-hint">{{ form.imageUrl }}</small></div></el-form-item><el-form-item label="跳转地址"><el-input v-model="form.targetUrl" maxlength="255" placeholder="如 /playlists 或 https://..." /></el-form-item><el-form-item label="排序号"><el-input-number v-model="form.sortNo" :min="0" :max="9999" /></el-form-item><el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :value="1">展示</el-radio><el-radio :value="0">下线</el-radio></el-radio-group></el-form-item></el-form>
      <template #footer><el-button @click="dialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="cropOpen" title="裁剪轮播图" width="580px" append-to-body><ImageCropper v-if="cropSource" :src="cropSource" :aspect-ratio="2.4" :output-width="1200" @cancel="cropOpen = false" @confirm="uploadCroppedImage" /></el-dialog>
  </section>
</template>
