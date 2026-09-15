package com.musicdemo1.dto;

import com.musicdemo1.entity.MusicUser;

public record UserView(Long id, String username, String nickname, String avatar, String profile, Integer status) {
    public static UserView from(MusicUser user) {
        return new UserView(user.getId(), user.getUsername(), user.getNickname(), user.getAvatar(),
                user.getProfile(), user.getStatus());
    }
}
