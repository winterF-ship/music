<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteUser, fetchUsers, updateUser, updateUserStatus } from '../api/admin'
import AdminImageUpload from '../components/AdminImageUpload.vue'

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const rows = ref([])
const dialogOpen = ref(false)
const editing = reactive({ id: 0, username: '', nickname: '', avatar: '', profile: '', status: 1 })

async function loadUsers() {
  loading.value = true
  try {
    const result = await fetchUsers({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    if (result.code !== 200) throw new Error(result.message)
    rows.value = result.data.records
    total.value = result.data.total
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '用户列表加载失败')
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  void loadUsers()
}

function openEdit(row) {
  Object.assign(editing, row)
  dialogOpen.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    const result = await updateUser(editing.id, { nickname: editing.nickname, avatar: editing.avatar, profile: editing.profile, status: editing.status })
    if (result.code !== 200) throw new Error(result.message)
    ElMessage.success('用户资料已保存')
    dialogOpen.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  try {
    const result = await updateUserStatus(row.id, next)
    if (result.code !== 200) throw new Error(result.message)
    row.status = next
    ElMessage.success(next === 1 ? '账号已启用' : '账号已禁用')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败')
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`删除用户“${row.username}”后，其收藏和播放历史也会清理，是否继续？`, '确认删除', { type: 'warning', confirmButtonText: '删除用户', cancelButtonText: '取消' })
    const result = await deleteUser(row.id)
    if (result.code !== 200) throw new Error(result.message)
    ElMessage.success('用户已删除')
    if (rows.value.length === 1 && page.value > 1) page.value -= 1
    await loadUsers()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error instanceof Error ? error.message : '删除失败')
  }
}

onMounted(() => { void loadUsers() })
</script>

<template>
  <section class="admin-page">
    <div class="admin-page-heading"><div><p class="eyebrow">ACCOUNT DIRECTORY</p><h1>用户管理</h1><p>查看注册用户、调整账号状态并维护公开资料。</p></div><el-tag type="info">共 {{ total }} 位用户</el-tag></div>
    <el-card shadow="never" class="admin-card">
      <div class="admin-toolbar"><el-input v-model="keyword" clearable placeholder="搜索账号或昵称" class="admin-search" @keyup.enter="search" @clear="search" /><el-button type="primary" @click="search">查询</el-button></div>
      <el-table v-loading="loading" :data="rows" row-key="id" empty-text="暂无用户数据" class="admin-table">
        <el-table-column prop="username" label="账号" min-width="160" />
        <el-table-column prop="nickname" label="昵称" min-width="160" />
        <el-table-column label="状态" width="120"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="240" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
      </el-table>
      <div class="admin-pagination"><el-pagination v-model:current-page="page" v-model:page-size="size" background layout="total, sizes, prev, pager, next" :total="total" :page-sizes="[10, 20, 50]" @current-change="loadUsers" @size-change="() => { page = 1; loadUsers() }" /></div>
    </el-card>
    <el-dialog v-model="dialogOpen" title="编辑用户资料" width="520px">
      <el-form label-position="top"><el-form-item label="账号"><el-input :model-value="editing.username" disabled /></el-form-item><el-form-item label="昵称"><el-input v-model="editing.nickname" maxlength="50" show-word-limit /></el-form-item><el-form-item label="用户头像"><AdminImageUpload v-model="editing.avatar" label="用户头像" round /><el-input v-model="editing.avatar" maxlength="255" placeholder="也可填写图片地址" /></el-form-item><el-form-item label="个人简介"><el-input v-model="editing.profile" type="textarea" :rows="4" maxlength="500" show-word-limit /></el-form-item><el-form-item label="账号状态"><el-switch v-model="editing.status" :active-value="1" :inactive-value="0" inline-prompt active-text="启用" inactive-text="禁用" /></el-form-item></el-form>
      <template #footer><el-button @click="dialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveEdit">保存修改</el-button></template>
    </el-dialog>
  </section>
</template>
