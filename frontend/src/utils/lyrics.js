const TIMESTAMP = /\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?\]/g

export function parseLrc(source = '') {
  const offsetMatch = source.match(/\[offset:([+-]?\d+)\]/i)
  const offsetSeconds = offsetMatch ? Number(offsetMatch[1]) / 1000 : 0
  const lines = []

  for (const rawLine of source.replace(/^\uFEFF/, '').split(/\r?\n/)) {
    const stamps = [...rawLine.matchAll(TIMESTAMP)]
    if (!stamps.length) continue
    const text = rawLine.replace(TIMESTAMP, '').trim() || '♪'
    for (const stamp of stamps) {
      const fraction = stamp[3] ? Number(`0.${stamp[3].padEnd(3, '0').slice(0, 3)}`) : 0
      lines.push({ time: Math.max(0, Number(stamp[1]) * 60 + Number(stamp[2]) + fraction + offsetSeconds), text })
    }
  }

  return lines.sort((a, b) => a.time - b.time)
}

export function activeLyricIndex(lines, currentTime) {
  let active = -1
  for (let index = 0; index < lines.length; index += 1) {
    if (lines[index].time > currentTime + 0.05) break
    active = index
  }
  return active
}
