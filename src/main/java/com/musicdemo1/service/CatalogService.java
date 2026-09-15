package com.musicdemo1.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.dto.PlaylistDetailResponse;
import com.musicdemo1.dto.SingerDetailResponse;
import com.musicdemo1.dto.SongView;
import com.musicdemo1.entity.Singer;
import com.musicdemo1.entity.SongList;
import com.musicdemo1.entity.Banner;
import com.musicdemo1.mapper.BannerMapper;
import java.util.List;
import com.musicdemo1.mapper.SingerMapper;
import com.musicdemo1.mapper.SongListMapper;
import com.musicdemo1.mapper.SongMapper;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {
    private final SongMapper songMapper;
    private final SingerMapper singerMapper;
    private final SongListMapper songListMapper;
    private final BannerMapper bannerMapper;

    public CatalogService(SongMapper songMapper, SingerMapper singerMapper, SongListMapper songListMapper, BannerMapper bannerMapper) {
        this.songMapper = songMapper;
        this.singerMapper = singerMapper;
        this.songListMapper = songListMapper;
        this.bannerMapper = bannerMapper;
    }

    public PageResponse<SongView> songs(long current, long size, String keyword, Long singerId) {
        Page<SongView> page = new Page<>(safePage(current), safeSize(size));
        return PageResponse.from(songMapper.selectPageView(page, blankToNull(keyword), singerId));
    }

    public SongView song(Long id) {
        SongView song = songMapper.selectViewById(id);
        if (song == null) throw new BusinessException(404, "歌曲不存在");
        return song;
    }

    public PageResponse<Singer> singers(long current, long size) {
        Page<Singer> page = new Page<>(safePage(current), safeSize(size));
        singerMapper.selectPage(page, Wrappers.<Singer>lambdaQuery().orderByDesc(Singer::getCreatedAt).orderByDesc(Singer::getId));
        return PageResponse.from(page);
    }

    public SingerDetailResponse singer(Long id) {
        Singer singer = singerMapper.selectById(id);
        if (singer == null) throw new BusinessException(404, "歌手不存在");
        return new SingerDetailResponse(singer, songMapper.selectViewsBySingerId(id));
    }

    public PageResponse<SongList> playlists(long current, long size) {
        Page<SongList> page = new Page<>(safePage(current), safeSize(size));
        songListMapper.selectPage(page, Wrappers.<SongList>lambdaQuery()
                .orderByDesc(SongList::getCreatedAt).orderByDesc(SongList::getId));
        return PageResponse.from(page);
    }

    public PlaylistDetailResponse playlist(Long id) {
        SongList playlist = songListMapper.selectById(id);
        if (playlist == null) throw new BusinessException(404, "歌单不存在");
        return new PlaylistDetailResponse(playlist, songMapper.selectViewsByPlaylistId(id));
    }

    public List<Banner> banners() {
        return bannerMapper.selectActive();
    }

    private long safePage(long current) { return Math.max(current, 1); }
    private long safeSize(long size) { return Math.min(Math.max(size, 1), 100); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
