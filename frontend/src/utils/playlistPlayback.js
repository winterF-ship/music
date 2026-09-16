let latestPlaylistPlaybackRequest = 0

export function beginPlaylistPlaybackRequest() {
  latestPlaylistPlaybackRequest += 1
  return latestPlaylistPlaybackRequest
}

export function isLatestPlaylistPlaybackRequest(requestId) {
  return requestId === latestPlaylistPlaybackRequest
}

export function shufflePlaylistSongs(songs, random = Math.random) {
  const shuffled = [...songs]

  for (let index = shuffled.length - 1; index > 0; index -= 1) {
    const target = Math.floor(random() * (index + 1))
    ;[shuffled[index], shuffled[target]] = [shuffled[target], shuffled[index]]
  }

  return shuffled
}
