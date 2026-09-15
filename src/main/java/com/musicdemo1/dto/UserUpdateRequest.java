package com.musicdemo1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称不能超过 50 个字符")
    private String nickname;
    @Size(max = 255, message = "头像地址不能超过 255 个字符")
    private String avatar;
    @Size(max = 500, message = "个人简介不能超过 500 个字符")
    private String profile;
    @NotNull(message = "账号状态不能为空")
    private Integer status;
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
