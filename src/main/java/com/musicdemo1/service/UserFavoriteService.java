package com.musicdemo1.service;

import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.FavoriteStateResponse;
import com.musicdemo1.dto.SongView;
import com.musicdemo1.entity.SongList;
import com.musicdemo1.mapper.SongListMapper;
import com.musicdemo1.mapper.SongMapper;
import com.musicdemo1.mapper.UserFavoriteMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserFavoriteService {
    private final UserFavoriteMapper favoriteMapper;
    private final SongMapper songMapper;
    private final SongListMapper songListMapper;

    public UserFavoriteService(UserFavoriteMapper favoriteMapper, SongMapper songMapper,
            SongListMapper songListMapper) {
        this.favoriteMapper = favoriteMapper;
        this.songMapper = songMapper;
        this.songListMapper = songListMapper;
    }

    public List<SongView> songs(Long userId) {
        return favoriteMapper.selectSongs(userId);
    }

    public FavoriteStateResponse addSong(Long userId, Long songId) {
        if (songMapper.selectById(songId) == null) throw new BusinessException(404, "歌曲不存在");
        favoriteMapper.addSong(userId, songId);
        return new FavoriteStateResponse(true);
    }

    public FavoriteStateResponse removeSong(Long userId, Long songId) {
        favoriteMapper.removeSong(userId, songId);
        return new FavoriteStateResponse(false);
    }

    public List<SongList> playlists(Long userId) {
        return favoriteMapper.selectPlaylists(userId);
    }

    public FavoriteStateResponse addPlaylist(Long userId, Long playlistId) {
        if (songListMapper.selectById(playlistId) == null) throw new BusinessException(404, "歌单不存在");
        favoriteMapper.addPlaylist(userId, playlistId);
        return new FavoriteStateResponse(true);
    }

    public FavoriteStateResponse removePlaylist(Long userId, Long playlistId) {
        favoriteMapper.removePlaylist(userId, playlistId);
        return new FavoriteStateResponse(false);
    }
}
