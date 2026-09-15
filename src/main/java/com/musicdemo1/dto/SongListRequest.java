package com.musicdemo1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SongListRequest {
    @NotBlank(message = "歌单名称不能为空")
    @Size(max = 150, message = "歌单名称不能超过 150 个字符")
    private String name;
    @Size(max = 255, message = "封面地址不能超过 255 个字符")
    private String coverUrl;
    @Size(max = 500, message = "歌单简介不能超过 500 个字符")
    private String description;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
