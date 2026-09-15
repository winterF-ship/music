import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { addSongToUserPlaylist, createUserPlaylist, fetchUserPlaylists } from '../api/user'
import { useAuthStore } from './auth'

export const useUserPlaylistStore = defineStore('user-playlists', () => {
  const auth = useAuthStore()
  const items = ref([])
  const loading = ref(false)
  const loadedUserId = ref(null)
  const lastError = ref(null)
  const busyKeys = ref([])

  const hasPlaylists = computed(() => items.value.length > 0)

  function isBusy(playlistId, songId) {
    return busyKeys.value.includes(`${playlistId}:${songId}`)
  }

  function setBusy(playlistId, songId, value) {
    const key = `${playlistId}:${songId}`
    busyKeys.value = value ? [...busyKeys.value, key] : busyKeys.value.filter((item) => item !== key)
  }

  async function load(force = false) {
    const userId = auth.session?.id
    if (!userId) { reset(); return [] }
    if (!force && loadedUserId.value === userId) return items.value
    loading.value = true
    lastError.value = null
    try {
      items.value = await fetchUserPlaylists()
      loadedUserId.value = userId
      return items.value
    } catch (error) {
      lastError.value = error
      throw error
    } finally { loading.value = false }
  }

  async function create(payload) {
    const created = await createUserPlaylist(payload)
    items.value = [created, ...items.value]
    loadedUserId.value = auth.session?.id ?? loadedUserId.value
    return created
  }

  async function addSong(playlistId, songId) {
    if (isBusy(playlistId, songId)) return null
    setBusy(playlistId, songId, true)
    try { return await addSongToUserPlaylist(playlistId, songId) }
    finally { setBusy(playlistId, songId, false) }
  }

  function reset() {
    items.value = []
    loadedUserId.value = null
    lastError.value = null
    busyKeys.value = []
  }

  return { items, loading, lastError, hasPlaylists, load, create, addSong, reset }
})
