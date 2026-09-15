import { api } from './http'

export async function adminLogin(username, password) {
  const { data } = await api.post('/admin/auth/login', { username, password })
  return data
}

export async function fetchUsers(params) {
  const { data } = await api.get('/admin/users', { params })
  return data
}

export async function updateUser(id, payload) {
  const { data } = await api.put(`/admin/users/${id}`, payload)
  return data
}

export async function updateUserStatus(id, status) {
  const { data } = await api.patch(`/admin/users/${id}/status`, { status })
  return data
}

export async function deleteUser(id) {
  const { data } = await api.delete(`/admin/users/${id}`)
  return data
}

export async function fetchSingers(params) {
  const { data } = await api.get('/admin/singers', { params })
  return data
}

export async function createSinger(payload) {
  const { data } = await api.post('/admin/singers', payload)
  return data
}

export async function updateSinger(id, payload) {
  const { data } = await api.put(`/admin/singers/${id}`, payload)
  return data
}

export async function deleteSinger(id) {
  const { data } = await api.delete(`/admin/singers/${id}`)
  return data
}

export async function fetchSongs(params) {
  const { data } = await api.get('/admin/songs', { params })
  return data
}

export async function createSong(payload) {
  const { data } = await api.post('/admin/songs', payload)
  return data
}

export async function updateSong(id, payload) {
  const { data } = await api.put(`/admin/songs/${id}`, payload)
  return data
}

export async function deleteSong(id) {
  const { data } = await api.delete(`/admin/songs/${id}`)
  return data
}

export async function uploadAudio(file) {
  const form = new FormData()
  form.append('audio', file)
  const { data } = await api.post('/admin/songs/upload/audio', form)
  return data
}

export async function uploadLyrics(file, onProgress) {
  const form = new FormData()
  form.append('lyrics', file)
  const { data } = await api.post('/admin/songs/upload/lyrics', form, {
    onUploadProgress: (event) => {
      if (event.total && onProgress) onProgress(Math.round((event.loaded / event.total) * 100))
    },
  })
  return data
}

export async function uploadImage(file, onProgress) {
  const form = new FormData()
  form.append('image', file)
  const { data } = await api.post('/admin/uploads/image', form, {
    onUploadProgress: (event) => {
      if (event.total && onProgress) onProgress(Math.round((event.loaded / event.total) * 100))
    },
  })
  return data
}

export async function fetchPlaylists(params) {
  const { data } = await api.get('/admin/playlists', { params })
  return data
}

export async function createPlaylist(payload) {
  const { data } = await api.post('/admin/playlists', payload)
  return data
}

export async function updatePlaylist(id, payload) {
  const { data } = await api.put(`/admin/playlists/${id}`, payload)
  return data
}

export async function deletePlaylist(id) {
  const { data } = await api.delete(`/admin/playlists/${id}`)
  return data
}

export async function fetchPlaylist(id) {
  const { data } = await api.get(`/admin/playlists/${id}`)
  return data
}

export async function addPlaylistSong(id, songId, sortNo = 0) {
  const { data } = await api.post(`/admin/playlists/${id}/songs`, { songId, sortNo })
  return data
}

export async function removePlaylistSong(id, songId) {
  const { data } = await api.delete(`/admin/playlists/${id}/songs/${songId}`)
  return data
}

export async function fetchBanners(params) {
  const { data } = await api.get('/admin/banners', { params })
  return data
}

export async function createBanner(payload) {
  const { data } = await api.post('/admin/banners', payload)
  return data
}

export async function updateBanner(id, payload) {
  const { data } = await api.put(`/admin/banners/${id}`, payload)
  return data
}

export async function deleteBanner(id) {
  const { data } = await api.delete(`/admin/banners/${id}`)
  return data
}

export async function uploadBannerImage(file) {
  const form = new FormData()
  // ImageCropper emits a Blob, which browsers otherwise serialize as a file named "blob".
  // The storage API validates the extension, so keep a real image filename on the part.
  form.append('image', file, file?.name || 'banner.jpg')
  const { data } = await api.post('/admin/banners/upload/image', form)
  return data
}

export async function fetchAnalyticsSummary() {
  const { data } = await api.get('/admin/analytics/summary')
  return data
}

export async function fetchSongsBySinger() {
  const { data } = await api.get('/admin/analytics/songs-by-singer')
  return data
}
