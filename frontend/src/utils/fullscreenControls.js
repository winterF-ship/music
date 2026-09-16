export const FULLSCREEN_IDLE_DELAY = 3200

/**
 * 全屏播放器控制条与光标的自动隐藏策略。
 *
 * - 播放中静置 `delay` 毫秒后隐藏控制条与光标；
 * - 指针移动、滚轮、触摸或键盘操作会立刻唤回，并重新计时；
 * - 暂停、音频失败或没有歌曲时始终可见，避免找不到播放按钮。
 *
 * 只维护状态与计时，不依赖 DOM 或 Vue，便于单独测试。
 */
export function createIdleChrome(options = {}) {
  const delay = Number.isFinite(options.delay) ? options.delay : FULLSCREEN_IDLE_DELAY
  let timer
  let active = false
  let visible = true
  let onHidden

  function clearTimer() {
    if (timer === undefined) return
    clearTimeout(timer)
    timer = undefined
  }

  function schedule() {
    clearTimer()
    if (!active || !visible) return
    timer = setTimeout(() => {
      timer = undefined
      if (!active) return
      visible = false
      onHidden?.()
    }, delay)
  }

  return {
    /** 控制条当前是否应当显示 */
    get visible() {
      return visible
    },
    /** 是否处于可自动隐藏的状态（全屏且正在播放） */
    get active() {
      return active
    },
    setHiddenHandler(handler) {
      onHidden = typeof handler === 'function' ? handler : undefined
    },
    /** 同步「应当自动隐藏」的条件；条件不成立时立刻恢复显示并停止计时 */
    setActive(next) {
      const value = Boolean(next)
      // 重复同步同一条件（例如播放状态未变化）不重置静置计时
      if (value === active) return
      active = value
      if (active) schedule()
      else {
        clearTimer()
        visible = true
      }
    },
    /** 指针、滚轮、触摸或键盘活动：唤回控制条并重新计时 */
    wake() {
      visible = true
      schedule()
    },
    stop() {
      clearTimer()
    },
  }
}
