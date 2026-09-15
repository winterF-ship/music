<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createSinger, deleteSinger, fetchSingers, updateSinger } from '../api/admin'
import AdminImageUpload from '../components/AdminImageUpload.vue'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const rows = ref([])
const dialogOpen = ref(false)
const editingId = ref(null)
const form = reactive({ name: '', avatarUrl: '', introduction: '' })

async function load() {
  loading.value = true
  try {
    const result = await fetchSingers({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    if (result.code !== 200) throw new Error(result.message)
    rows.value = result.data.records
    total.value = result.data.total
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '歌手列表加载失败') } finally { loading.value = false }
}

function search() { page.value = 1; void load() }
function openCreate() { editingId.value = null; Object.assign(form, { name: '', avatarUrl: '', introduction: '' }); dialogOpen.value = true }
function openEdit(row) { editingId.value = row.id; Object.assign(form, { name: row.name, avatarUrl: row.avatarUrl ?? '', introduction: row.introduction ?? '' }); dialogOpen.value = true }
async function save() {
  if (!form.name.trim()) { ElMessage.warning('歌手姓名不能为空'); return }
  saving.value = true
  try {
    const result = editingId.value === null ? await createSinger(form) : await updateSinger(editingId.value, form)
    if (result.code !== 200) throw new Error(result.message)
    ElMessage.success(editingId.value === null ? '歌手已创建' : '歌手资料已保存')
    dialogOpen.value = false
    await load()
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '保存失败') } finally { saving.value = false }
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`删除歌手“${row.name}”前会检查关联歌曲。`, '确认删除', { type: 'warning', confirmButtonText: '删除歌手', cancelButtonText: '取消' })
    const result = await deleteSinger(row.id)
    if (result.code !== 200) throw new Error(result.message)
    ElMessage.success('歌手已删除')
    await load()
  } catch (error) { if (error === 'cancel' || error === 'close') return; ElMessage.error(error instanceof Error ? error.message : '删除失败') }
}
onMounted(() => { void load() })
</script>

<template>
  <section class="admin-page"><div class="admin-page-heading"><div><p class="eyebrow">ARTIST DIRECTORY</p><h1>歌手管理</h1><p>维护歌手姓名、头像和简介；有关联歌曲的歌手不能直接删除。</p></div><el-button type="primary" @click="openCreate">新增歌手</el-button></div>
    <el-card shadow="never" class="admin-card"><div class="admin-toolbar"><el-input v-model="keyword" clearable placeholder="搜索歌手姓名" class="admin-search" @keyup.enter="search" @clear="search" /><el-button type="primary" @click="search">查询</el-button></div>
      <el-table v-loading="loading" :data="rows" row-key="id" empty-text="暂无歌手数据" class="admin-table"><el-table-column prop="name" label="姓名" min-width="180" /><el-table-column prop="introduction" label="简介" min-width="300" show-overflow-tooltip /><el-table-column label="操作" width="150" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table>
      <div class="admin-pagination"><el-pagination v-model:current-page="page" v-model:page-size="size" background layout="total, sizes, prev, pager, next" :total="total" :page-sizes="[10, 20, 50]" @current-change="load" @size-change="() => { page = 1; load() }" /></div>
    </el-card>
    <el-dialog v-model="dialogOpen" :title="editingId === null ? '新增歌手' : '编辑歌手'" width="520px"><el-form label-position="top"><el-form-item label="姓名" required><el-input v-model="form.name" maxlength="100" show-word-limit /></el-form-item><el-form-item label="歌手头像"><AdminImageUpload v-model="form.avatarUrl" label="歌手头像" round /><el-input v-model="form.avatarUrl" maxlength="255" placeholder="也可填写图片地址" /></el-form-item><el-form-item label="简介"><el-input v-model="form.introduction" type="textarea" :rows="5" maxlength="1000" show-word-limit /></el-form-item></el-form><template #footer><el-button @click="dialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template></el-dialog>
  </section>
</template>
