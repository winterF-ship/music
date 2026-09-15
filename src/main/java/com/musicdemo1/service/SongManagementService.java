package com.musicdemo1.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.dto.SongRequest;
import com.musicdemo1.dto.SongView;
import com.musicdemo1.entity.Singer;
import com.musicdemo1.entity.Song;
import com.musicdemo1.mapper.SingerMapper;
import com.musicdemo1.mapper.SongMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SongManagementService {
    private final SongMapper songMapper;
    private final SingerMapper singerMapper;

    public SongManagementService(SongMapper songMapper, SingerMapper singerMapper) {
        this.songMapper = songMapper;
        this.singerMapper = singerMapper;
    }

    public PageResponse<SongView> page(long current, long size, String keyword, Long singerId) {
        Page<SongView> page = new Page<>(Math.max(current, 1), Math.min(Math.max(size, 1), 100));
        return PageResponse.from(songMapper.selectPageView(page, keyword == null || keyword.isBlank() ? null : keyword.trim(), singerId));
    }

    public SongView get(Long id) {
        SongView song = songMapper.selectViewById(id);
        if (song == null) throw new BusinessException(404, "歌曲不存在");
        return song;
    }

    public SongView create(SongRequest request) {
        Singer singer = singerMapper.selectById(request.getSingerId());
        if (singer == null) throw new BusinessException("所选歌手不存在");
        Song song = new Song();
        apply(song, request);
        songMapper.insert(song);
        return get(song.getId());
    }

    public SongView update(Long id, SongRequest request) {
        Singer singer = singerMapper.selectById(request.getSingerId());
        if (singer == null) throw new BusinessException("所选歌手不存在");
        Song song = requireEntity(id);
        apply(song, request);
        songMapper.updateById(song);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        if (songMapper.deleteById(id) == 0) throw new BusinessException(404, "歌曲不存在");
    }

    private Song requireEntity(Long id) {
        Song song = songMapper.selectById(id);
        if (song == null) throw new BusinessException(404, "歌曲不存在");
        return song;
    }

    private void apply(Song song, SongRequest request) {
        song.setTitle(request.getTitle().trim());
        song.setSingerId(request.getSingerId());
        song.setCoverUrl(blankToNull(request.getCoverUrl()));
        song.setAudioUrl(request.getAudioUrl().trim());
        song.setLyricUrl(blankToNull(request.getLyricUrl()));
        song.setDuration(request.getDurationSeconds());
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
