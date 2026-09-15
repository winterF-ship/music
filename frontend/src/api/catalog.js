import { api } from './http'

const backendOrigin = (import.meta.env.VITE_API_BASE_URL ?? 'http://127.0.0.1:8081/api').replace(/\/api\/?$/, '')

export function assetUrl(value) {
  if (!value) return ''
  if (/^(https?:|data:|blob:)/i.test(value)) return value
  return `${backendOrigin}${value.startsWith('/') ? '' : '/'}${value}`
}

async function unwrap(request) {
  const { data } = await request
  if (data.code !== 200) throw new Error(data.message || '请求失败')
  return data.data
}

export const fetchBanners = () => unwrap(api.get('/banners'))
export const fetchPlaylists = (page = 1, size = 12) => unwrap(api.get('/playlists', { params: { page, size } }))
export const fetchPlaylistDetail = (id) => unwrap(api.get(`/playlists/${id}`))
export const fetchSingers = (page = 1, size = 12) => unwrap(api.get('/singers', { params: { page, size } }))
export const fetchSingerDetail = (id) => unwrap(api.get(`/singers/${id}`))
export const fetchSongs = (page = 1, size = 20, keyword = '', singerId) => unwrap(api.get('/songs', { params: { page, size, keyword: keyword || undefined, singerId } }))
