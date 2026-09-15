export const IMAGE_ACCEPT = 'image/jpeg,image/png,image/gif,image/webp'
export const MAX_IMAGE_SIZE = 10 * 1024 * 1024

const IMAGE_EXTENSIONS = new Set(['jpg', 'jpeg', 'png', 'gif', 'webp'])

export function validateImageFile(file) {
  if (!file || file.size === 0) return '请选择有效的图片文件'
  const extension = String(file.name || '').split('.').pop()?.toLowerCase()
  if (!String(file.type || '').startsWith('image/') || !IMAGE_EXTENSIONS.has(extension)) {
    return '仅支持 JPG、PNG、GIF 或 WebP 图片'
  }
  if (file.size > MAX_IMAGE_SIZE) return '图片大小不能超过 10MB'
  return ''
}

export function resolveAssetUrl(path, baseUrl = 'http://127.0.0.1:8081/api') {
  if (!path || /^https?:\/\//i.test(path) || path.startsWith('data:') || path.startsWith('blob:')) return path || ''
  const assetBase = new URL(baseUrl, window.location.origin)
  assetBase.pathname = assetBase.pathname.replace(/\/api\/?$/, '/')
  return new URL(path.replace(/^\//, ''), assetBase).href
}
