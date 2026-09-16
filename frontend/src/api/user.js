import { api } from './http'

async function unwrap(request) {
  const { data } = await request
  if (data.code !== 200) throw new Error(data.message || '请求失败')
  return data.data
}

export const loginUser = (payload) => unwrap(api.post('/auth/login', payload))
export const registerUser = (payload) => unwrap(api.post('/auth/register', payload))
export const fetchProfile = () => unwrap(api.get('/user/profile'))
export const updateProfile = (payload) => unwrap(api.put('/user/profile', payload))

export function uploadAvatar(file, onProgress) {
  const form = new FormData()
  form.append('avatar', file)
  return unwrap(api.post('/user/profile/avatar', form, {
    onUploadProgress: (event) => {
      if (event.total && onProgress) onProgress(Math.round(event.loaded / event.total * 100))
    },
  }))
}

export function uploadPlaylistCover(file, onProgress, signal) {
  const form = new FormData()
  form.append('cover', file)
  return unwrap(api.post('/user/playlists/upload/cover', form, {
    signal,
    onUploadProgress: (event) => {
      if (event.total && onProgress) onProgress(Math.round(event.loaded / event.total * 100))
    },
  }))
}

export const fetchFavoriteSongs = () => unwrap(api.get('/user/favorites/songs'))
export const addFavoriteSong = (songId) => unwrap(api.post(`/user/favorites/songs/${songId}`))
export const removeFavoriteSong = (songId) => unwrap(api.delete(`/user/favorites/songs/${songId}`))
export const fetchFavoritePlaylists = () => unwrap(api.get('/user/favorites/playlists'))
export const addFavoritePlaylist = (playlistId) => unwrap(api.post(`/user/favorites/playlists/${playlistId}`))
export const removeFavoritePlaylist = (playlistId) => unwrap(api.delete(`/user/favorites/playlists/${playlistId}`))
export const fetchUserPlaylists = () => unwrap(api.get('/user/playlists'))
export const createUserPlaylist = (payload) => unwrap(api.post('/user/playlists', payload))
export const updateUserPlaylist = (playlistId, payload) => unwrap(api.put(`/user/playlists/${playlistId}`, payload))
export const addSongToUserPlaylist = (playlistId, songId, sortNo = 0) => unwrap(api.post(`/user/playlists/${playlistId}/songs`, { songId, sortNo }))
