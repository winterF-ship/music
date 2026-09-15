import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { addFavoritePlaylist, addFavoriteSong, fetchFavoritePlaylists, fetchFavoriteSongs, removeFavoritePlaylist, removeFavoriteSong } from '../api/user'
import { useAuthStore } from './auth'

export const useFavoriteStore = defineStore('favorites', () => {
  const auth = useAuthStore()
  const songs = ref([])
  const playlists = ref([])
  const loading = ref(false)
  const loadedUserId = ref(null)
  const busyKeys = ref([])
  const songIds = computed(() => new Set(songs.value.map((item) => item.id)))
  const playlistIds = computed(() => new Set(playlists.value.map((item) => item.id)))

  function isBusy(kind, id) { return busyKeys.value.includes(`${kind}:${id}`) }
  function setBusy(kind, id, value) {
    const key = `${kind}:${id}`
    busyKeys.value = value ? [...busyKeys.value, key] : busyKeys.value.filter((item) => item !== key)
  }

  async function load(force = false) {
    const userId = auth.session?.id
    if (!userId) { reset(); return }
    if (!force && loadedUserId.value === userId) return
    loading.value = true
    try {
      const [songItems, playlistItems] = await Promise.all([fetchFavoriteSongs(), fetchFavoritePlaylists()])
      songs.value = songItems
      playlists.value = playlistItems
      loadedUserId.value = userId
    } finally { loading.value = false }
  }

  async function toggleSong(song) {
    if (isBusy('song', song.id)) return
    setBusy('song', song.id, true)
    try {
      if (songIds.value.has(song.id)) {
        await removeFavoriteSong(song.id)
        songs.value = songs.value.filter((item) => item.id !== song.id)
        return false
      }
      await addFavoriteSong(song.id)
      songs.value = [song, ...songs.value]
      return true
    } finally { setBusy('song', song.id, false) }
  }

  async function togglePlaylist(playlist) {
    if (isBusy('playlist', playlist.id)) return
    setBusy('playlist', playlist.id, true)
    try {
      if (playlistIds.value.has(playlist.id)) {
        await removeFavoritePlaylist(playlist.id)
        playlists.value = playlists.value.filter((item) => item.id !== playlist.id)
        return false
      }
      await addFavoritePlaylist(playlist.id)
      playlists.value = [playlist, ...playlists.value]
      return true
    } finally { setBusy('playlist', playlist.id, false) }
  }

  function reset() {
    songs.value = []
    playlists.value = []
    loadedUserId.value = null
    busyKeys.value = []
  }

  return { songs, playlists, loading, songIds, playlistIds, isBusy, load, toggleSong, togglePlaylist, reset }
})
