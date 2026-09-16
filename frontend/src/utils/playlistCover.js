export const PLAYLIST_COVER_GRADIENTS = [
  'linear-gradient(145deg, #8CA8D0 0%, #F9D878 100%)',
  'linear-gradient(145deg, #F2B6B6 0%, #6A90A6 100%)',
  'linear-gradient(145deg, #F5B89E 0%, #B8A6D3 100%)',
  'linear-gradient(145deg, #FBE2A2 0%, #8C8C94 100%)',
]

export function playlistCoverGradient(seed = '') {
  const value = String(seed)
  let hash = 0

  for (let index = 0; index < value.length; index += 1) {
    hash = ((hash << 5) - hash + value.charCodeAt(index)) | 0
  }

  return PLAYLIST_COVER_GRADIENTS[Math.abs(hash) % PLAYLIST_COVER_GRADIENTS.length]
}
