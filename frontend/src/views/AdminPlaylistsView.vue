<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addPlaylistSong, createPlaylist, deletePlaylist, fetchPlaylist, fetchPlaylists, fetchSongs, removePlaylistSong, updatePlaylist } from '../api/admin'
import AdminImageUpload from '../components/AdminImageUpload.vue'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const rows = ref([])
const songs = ref([])
const dialogOpen = ref(false)
const songsDialogOpen = ref(false)
const editingId = ref(null)
const selectedPlaylist = ref(null)
const playlistSongs = ref([])
const selectedSongId = ref()
const form = reactive({ name: '', coverUrl: '', description: '' })

async function load() {
  loading.value = true
  try { const result = await fetchPlaylists({ page: page.value, size: size.value, keyword: keyword.value || undefined }); if (result.code !== 200) throw new Error(result.message); rows.value = result.data.records; total.value = result.data.total } catch (error) { ElMessage.error(error instanceof Error ? error.message : '歌单列表加载失败') } finally { loading.value = false }
}
async function loadSongs() { const result = await fetchSongs({ page: 1, size: 100 }); if (result.code === 200) songs.value = result.data.records }
function search() { page.value = 1; void load() }
function openCreate() { editingId.value = null; Object.assign(form, { name: '', coverUrl: '', description: '' }); dialogOpen.value = true }
function openEdit(row) { editingId.value = row.id; Object.assign(form, { name: row.name, coverUrl: row.coverUrl ?? '', description: row.description ?? '' }); dialogOpen.value = true }
async function save() { if (!form.name.trim()) { ElMessage.warning('歌单名称不能为空'); return }; saving.value = true; try { const payload = { name: form.name, coverUrl: form.coverUrl, description: form.description }; const result = editingId.value === null ? await createPlaylist(payload) : await updatePlaylist(editingId.value, payload); if (result.code !== 200) throw new Error(result.message); ElMessage.success(editingId.value === null ? '歌单已创建' : '歌单资料已保存'); dialogOpen.value = false; await load() } catch (error) { ElMessage.error(error instanceof Error ? error.message : '保存失败') } finally { saving.value = false } }
async function remove(row) { try { await ElMessageBox.confirm(`删除歌单“${row.name}”只会移除歌单和歌曲关联，不会删除歌曲。`, '确认删除', { type: 'warning', confirmButtonText: '删除歌单', cancelButtonText: '取消' }); const result = await deletePlaylist(row.id); if (result.code !== 200) throw new Error(result.message); ElMessage.success('歌单已删除'); await load() } catch (error) { if (error === 'cancel' || error === 'close') return; ElMessage.error(error instanceof Error ? error.message : '删除失败') } }
async function openSongs(row) { selectedPlaylist.value = row; selectedSongId.value = undefined; const result = await fetchPlaylist(row.id); if (result.code !== 200) { ElMessage.error(result.message); return }; playlistSongs.value = result.data.songs; songsDialogOpen.value = true }
async function addSong() { if (!selectedPlaylist.value || !selectedSongId.value) { ElMessage.warning('请选择歌曲'); return }; try { const result = await addPlaylistSong(selectedPlaylist.value.id, selectedSongId.value); if (result.code !== 200) throw new Error(result.message); playlistSongs.value = result.data.songs; selectedSongId.value = undefined; ElMessage.success('歌曲已加入歌单') } catch (error) { ElMessage.error(error instanceof Error ? error.message : '加入失败') } }
async function removeSong(song) { if (!selectedPlaylist.value) return; try { const result = await removePlaylistSong(selectedPlaylist.value.id, song.id); if (result.code !== 200) throw new Error(result.message); playlistSongs.value = result.data.songs; ElMessage.success('歌曲已移出歌单') } catch (error) { ElMessage.error(error instanceof Error ? error.message : '移除失败') } }
onMounted(() => { void Promise.all([load(), loadSongs()]) })
</script>

<template>
  <section class="admin-page"><div class="admin-page-heading"><div><p class="eyebrow">PLAYLIST STUDIO</p><h1>歌单管理</h1><p>维护歌单资料，并将已有歌曲加入或移出歌单。</p></div><el-button type="primary" @click="openCreate">新增歌单</el-button></div>
    <el-card shadow="never" class="admin-card"><div class="admin-toolbar"><el-input v-model="keyword" clearable placeholder="搜索歌单名称" class="admin-search" @keyup.enter="search" @clear="search" /><el-button type="primary" @click="search">查询</el-button></div><el-table v-loading="loading" :data="rows" row-key="id" empty-text="暂无歌单数据" class="admin-table"><el-table-column prop="name" label="歌单名称" min-width="220" /><el-table-column prop="description" label="简介" min-width="320" show-overflow-tooltip /><el-table-column label="操作" width="220" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openSongs(row)">歌曲管理</el-button><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table><div class="admin-pagination"><el-pagination v-model:current-page="page" v-model:page-size="size" background layout="total, sizes, prev, pager, next" :total="total" :page-sizes="[10, 20, 50]" @current-change="load" @size-change="() => { page = 1; load() }" /></div></el-card>
    <el-dialog v-model="dialogOpen" :title="editingId === null ? '新增歌单' : '编辑歌单'" width="520px"><el-form label-position="top"><el-form-item label="名称" required><el-input v-model="form.name" maxlength="150" show-word-limit /></el-form-item><el-form-item label="歌单封面"><AdminImageUpload v-model="form.coverUrl" label="歌单封面" /><el-input v-model="form.coverUrl" maxlength="255" placeholder="也可填写图片地址" /></el-form-item><el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="5" maxlength="500" show-word-limit /></el-form-item></el-form><template #footer><el-button @click="dialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></template></el-dialog>
    <el-dialog v-model="songsDialogOpen" :title="`${selectedPlaylist?.name ?? ''} · 歌曲管理`" width="640px"><div class="playlist-add-row"><el-select v-model="selectedSongId" filterable clearable placeholder="选择歌曲" style="flex:1"><el-option v-for="song in songs" :key="song.id" :label="`${song.title} · ${song.singerName ?? '未知歌手'}`" :value="song.id" /></el-select><el-button type="primary" @click="addSong">加入歌单</el-button></div><el-table :data="playlistSongs" empty-text="歌单中还没有歌曲" row-key="id"><el-table-column prop="title" label="歌曲" /><el-table-column prop="singerName" label="歌手" /><el-table-column label="操作" width="90"><template #default="{ row }"><el-button link type="danger" @click="removeSong(row)">移出</el-button></template></el-table-column></el-table></el-dialog>
  </section>
</template>
