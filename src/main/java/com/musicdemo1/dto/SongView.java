package com.musicdemo1.dto;

public class SongView {
    private Long id;
    private String title;
    private Long singerId;
    private String singerName;
    private String coverUrl;
    private String audioUrl;
    private String lyricUrl;
    private Integer duration;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getSingerId() { return singerId; }
    public void setSingerId(Long singerId) { this.singerId = singerId; }
    public String getSingerName() { return singerName; }
    public void setSingerName(String singerName) { this.singerName = singerName; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public String getLyricUrl() { return lyricUrl; }
    public void setLyricUrl(String lyricUrl) { this.lyricUrl = lyricUrl; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}
