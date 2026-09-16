package com.musicdemo1.service;

import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.CountItem;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PlaybackService {
    private final JdbcTemplate jdbc;
    public PlaybackService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void record(long songId, UUID eventId) {
        Integer exists = jdbc.queryForObject("SELECT COUNT(*) FROM song WHERE id = ?", Integer.class, songId);
        if (exists == null || exists == 0) throw new BusinessException(404, "歌曲不存在");
        jdbc.update("""
            INSERT INTO song_play_event (event_id, song_id) VALUES (?, ?)
            ON DUPLICATE KEY UPDATE event_id = event_id
            """, eventId.toString(), songId);
    }

    public List<CountItem> counts() {
        return jdbc.query("""
            SELECT CONCAT(s.title, ' · ', COALESCE(si.name, '未知歌手'), ' #', s.id) AS label,
                   COUNT(*) AS value
            FROM song_play_event p JOIN song s ON s.id = p.song_id
            LEFT JOIN singer si ON si.id = s.singer_id
            GROUP BY s.id, s.title, si.name
            ORDER BY value DESC, s.id ASC
            """, (rs, row) -> new CountItem(rs.getString("label"), rs.getLong("value")));
    }
}
