import { describe, expect, it } from 'vitest'
import { beginPlaylistPlaybackRequest, isLatestPlaylistPlaybackRequest, shufflePlaylistSongs } from '../src/utils/playlistPlayback'

describe('歌单随机播放', () => {
  it('随机排列歌曲且不修改原数组', () => {
    const songs = [{ id: 1 }, { id: 2 }, { id: 3 }]
    const shuffled = shufflePlaylistSongs(songs, () => 0)

    expect(shuffled.map((song) => song.id)).toEqual([2, 3, 1])
    expect(songs.map((song) => song.id)).toEqual([1, 2, 3])
  })

  it('只允许最后一次点击完成播放', () => {
    const first = beginPlaylistPlaybackRequest()
    const second = beginPlaylistPlaybackRequest()

    expect(isLatestPlaylistPlaybackRequest(first)).toBe(false)
    expect(isLatestPlaylistPlaybackRequest(second)).toBe(true)
  })
})
