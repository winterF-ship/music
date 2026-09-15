package com.musicdemo1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SingerRequest {
    @NotBlank(message = "歌手姓名不能为空")
    @Size(max = 100, message = "歌手姓名不能超过 100 个字符")
    private String name;
    @Size(max = 255, message = "头像地址不能超过 255 个字符")
    private String avatarUrl;
    @Size(max = 1000, message = "歌手简介不能超过 1000 个字符")
    private String introduction;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
}
