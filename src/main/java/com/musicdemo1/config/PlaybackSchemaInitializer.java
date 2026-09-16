package com.musicdemo1.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PlaybackSchemaInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    public PlaybackSchemaInitializer(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override
    public void run(ApplicationArguments args) {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS song_play_event (
                event_id CHAR(36) NOT NULL PRIMARY KEY,
                song_id BIGINT NOT NULL,
                played_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                KEY idx_song_play_event_song (song_id),
                CONSTRAINT fk_song_play_event_song FOREIGN KEY (song_id) REFERENCES song(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);
    }
}
