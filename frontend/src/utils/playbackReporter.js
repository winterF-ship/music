import { api } from '../api/http'

// One ID per playback attempt; retries and playing events after buffering share it.
export function createPlaybackReporter(send = (id, eventId) => api.post(`/songs/${id}/plays`, { eventId })) {
  let session = null
  return {
    start(songId) { session = songId ? { songId, eventId: crypto.randomUUID(), sent: false, pending: false } : null },
    async playing() {
      const current = session
      if (!current || current.sent || current.pending) return
      current.pending = true
      try {
        const result = await send(current.songId, current.eventId)
        if (result?.data?.code !== undefined && result.data.code !== 200) throw new Error('播放记录保存失败')
        current.sent = true
      } catch { /* Retry on the next playback time update without interrupting audio. */ }
      finally { current.pending = false }
    },
  }
}
