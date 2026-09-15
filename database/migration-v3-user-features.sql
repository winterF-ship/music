USE `musicdemo1`;

CREATE TABLE IF NOT EXISTS `collection` (
  `user_id` BIGINT NOT NULL,
  `song_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `song_id`),
  KEY `idx_collection_song_id` (`song_id`),
  CONSTRAINT `fk_collection_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_collection_song` FOREIGN KEY (`song_id`) REFERENCES `song` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏歌曲';

CREATE TABLE IF NOT EXISTS `playlist_collection` (
  `user_id` BIGINT NOT NULL,
  `song_list_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `song_list_id`),
  KEY `idx_playlist_collection_song_list_id` (`song_list_id`),
  CONSTRAINT `fk_playlist_collection_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_playlist_collection_song_list` FOREIGN KEY (`song_list_id`) REFERENCES `song_list` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏歌单';
