<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createSong, deleteSong, fetchSingers, fetchSongs, updateSong, uploadAudio, uploadLyrics } from '../api/admin'
import AdminImageUpload from '../components/AdminImageUpload.vue'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const uploadingLyrics = ref(false)
const lyricProgress = ref(0)
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const rows = ref([])
const singers = ref([])
const dialogOpen = ref(false)
const editingId = ref(null)
const form = reactive({ title: '', singerId: undefined, coverUrl: '', audioUrl: '', lyricUrl: '', durationSeconds: undefined })

async function load() {
  loading.value = true
  try {
    const result = await fetchSongs({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    if (result.code !== 200) throw new Error(result.message)
    rows.value = result.data.records
    total.value = result.data.total
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '歌曲列表加载失败') } finally { loading.value = false }
}
async function loadSingers() {
  const result = await fetchSingers({ page: 1, size: 100 })
  if (result.code === 200) singers.value = result.data.records
}
function search() { page.value = 1; void load() }
function openCreate() { editingId.value = null; Object.assign(form, { title: '', singerId: undefined, coverUrl: '', audioUrl: '', lyricUrl: '', durationSeconds: undefined }); lyricProgress.value = 0; dialogOpen.value = true }
function openEdit(row) { editingId.value = row.id; Object.assign(form, { title: row.title, singerId: row.singerId, coverUrl: row.coverUrl ?? '', audioUrl: row.audioUrl, lyricUrl: row.lyricUrl ?? '', durationSeconds: row.duration ?? undefined }); lyricProgress.value = 0; dialogOpen.value = true }
async function uploadAudioFile(options) {
  uploading.value = true
  try {
    const result = await uploadAudio(options.file)
    if (result.code !== 200) throw new Error(result.message)
    form.audioUrl = result.data.url
    ElMessage.success('音频上传完成')
    options.onSuccess(result.data)
  } catch (error) {
    const uploadError = Object.assign(new Error(error instanceof Error ? error.message : '音频上传失败'), { status: 500, method: 'POST', url: '' })
    options.onError(uploadError)
    ElMessage.error(uploadError.message)
  } finally { uploading.value = false }
}
async function uploadLyricFile(options) {
  const file = options.file
  if (!file.name.toLowerCase().endsWith('.lrc')) {
    const error = new Error('请选择 .lrc 格式的歌词文件')
    options.onError(error)
    ElMessage.error(error.message)
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    const error = new Error('歌词文件不能超过 2MB')
    options.onError(error)
    ElMessage.error(error.message)
    return
  }
  uploadingLyrics.value = true
  lyricProgress.value = 0
  try {
    const result = await uploadLyrics(file, (progress) => { lyricProgress.value = progress })
    if (result.code !== 200) throw new Error(result.message)
    form.lyricUrl = result.data.url
    lyricProgress.value = 100
    ElMessage.success('歌词上传完成')
    options.onSuccess(result.data)
  } catch (error) {
    const uploadError = Object.assign(new Error(error instanceof Error ? error.message : '歌词上传失败'), { status: 500, method: 'POST', url: '' })
    options.onError(uploadError)
    ElMessage.error(uploadError.message)
  } finally { uploadingLyrics.value = false }
}
async function save() {
  if (!form.title.trim() || !form.singerId || !form.audioUrl) { ElMessage.warning('请填写歌曲名称、歌手并上传音频'); return }
  saving.value = true
  try {
    const payload = { title: form.title, singerId: form.singerId, coverUrl: form.coverUrl, audioUrl: form.audioUrl, lyricUrl: form.lyricUrl, durationSeconds: form.durationSeconds }
    const result = editingId.value === null ? await createSong(payload) : await updateSong(editingId.value, payload)
    if (result.code !== 200) throw new Error(result.message)
    ElMessage.success(editingId.value === null ? '歌曲已创建' : '歌曲资料已保存')
    dialogOpen.value = false
    await load()
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '保存失败') } finally { saving.value = false }
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`删除歌曲“${row.title}”后，相关歌单、收藏和播放历史关联也会清理。`, '确认删除', { type: 'warning', confirmButtonText: '删除歌曲', cancelButtonText: '取消' })
    const result = await deleteSong(row.id)
    if (result.code !== 200) throw new Error(result.message)
    ElMessage.success('歌曲已删除')
    await load()
  } catch (error) { if (error === 'cancel' || error === 'close') return; ElMessage.error(error instanceof Error ? error.message : '删除失败') }
}
onMounted(() => { void Promise.all([load(), loadSingers()]) })
</script>

<template>
  <section class="admin-page"><div class="admin-page-heading"><div><p class="eyebrow">TRACK LIBRARY</p><h1>歌曲管理</h1><p>维护歌曲信息、歌手归属、音频与实时歌词。</p></div><el-button type="primary" @click="openCreate">新增歌曲</el-button></div>
    <el-card shadow="never" class="admin-card"><div class="admin-toolbar"><el-input v-model="keyword" clearable placeholder="搜索歌曲名称" class="admin-search" @keyup.enter="search" @clear="search" /><el-button type="primary" @click="search">查询</el-button></div>
      <el-table v-loading="loading" :data="rows" row-key="id" empty-text="暂无歌曲数据" class="admin-table"><el-table-column prop="title" label="歌曲名称" min-width="220" /><el-table-column prop="singerName" label="歌手" min-width="160" /><el-table-column label="音频" min-width="140"><template #default="{ row }"><el-link :href="`http://127.0.0.1:8081${row.audioUrl}`" target="_blank" type="primary">试听文件</el-link></template></el-table-column><el-table-column label="歌词" min-width="130"><template #default="{ row }"><el-link v-if="row.lyricUrl" :href="`http://127.0.0.1:8081${row.lyricUrl}`" target="_blank" type="primary">查看 LRC</el-link><span v-else class="table-muted">未上传</span></template></el-table-column><el-table-column label="操作" width="150" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table>
      <div class="admin-pagination"><el-pagination v-model:current-page="page" v-model:page-size="size" background layout="total, sizes, prev, pager, next" :total="total" :page-sizes="[10, 20, 50]" @current-change="load" @size-change="() => { page = 1; load() }" /></div>
    </el-card>
    <el-dialog v-model="dialogOpen" :title="editingId === null ? '新增歌曲' : '编辑歌曲'" width="560px"><el-form label-position="top" novalidate><el-form-item label="歌曲名称" required><el-input v-model="form.title" maxlength="150" show-word-limit /></el-form-item><el-form-item label="歌手" required><el-select v-model="form.singerId" placeholder="请选择歌手" filterable style="width:100%"><el-option v-for="singer in singers" :key="singer.id" :label="singer.name" :value="singer.id" /></el-select></el-form-item><el-form-item label="歌曲封面"><AdminImageUpload v-model="form.coverUrl" label="歌曲封面" /><el-input v-model="form.coverUrl" maxlength="255" placeholder="也可填写图片地址" /></el-form-item><el-form-item label="音频文件" required><el-upload :http-request="uploadAudioFile" :show-file-list="false" accept="audio/*,.mp3,.wav,.ogg,.m4a,.aac"><el-button :loading="uploading">{{ form.audioUrl ? '重新上传音频' : '选择音频文件' }}</el-button></el-upload><small v-if="form.audioUrl" class="upload-hint">已上传：{{ form.audioUrl }}</small></el-form-item><el-form-item label="实时歌词（LRC）"><div class="lyric-upload-field"><el-upload :http-request="uploadLyricFile" :show-file-list="false" accept=".lrc,text/plain"><el-button :loading="uploadingLyrics">{{ form.lyricUrl ? '重新上传歌词' : '选择 LRC 文件' }}</el-button></el-upload><el-button v-if="form.lyricUrl" link type="danger" :disabled="uploadingLyrics" @click="form.lyricUrl = ''; lyricProgress = 0">移除歌词</el-button></div><el-progress v-if="uploadingLyrics" :percentage="lyricProgress" :stroke-width="5" /><small class="upload-hint">LRC 文件需包含 [分钟:秒] 时间标签，最大 2MB。</small><small v-if="form.lyricUrl" class="upload-hint">已上传：{{ form.lyricUrl }}</small></el-form-item><el-form-item label="时长（秒）"><el-input-number v-model="form.durationSeconds" :min="1" :max="86400" /></el-form-item></el-form><template #footer><el-button @click="dialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template></el-dialog>
  </section>
</template>
