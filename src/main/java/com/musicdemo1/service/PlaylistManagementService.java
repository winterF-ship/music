package com.musicdemo1.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.dto.PlaylistDetailResponse;
import com.musicdemo1.dto.PlaylistSongRequest;
import com.musicdemo1.dto.SongListRequest;
import com.musicdemo1.entity.Song;
import com.musicdemo1.entity.SongList;
import com.musicdemo1.entity.SongListSong;
import com.musicdemo1.mapper.SongListMapper;
import com.musicdemo1.mapper.SongListSongMapper;
import com.musicdemo1.mapper.SongMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaylistManagementService {
    private final SongListMapper songListMapper;
    private final SongListSongMapper relationMapper;
    private final SongMapper songMapper;
    private final CatalogService catalogService;

    public PlaylistManagementService(SongListMapper songListMapper, SongListSongMapper relationMapper,
            SongMapper songMapper, CatalogService catalogService) {
        this.songListMapper = songListMapper;
        this.relationMapper = relationMapper;
        this.songMapper = songMapper;
        this.catalogService = catalogService;
    }

    public PageResponse<SongList> page(long current, long size, String keyword) {
        Page<SongList> page = new Page<>(Math.max(current, 1), Math.min(Math.max(size, 1), 100));
        String normalizedKeyword = blankToNull(keyword);
        songListMapper.selectPage(page, Wrappers.<SongList>lambdaQuery()
                .like(normalizedKeyword != null, SongList::getName, normalizedKeyword)
                .orderByDesc(SongList::getCreatedAt).orderByDesc(SongList::getId));
        return PageResponse.from(page);
    }

    public PlaylistDetailResponse get(Long id) { return catalogService.playlist(id); }

    public SongList create(SongListRequest request) {
        SongList playlist = new SongList();
        apply(playlist, request);
        songListMapper.insert(playlist);
        return playlist;
    }

    public SongList update(Long id, SongListRequest request) {
        SongList playlist = requirePlaylist(id);
        apply(playlist, request);
        songListMapper.updateById(playlist);
        return playlist;
    }

    @Transactional
    public void delete(Long id) {
        if (songListMapper.deleteById(id) == 0) throw new BusinessException(404, "歌单不存在");
    }

    @Transactional
    public PlaylistDetailResponse addSong(Long playlistId, PlaylistSongRequest request) {
        requirePlaylist(playlistId);
        Song song = songMapper.selectById(request.getSongId());
        if (song == null) throw new BusinessException(404, "歌曲不存在");
        SongListSong existing = relationMapper.selectOne(Wrappers.<SongListSong>lambdaQuery()
                .eq(SongListSong::getSongListId, playlistId).eq(SongListSong::getSongId, request.getSongId()));
        if (existing != null) throw new BusinessException(409, "该歌曲已经在歌单中");
        SongListSong relation = new SongListSong();
        relation.setSongListId(playlistId);
        relation.setSongId(request.getSongId());
        relation.setSortNo(request.getSortNo() == null ? 0 : Math.max(request.getSortNo(), 0));
        relationMapper.insert(relation);
        return get(playlistId);
    }

    @Transactional
    public PlaylistDetailResponse removeSong(Long playlistId, Long songId) {
        requirePlaylist(playlistId);
        int deleted = relationMapper.delete(Wrappers.<SongListSong>lambdaQuery()
                .eq(SongListSong::getSongListId, playlistId).eq(SongListSong::getSongId, songId));
        if (deleted == 0) throw new BusinessException(404, "歌单中没有这首歌曲");
        return get(playlistId);
    }

    private SongList requirePlaylist(Long id) {
        SongList playlist = songListMapper.selectById(id);
        if (playlist == null) throw new BusinessException(404, "歌单不存在");
        return playlist;
    }

    private void apply(SongList playlist, SongListRequest request) {
        playlist.setName(request.getName().trim());
        playlist.setCoverUrl(blankToNull(request.getCoverUrl()));
        playlist.setDescription(blankToNull(request.getDescription()));
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
