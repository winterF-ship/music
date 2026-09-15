import { beforeEach, describe, expect, it, vi } from 'vitest'

const { post } = vi.hoisted(() => ({ post: vi.fn() }))

vi.mock('../src/api/http', () => ({
  api: { post },
}))

import { uploadBannerImage } from '../src/api/admin'

describe('后台轮播图裁剪上传', () => {
  beforeEach(() => {
    post.mockReset()
    post.mockResolvedValue({ data: { code: 200 } })
  })

  it('为裁剪产生的 Blob 补充图片文件名', async () => {
    await uploadBannerImage(new Blob(['jpeg'], { type: 'image/jpeg' }))

    const form = post.mock.calls[0][1]
    const uploaded = form.get('image')
    expect(uploaded.name).toBe('banner.jpg')
    expect(uploaded.type).toBe('image/jpeg')
  })
})
