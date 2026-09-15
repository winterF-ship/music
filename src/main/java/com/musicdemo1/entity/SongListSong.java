package com.musicdemo1.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("song_list_song")
public class SongListSong {
    private Long songListId;
    private Long songId;
    private Integer sortNo;
    private LocalDateTime createdAt;
    public Long getSongListId() { return songListId; }
    public void setSongListId(Long songListId) { this.songListId = songListId; }
    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
