import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { usePlayerStore } from '../src/stores/player'

const tracks = [
  { id: 1, title: '第一首', audioUrl: '/1.mp3' },
  { id: 2, title: '第二首', audioUrl: '/2.mp3' },
  { id: 3, title: '第三首', audioUrl: '/3.mp3' },
]

describe('播放器队列', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('从列表播放歌曲时建立队列并定位当前歌曲', () => {
    const player = usePlayerStore()
    player.playSong(tracks[1], tracks)
    expect(player.queue).toEqual(tracks)
    expect(player.currentIndex).toBe(1)
    expect(player.currentSong.id).toBe(2)
    expect(player.isPlaying).toBe(true)
  })

  it('上一首和下一首在队列首尾循环', () => {
    const player = usePlayerStore()
    player.playAll(tracks)
    player.previous()
    expect(player.currentSong.id).toBe(3)
    player.next()
    expect(player.currentSong.id).toBe(1)
  })

  it('队列自然播放结束时按循环模式决定是否回到开头', () => {
    const player = usePlayerStore()
    player.playAll(tracks)
    player.playAt(2)
    player.duration = 180
    player.handleEnded()
    expect(player.currentSong.id).toBe(3)
    expect(player.isPlaying).toBe(false)

    player.toggleRepeat()
    player.handleEnded()
    expect(player.currentSong.id).toBe(1)
    expect(player.isPlaying).toBe(true)
  })

  it('随机和循环模式可独立切换', () => {
    const player = usePlayerStore()
    expect(player.shuffleEnabled).toBe(false)
    expect(player.repeatEnabled).toBe(false)
    player.toggleShuffle()
    player.toggleRepeat()
    expect(player.shuffleEnabled).toBe(true)
    expect(player.repeatEnabled).toBe(true)
  })

  it('移除当前歌曲后继续定位到剩余歌曲', () => {
    const player = usePlayerStore()
    player.playAll(tracks)
    player.playAt(1)
    player.removeFromQueue(1)
    expect(player.queue.map((item) => item.id)).toEqual([1, 3])
    expect(player.currentSong.id).toBe(3)
    expect(player.currentIndex).toBe(1)
  })

  it('移除当前歌曲之前的项目时保持当前歌曲不变', () => {
    const player = usePlayerStore()
    player.playAll(tracks)
    player.playAt(2)
    player.removeFromQueue(0)
    expect(player.currentSong.id).toBe(3)
    expect(player.currentIndex).toBe(1)
  })

  it('清空队列时重置播放状态', () => {
    const player = usePlayerStore()
    player.playAll(tracks)
    player.currentTime = 28
    player.duration = 180
    player.clearQueue()
    expect(player.queue).toEqual([])
    expect(player.currentSong).toBeUndefined()
    expect(player.currentIndex).toBe(-1)
    expect(player.isPlaying).toBe(false)
    expect(player.currentTime).toBe(0)
    expect(player.duration).toBe(0)
  })

  it('音量始终限制在 0 到 1', () => {
    const player = usePlayerStore()
    player.setVolume(1.4)
    expect(player.volume).toBe(1)
    player.setVolume(-0.2)
    expect(player.volume).toBe(0)
  })

  it('全屏歌词跳转时限制进度范围并发出定位请求', () => {
    const player = usePlayerStore()
    player.duration = 180
    player.seek(48.5)
    expect(player.currentTime).toBe(48.5)
    expect(player.seekVersion).toBe(1)
    player.seek(999)
    expect(player.currentTime).toBe(180)
  })
})
