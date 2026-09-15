package com.musicdemo1.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserFeatureSchemaInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public UserFeatureSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer lyricColumnCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'song' AND COLUMN_NAME = 'lyric_url'
                """, Integer.class);
        if (lyricColumnCount != null && lyricColumnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE `song` ADD COLUMN `lyric_url` VARCHAR(255) DEFAULT NULL AFTER `audio_url`");
        }
        Integer playlistOwnerColumnCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'song_list' AND COLUMN_NAME = 'owner_user_id'
                """, Integer.class);
        if (playlistOwnerColumnCount != null && playlistOwnerColumnCount == 0) {
            jdbcTemplate.execute("ALTER TABLE `song_list` ADD COLUMN `owner_user_id` BIGINT DEFAULT NULL AFTER `description`");
        }
        Integer playlistOwnerKeyCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
                WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'song_list'
                  AND CONSTRAINT_NAME = 'fk_song_list_owner_user'
                """, Integer.class);
        if (playlistOwnerKeyCount != null && playlistOwnerKeyCount == 0) {
            jdbcTemplate.execute("ALTER TABLE `song_list` ADD CONSTRAINT `fk_song_list_owner_user` FOREIGN KEY (`owner_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE");
        }
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `collection` (
                  `user_id` BIGINT NOT NULL,
                  `song_id` BIGINT NOT NULL,
                  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  PRIMARY KEY (`user_id`, `song_id`),
                  KEY `idx_collection_song_id` (`song_id`),
                  CONSTRAINT `fk_collection_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                  CONSTRAINT `fk_collection_song` FOREIGN KEY (`song_id`) REFERENCES `song` (`id`) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏歌曲'
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `playlist_collection` (
                  `user_id` BIGINT NOT NULL,
                  `song_list_id` BIGINT NOT NULL,
                  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  PRIMARY KEY (`user_id`, `song_list_id`),
                  KEY `idx_playlist_collection_song_list_id` (`song_list_id`),
                  CONSTRAINT `fk_playlist_collection_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                  CONSTRAINT `fk_playlist_collection_song_list` FOREIGN KEY (`song_list_id`) REFERENCES `song_list` (`id`) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏歌单'
                """);
    }
}
