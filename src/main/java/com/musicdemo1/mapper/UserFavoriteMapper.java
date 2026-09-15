package com.musicdemo1.mapper;

import com.musicdemo1.dto.SongView;
import com.musicdemo1.entity.SongList;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserFavoriteMapper {
    @Select("SELECT COUNT(*) FROM `collection` WHERE user_id = #{userId} AND song_id = #{songId}")
    int countSong(@Param("userId") Long userId, @Param("songId") Long songId);

    @Insert("INSERT IGNORE INTO `collection` (user_id, song_id) VALUES (#{userId}, #{songId})")
    int addSong(@Param("userId") Long userId, @Param("songId") Long songId);

    @Delete("DELETE FROM `collection` WHERE user_id = #{userId} AND song_id = #{songId}")
    int removeSong(@Param("userId") Long userId, @Param("songId") Long songId);

    @Select("""
            SELECT s.id, s.title, s.singer_id, si.name AS singer_name,
                   s.cover_url, s.audio_url, s.duration_seconds AS duration
            FROM `collection` c
            INNER JOIN song s ON s.id = c.song_id
            LEFT JOIN singer si ON si.id = s.singer_id
            WHERE c.user_id = #{userId}
            ORDER BY c.created_at DESC, s.id DESC
            """)
    List<SongView> selectSongs(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM playlist_collection WHERE user_id = #{userId} AND song_list_id = #{playlistId}")
    int countPlaylist(@Param("userId") Long userId, @Param("playlistId") Long playlistId);

    @Insert("INSERT IGNORE INTO playlist_collection (user_id, song_list_id) VALUES (#{userId}, #{playlistId})")
    int addPlaylist(@Param("userId") Long userId, @Param("playlistId") Long playlistId);

    @Delete("DELETE FROM playlist_collection WHERE user_id = #{userId} AND song_list_id = #{playlistId}")
    int removePlaylist(@Param("userId") Long userId, @Param("playlistId") Long playlistId);

    @Select("""
            SELECT sl.id, sl.name, sl.cover_url, sl.description, sl.created_at, sl.updated_at
            FROM playlist_collection pc
            INNER JOIN song_list sl ON sl.id = pc.song_list_id
            WHERE pc.user_id = #{userId}
            ORDER BY pc.created_at DESC, sl.id DESC
            """)
    List<SongList> selectPlaylists(@Param("userId") Long userId);
}
