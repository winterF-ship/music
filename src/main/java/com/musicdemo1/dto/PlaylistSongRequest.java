package com.musicdemo1.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PlaylistSongRequest {
    @NotNull(message = "请选择歌曲")
    @Positive(message = "歌曲编号不合法")
    private Long songId;
    private Integer sortNo;
    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
}
