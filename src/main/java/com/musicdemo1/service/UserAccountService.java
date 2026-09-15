package com.musicdemo1.service;

import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.UserProfileUpdateRequest;
import com.musicdemo1.dto.UserView;
import com.musicdemo1.entity.MusicUser;
import com.musicdemo1.mapper.MusicUserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserAccountService {
    private final MusicUserMapper userMapper;

    public UserAccountService(MusicUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserView profile(Long userId) {
        return UserView.from(requireUser(userId));
    }

    public UserView update(Long userId, UserProfileUpdateRequest request) {
        MusicUser user = requireUser(userId);
        user.setNickname(request.getNickname().trim());
        user.setProfile(blankToNull(request.getProfile()));
        userMapper.updateById(user);
        return UserView.from(user);
    }

    public UserView updateAvatar(Long userId, String avatarUrl) {
        MusicUser user = requireUser(userId);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
        return UserView.from(user);
    }

    private MusicUser requireUser(Long userId) {
        MusicUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (!Integer.valueOf(1).equals(user.getStatus())) throw new BusinessException(403, "该账号已被停用");
        return user;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
