package com.musicdemo1.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.BannerRequest;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.entity.Banner;
import com.musicdemo1.mapper.BannerMapper;
import org.springframework.stereotype.Service;

@Service
public class BannerManagementService {
    private final BannerMapper bannerMapper;

    public BannerManagementService(BannerMapper bannerMapper) { this.bannerMapper = bannerMapper; }

    public PageResponse<Banner> page(long current, long size, String keyword) {
        Page<Banner> page = new Page<>(Math.max(current, 1), Math.min(Math.max(size, 1), 100));
        bannerMapper.selectPage(page, Wrappers.<Banner>lambdaQuery()
                .like(keyword != null && !keyword.isBlank(), Banner::getTitle, keyword == null ? null : keyword.trim())
                .orderByAsc(Banner::getSortNo).orderByDesc(Banner::getId));
        return PageResponse.from(page);
    }

    public Banner get(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) throw new BusinessException(404, "轮播图不存在");
        return banner;
    }

    public Banner create(BannerRequest request) { Banner banner = new Banner(); apply(banner, request); bannerMapper.insert(banner); return banner; }

    public Banner update(Long id, BannerRequest request) { Banner banner = get(id); apply(banner, request); bannerMapper.updateById(banner); return banner; }

    public void delete(Long id) { if (bannerMapper.deleteById(id) == 0) throw new BusinessException(404, "轮播图不存在"); }

    private void apply(Banner banner, BannerRequest request) {
        banner.setTitle(request.getTitle().trim());
        banner.setImageUrl(request.getImageUrl().trim());
        banner.setTargetUrl(request.getTargetUrl() == null || request.getTargetUrl().isBlank() ? null : request.getTargetUrl().trim());
        banner.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        banner.setStatus(request.getStatus());
    }
}
