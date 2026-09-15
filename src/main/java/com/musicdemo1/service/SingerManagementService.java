package com.musicdemo1.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.dto.SingerRequest;
import com.musicdemo1.dto.SongView;
import com.musicdemo1.entity.Singer;
import com.musicdemo1.mapper.SingerMapper;
import com.musicdemo1.mapper.SongMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SingerManagementService {
    private final SingerMapper singerMapper;
    private final SongMapper songMapper;

    public SingerManagementService(SingerMapper singerMapper, SongMapper songMapper) {
        this.singerMapper = singerMapper;
        this.songMapper = songMapper;
    }

    public PageResponse<Singer> page(long current, long size, String keyword) {
        Page<Singer> page = new Page<>(Math.max(current, 1), Math.min(Math.max(size, 1), 100));
        String normalizedKeyword = blankToNull(keyword);
        singerMapper.selectPage(page, Wrappers.<Singer>lambdaQuery()
                .like(normalizedKeyword != null, Singer::getName, normalizedKeyword)
                .orderByDesc(Singer::getCreatedAt).orderByDesc(Singer::getId));
        return PageResponse.from(page);
    }

    public Singer get(Long id) {
        Singer singer = singerMapper.selectById(id);
        if (singer == null) throw new BusinessException(404, "歌手不存在");
        return singer;
    }

    public Singer create(SingerRequest request) {
        ensureNameUnique(request.getName(), null);
        Singer singer = new Singer();
        apply(singer, request);
        singerMapper.insert(singer);
        return singer;
    }

    public Singer update(Long id, SingerRequest request) {
        Singer singer = get(id);
        ensureNameUnique(request.getName(), id);
        apply(singer, request);
        singerMapper.updateById(singer);
        return singer;
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        Long songs = songMapper.selectCount(Wrappers.<com.musicdemo1.entity.Song>lambdaQuery()
                .eq(com.musicdemo1.entity.Song::getSingerId, id));
        if (songs != null && songs > 0) {
            throw new BusinessException(409, "该歌手仍有关联歌曲，请先处理歌曲");
        }
        singerMapper.deleteById(id);
    }

    private void ensureNameUnique(String name, Long id) {
        var query = Wrappers.<Singer>lambdaQuery().eq(Singer::getName, name.trim());
        if (id != null) query.ne(Singer::getId, id);
        if (singerMapper.selectCount(query) > 0) throw new BusinessException("歌手姓名已存在");
    }

    private void apply(Singer singer, SingerRequest request) {
        singer.setName(request.getName().trim());
        singer.setAvatarUrl(blankToNull(request.getAvatarUrl()));
        singer.setIntroduction(blankToNull(request.getIntroduction()));
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
