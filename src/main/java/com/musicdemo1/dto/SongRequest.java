package com.musicdemo1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SongRequest {
    @NotBlank(message = "歌曲名称不能为空")
    @Size(max = 150, message = "歌曲名称不能超过 150 个字符")
    private String title;
    @NotNull(message = "请选择歌手")
    @Positive(message = "歌手编号不合法")
    private Long singerId;
    @Size(max = 255, message = "封面地址不能超过 255 个字符")
    private String coverUrl;
    @NotBlank(message = "请先上传音频文件")
    @Size(max = 255, message = "音频地址不能超过 255 个字符")
    private String audioUrl;
    @Size(max = 255, message = "歌词地址不能超过 255 个字符")
    private String lyricUrl;
    @Positive(message = "时长必须为正数")
    private Integer durationSeconds;
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getSingerId() { return singerId; }
    public void setSingerId(Long singerId) { this.singerId = singerId; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public String getLyricUrl() { return lyricUrl; }
    public void setLyricUrl(String lyricUrl) { this.lyricUrl = lyricUrl; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
}
