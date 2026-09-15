package com.musicdemo1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.musicdemo1.dto.SongView;
import com.musicdemo1.dto.CountItem;
import com.musicdemo1.entity.Song;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SongMapper extends BaseMapper<Song> {
    @Select("""
            <script>
            SELECT s.id, s.title, s.singer_id, si.name AS singer_name,
                   s.cover_url, s.audio_url, s.lyric_url, s.duration_seconds AS duration
            FROM song s LEFT JOIN singer si ON si.id = s.singer_id
            <where>
              <if test="keyword != null and keyword != ''">
                AND s.title LIKE CONCAT('%', #{keyword}, '%')
              </if>
              <if test="singerId != null">
                AND s.singer_id = #{singerId}
              </if>
            </where>
            ORDER BY s.created_at DESC, s.id DESC
            </script>
            """)
    IPage<SongView> selectPageView(Page<SongView> page, @Param("keyword") String keyword,
                                   @Param("singerId") Long singerId);

    @Select("""
            SELECT s.id, s.title, s.singer_id, si.name AS singer_name,
                   s.cover_url, s.audio_url, s.lyric_url, s.duration_seconds AS duration
            FROM song s LEFT JOIN singer si ON si.id = s.singer_id
            WHERE s.id = #{id}
            """)
    SongView selectViewById(@Param("id") Long id);

    @Select("""
            SELECT s.id, s.title, s.singer_id, si.name AS singer_name,
                   s.cover_url, s.audio_url, s.lyric_url, s.duration_seconds AS duration
            FROM song s LEFT JOIN singer si ON si.id = s.singer_id
            WHERE s.singer_id = #{singerId}
            ORDER BY s.created_at DESC, s.id DESC
            """)
    List<SongView> selectViewsBySingerId(@Param("singerId") Long singerId);

    @Select("""
            SELECT s.id, s.title, s.singer_id, si.name AS singer_name,
                   s.cover_url, s.audio_url, s.lyric_url, s.duration_seconds AS duration
            FROM song_list_song sls
            INNER JOIN song s ON s.id = sls.song_id
            LEFT JOIN singer si ON si.id = s.singer_id
            WHERE sls.song_list_id = #{playlistId}
            ORDER BY sls.sort_no ASC, s.id ASC
            """)
    List<SongView> selectViewsByPlaylistId(@Param("playlistId") Long playlistId);

    @Select("""
            SELECT COALESCE(si.name, '未命名歌手') AS label, COUNT(*) AS value
            FROM song s LEFT JOIN singer si ON si.id = s.singer_id
            GROUP BY s.singer_id, si.name
            ORDER BY value DESC, label ASC
            """)
    List<CountItem> countSongsBySinger();
}
