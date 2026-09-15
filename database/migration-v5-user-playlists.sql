USE `musicdemo1`;

ALTER TABLE `song_list`
  ADD COLUMN `owner_user_id` BIGINT DEFAULT NULL AFTER `description`;

ALTER TABLE `song_list`
  ADD CONSTRAINT `fk_song_list_owner_user`
  FOREIGN KEY (`owner_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;
