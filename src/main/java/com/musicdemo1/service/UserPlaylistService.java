package com.musicdemo1.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.PlaylistDetailResponse;
import com.musicdemo1.dto.PlaylistSongRequest;
import com.musicdemo1.dto.SongListRequest;
import com.musicdemo1.entity.Song;
import com.musicdemo1.entity.SongList;
import com.musicdemo1.entity.SongListSong;
import com.musicdemo1.mapper.SongListMapper;
import com.musicdemo1.mapper.SongListSongMapper;
import com.musicdemo1.mapper.SongMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPlaylistService {
    private final SongListMapper songListMapper;
    private final SongListSongMapper relationMapper;
    private final SongMapper songMapper;

    public UserPlaylistService(SongListMapper songListMapper, SongListSongMapper relationMapper,
            SongMapper songMapper) {
        this.songListMapper = songListMapper;
        this.relationMapper = relationMapper;
        this.songMapper = songMapper;
    }

    public List<SongList> list(Long userId) {
        return songListMapper.selectList(Wrappers.<SongList>lambdaQuery()
                .eq(SongList::getOwnerUserId, userId)
                .orderByDesc(SongList::getCreatedAt)
                .orderByDesc(SongList::getId));
    }

    @Transactional
    public SongList create(Long userId, SongListRequest request) {
        SongList playlist = new SongList();
        playlist.setOwnerUserId(userId);
        playlist.setName(request.getName().trim());
        playlist.setCoverUrl(blankToNull(request.getCoverUrl()));
        playlist.setDescription(blankToNull(request.getDescription()));
        songListMapper.insert(playlist);
        return playlist;
    }

    @Transactional
    public SongList update(Long userId, Long playlistId, SongListRequest request) {
        SongList playlist = requireOwnedPlaylist(userId, playlistId);
        playlist.setName(request.getName().trim());
        playlist.setCoverUrl(blankToNull(request.getCoverUrl()));
        playlist.setDescription(blankToNull(request.getDescription()));
        songListMapper.updateById(playlist);
        return playlist;
    }

    @Transactional
    public PlaylistDetailResponse addSong(Long userId, Long playlistId, PlaylistSongRequest request) {
        SongList playlist = requireOwnedPlaylist(userId, playlistId);
        Song song = songMapper.selectById(request.getSongId());
        if (song == null) throw new BusinessException(404, "歌曲不存在");
        SongListSong existing = relationMapper.selectOne(Wrappers.<SongListSong>lambdaQuery()
                .eq(SongListSong::getSongListId, playlistId)
                .eq(SongListSong::getSongId, request.getSongId()));
        if (existing != null) throw new BusinessException(409, "该歌曲已经在歌单中");
        SongListSong relation = new SongListSong();
        relation.setSongListId(playlistId);
        relation.setSongId(request.getSongId());
        relation.setSortNo(request.getSortNo() == null ? 0 : Math.max(request.getSortNo(), 0));
        relationMapper.insert(relation);
        return new PlaylistDetailResponse(playlist, songMapper.selectViewsByPlaylistId(playlistId));
    }

    private SongList requireOwnedPlaylist(Long userId, Long playlistId) {
        SongList playlist = songListMapper.selectOne(Wrappers.<SongList>lambdaQuery()
                .eq(SongList::getId, playlistId)
                .eq(SongList::getOwnerUserId, userId));
        if (playlist == null) throw new BusinessException(404, "歌单不存在或无权操作");
        return playlist;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
