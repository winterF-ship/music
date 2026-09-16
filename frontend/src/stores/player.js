import { computed, ref, watch } from 'vue'
import { defineStore } from 'pinia'

function readVolume() {
  try {
    const value = Number(localStorage.getItem('musicdemo1.player.volume'))
    return Number.isFinite(value) && value >= 0 && value <= 1 ? value : 0.75
  } catch { return 0.75 }
}

export const usePlayerStore = defineStore('player', () => {
  const queue = ref([])
  const currentIndex = ref(-1)
  const isPlaying = ref(false)
  const currentTime = ref(0)
  const duration = ref(0)
  const seekVersion = ref(0)
  const volume = ref(readVolume())
  const shuffleEnabled = ref(false)
  const repeatEnabled = ref(false)
  const currentSong = computed(() => queue.value[currentIndex.value])

  function playSong(song, source) {
    if (source?.length) queue.value = [...source]
    let index = queue.value.findIndex((item) => item.id === song.id)
    if (index < 0) { queue.value.push(song); index = queue.value.length - 1 }
    currentIndex.value = index
    currentTime.value = 0
    isPlaying.value = true
  }
  function playAll(songs) { if (songs.length) { queue.value = [...songs]; currentIndex.value = 0; currentTime.value = 0; isPlaying.value = true } }
  function playAt(index) { if (queue.value[index]) { currentIndex.value = index; currentTime.value = 0; isPlaying.value = true } }
  function toggle() { if (currentSong.value) isPlaying.value = !isPlaying.value }
  function randomIndex() {
    if (queue.value.length < 2) return currentIndex.value
    let index = currentIndex.value
    while (index === currentIndex.value) index = Math.floor(Math.random() * queue.value.length)
    return index
  }
  function previous() { if (queue.value.length) { currentIndex.value = shuffleEnabled.value ? randomIndex() : (currentIndex.value - 1 + queue.value.length) % queue.value.length; currentTime.value = 0; isPlaying.value = true } }
  function next() { if (queue.value.length) { currentIndex.value = shuffleEnabled.value ? randomIndex() : (currentIndex.value + 1) % queue.value.length; currentTime.value = 0; isPlaying.value = true } }
  function handleEnded() {
    if (!queue.value.length) return
    if (!shuffleEnabled.value && !repeatEnabled.value && currentIndex.value === queue.value.length - 1) {
      isPlaying.value = false
      currentTime.value = duration.value
      return
    }
    next()
  }
  function toggleShuffle() { shuffleEnabled.value = !shuffleEnabled.value }
  function toggleRepeat() { repeatEnabled.value = !repeatEnabled.value }
  function removeFromQueue(index) {
    if (!queue.value[index]) return
    const removingCurrent = index === currentIndex.value
    queue.value.splice(index, 1)
    if (!queue.value.length) { currentIndex.value = -1; currentTime.value = 0; duration.value = 0; isPlaying.value = false; return }
    if (index < currentIndex.value) currentIndex.value -= 1
    else if (removingCurrent) { currentIndex.value = Math.min(index, queue.value.length - 1); currentTime.value = 0 }
  }
  function clearQueue() { queue.value = []; currentIndex.value = -1; currentTime.value = 0; duration.value = 0; isPlaying.value = false }
  function setVolume(value) { volume.value = Math.min(1, Math.max(0, Number(value) || 0)) }
  function seek(value) {
    currentTime.value = Math.min(duration.value || Number(value) || 0, Math.max(0, Number(value) || 0))
    seekVersion.value += 1
  }

  watch(volume, (value) => {
    try { localStorage.setItem('musicdemo1.player.volume', String(value)) } catch { /* storage can be unavailable */ }
  })

  return { queue, currentIndex, currentSong, isPlaying, currentTime, duration, volume, seekVersion, shuffleEnabled, repeatEnabled, playSong, playAll, playAt, toggle, previous, next, handleEnded, toggleShuffle, toggleRepeat, removeFromQueue, clearQueue, setVolume, seek }
})
