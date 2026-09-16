import { describe, expect, it } from 'vitest'
import { PLAYLIST_COVER_GRADIENTS, playlistCoverGradient } from '../src/utils/playlistCover'

describe('歌单占位封面渐变', () => {
  it('从参考配色中稳定选择渐变', () => {
    const first = playlistCoverGradient('playlist-18')

    expect(PLAYLIST_COVER_GRADIENTS).toContain(first)
    expect(playlistCoverGradient('playlist-18')).toBe(first)
  })

  it('不同歌单可以分配到不同配色', () => {
    const gradients = new Set(Array.from({ length: 12 }, (_, index) => playlistCoverGradient(index)))

    expect(gradients.size).toBeGreaterThan(1)
  })
})
