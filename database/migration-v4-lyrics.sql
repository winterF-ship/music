USE `musicdemo1`;

ALTER TABLE `song`
  ADD COLUMN `lyric_url` VARCHAR(255) DEFAULT NULL AFTER `audio_url`;
