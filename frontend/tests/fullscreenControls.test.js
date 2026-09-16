import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { FULLSCREEN_IDLE_DELAY, createIdleChrome } from '../src/utils/fullscreenControls'

describe('全屏播放器控制条自动隐藏', () => {
  beforeEach(() => vi.useFakeTimers())
  afterEach(() => vi.useRealTimers())

  it('播放中静置后隐藏，并触发一次隐藏回调', () => {
    const onHidden = vi.fn()
    const chrome = createIdleChrome()
    chrome.setHiddenHandler(onHidden)
    chrome.setActive(true)
    expect(chrome.visible).toBe(true)
    vi.advanceTimersByTime(FULLSCREEN_IDLE_DELAY)
    expect(chrome.visible).toBe(false)
    expect(onHidden).toHaveBeenCalledTimes(1)
  })

  it('指针或键盘活动会唤回控制条并重新计时', () => {
    const chrome = createIdleChrome({ delay: 1000 })
    chrome.setActive(true)
    vi.advanceTimersByTime(900)
    chrome.wake()
    expect(chrome.visible).toBe(true)
    vi.advanceTimersByTime(900)
    expect(chrome.visible).toBe(true)
    vi.advanceTimersByTime(100)
    expect(chrome.visible).toBe(false)
  })

  it('暂停或无歌曲时始终可见，不进入隐藏计时', () => {
    const chrome = createIdleChrome({ delay: 1000 })
    chrome.setActive(true)
    chrome.setActive(false)
    vi.advanceTimersByTime(5000)
    expect(chrome.visible).toBe(true)
    expect(chrome.active).toBe(false)
  })

  it('暂停后再次播放重新开始计时', () => {
    const chrome = createIdleChrome({ delay: 1000 })
    chrome.setActive(true)
    chrome.setActive(false)
    chrome.setActive(true)
    vi.advanceTimersByTime(999)
    expect(chrome.visible).toBe(true)
    vi.advanceTimersByTime(1)
    expect(chrome.visible).toBe(false)
  })

  it('隐藏后 wake 能恢复显示，stop 可清理计时器', () => {
    const chrome = createIdleChrome({ delay: 1000 })
    chrome.setActive(true)
    vi.advanceTimersByTime(1000)
    expect(chrome.visible).toBe(false)
    chrome.wake()
    expect(chrome.visible).toBe(true)
    chrome.stop()
    vi.advanceTimersByTime(5000)
    expect(chrome.visible).toBe(true)
  })

  it('重复设置相同的可隐藏状态不会重置已有计时', () => {
    const onHidden = vi.fn()
    const chrome = createIdleChrome({ delay: 1000 })
    chrome.setHiddenHandler(onHidden)
    chrome.setActive(true)
    vi.advanceTimersByTime(900)
    chrome.setActive(true)
    vi.advanceTimersByTime(100)
    expect(chrome.visible).toBe(false)
    expect(onHidden).toHaveBeenCalledTimes(1)
  })
})
