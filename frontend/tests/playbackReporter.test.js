import { describe, it, expect, vi } from 'vitest'
import { createPlaybackReporter } from '../src/utils/playbackReporter'
vi.mock('../src/api/http', () => ({ api: { post: vi.fn() } }))

describe('actual playback reporting', () => {
  it('does not count selection; counts playing once across resume and buffering', async () => {
    const send = vi.fn().mockResolvedValue({ data: { code: 200 } })
    const reporter = createPlaybackReporter(send)
    reporter.start(12)
    expect(send).not.toHaveBeenCalled()
    await reporter.playing()
    await reporter.playing()
    expect(send).toHaveBeenCalledTimes(1)
    reporter.start(12)
    await reporter.playing()
    expect(send).toHaveBeenCalledTimes(2)
    expect(send.mock.calls[0][1]).not.toBe(send.mock.calls[1][1])
  })
  it('retries failed reports with the same id and does not count an empty queue', async () => {
    const send = vi.fn().mockRejectedValueOnce(new Error('offline')).mockResolvedValue({ data: { code: 200 } })
    const reporter = createPlaybackReporter(send)
    await reporter.playing()
    expect(send).not.toHaveBeenCalled()
    reporter.start(3)
    await reporter.playing()
    await reporter.playing()
    expect(send.mock.calls[0]).toEqual(send.mock.calls[1])
  })
  it('keeps an in-flight old track separate from a new playback', async () => {
    let resolve
    const send = vi.fn().mockImplementationOnce(() => new Promise(r => { resolve = r })).mockResolvedValue({ data: { code: 200 } })
    const reporter = createPlaybackReporter(send)
    reporter.start(1)
    const pending = reporter.playing()
    await reporter.playing()
    expect(send).toHaveBeenCalledTimes(1)
    reporter.start(2)
    await reporter.playing()
    resolve({ data: { code: 200 } })
    await pending
    await reporter.playing()
    expect(send).toHaveBeenCalledTimes(2)
  })
})
