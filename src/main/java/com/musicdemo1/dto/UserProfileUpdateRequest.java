package com.musicdemo1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称不能超过 50 个字符")
    private String nickname;

    @Size(max = 500, message = "个人简介不能超过 500 个字符")
    private String profile;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
}
