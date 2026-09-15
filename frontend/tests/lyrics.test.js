import { describe, expect, it } from 'vitest'
import { activeLyricIndex, parseLrc } from '../src/utils/lyrics'

describe('LRC 歌词', () => {
  it('解析时间、多个时间标签并按时间排序', () => {
    expect(parseLrc('[00:12.50][00:20.5]副歌\n[00:02.00]开场')).toEqual([
      { time: 2, text: '开场' },
      { time: 12.5, text: '副歌' },
      { time: 20.5, text: '副歌' },
    ])
  })

  it('应用毫秒偏移并定位当前歌词', () => {
    const lyrics = parseLrc('[offset:500]\n[00:01.00]第一句\n[00:03.00]第二句')
    expect(lyrics[0].time).toBe(1.5)
    expect(activeLyricIndex(lyrics, 3.6)).toBe(1)
  })
})
