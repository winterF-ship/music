import { describe, expect, it } from 'vitest'
import { MAX_IMAGE_SIZE, validateImageFile } from '../src/utils/imageUpload'

function file(name, type, size = 1024) {
  return { name, type, size }
}

describe('后台图片上传校验', () => {
  it('接受支持的图片格式', () => {
    expect(validateImageFile(file('cover.webp', 'image/webp'))).toBe('')
    expect(validateImageFile(file('avatar.JPG', 'image/jpeg'))).toBe('')
  })

  it('拒绝伪装图片和超过 10MB 的文件', () => {
    expect(validateImageFile(file('cover.exe', 'image/png'))).toContain('仅支持')
    expect(validateImageFile(file('cover.png', 'image/png', MAX_IMAGE_SIZE + 1))).toContain('10MB')
  })
})
