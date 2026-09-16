package com.musicdemo1.service;

import com.musicdemo1.config.PlaybackSchemaInitializer;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "PLAYBACK_DB_TEST", matches = "true")
class PlaybackDatabaseTest {
    @Test
    void persistsDistinctEventsAndDeduplicatesRetries() {
        var dataSource = new DriverManagerDataSource(
            System.getenv().getOrDefault("DB_URL", "jdbc:mysql://127.0.0.1:3306/musicdemo1?serverTimezone=Asia/Shanghai"),
            System.getenv().getOrDefault("DB_USERNAME", "root"),
            System.getenv().getOrDefault("DB_PASSWORD", "123456"));
        var jdbc = new JdbcTemplate(dataSource);
        new PlaybackSchemaInitializer(jdbc).run(null);
        new TransactionTemplate(new DataSourceTransactionManager(dataSource)).execute(status -> {
            try {
                jdbc.update("INSERT INTO singer (name) VALUES (?)", "playback-test-" + UUID.randomUUID());
                long singerId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
                jdbc.update("INSERT INTO song (title, singer_id, audio_url) VALUES (?, ?, ?)", "播放统计事务测试", singerId, "/test.mp3");
                long songId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
                var service = new PlaybackService(jdbc);
                UUID eventId = UUID.randomUUID();
                service.record(songId, eventId);
                service.record(songId, eventId);
                service.record(songId, UUID.randomUUID());
                assertEquals(2L, jdbc.queryForObject("SELECT COUNT(*) FROM song_play_event WHERE song_id = ?", Long.class, songId));
                assertEquals(2L, service.counts().stream().filter(item -> item.getLabel().endsWith("#" + songId)).findFirst().orElseThrow().getValue());
                return null;
            } finally { status.setRollbackOnly(); }
        });
    }
}
