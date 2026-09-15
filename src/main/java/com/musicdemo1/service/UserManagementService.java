package com.musicdemo1.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.dto.UserUpdateRequest;
import com.musicdemo1.dto.UserView;
import com.musicdemo1.entity.MusicUser;
import com.musicdemo1.mapper.MusicUserMapper;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementService {
    private final MusicUserMapper userMapper;

    public UserManagementService(MusicUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public PageResponse<UserView> page(long current, long size, String keyword) {
        Page<MusicUser> page = new Page<>(Math.max(current, 1), Math.min(Math.max(size, 1), 100));
        String normalizedKeyword = blankToNull(keyword);
        userMapper.selectPage(page, Wrappers.<MusicUser>lambdaQuery()
                .and(normalizedKeyword != null, query -> query
                        .like(MusicUser::getUsername, normalizedKeyword)
                        .or().like(MusicUser::getNickname, normalizedKeyword))
                .orderByDesc(MusicUser::getCreatedAt).orderByDesc(MusicUser::getId));
        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(),
                page.getRecords().stream().map(UserView::from).toList());
    }

    public UserView get(Long id) {
        MusicUser user = requireUser(id);
        return UserView.from(user);
    }

    public UserView update(Long id, UserUpdateRequest request) {
        if (!Objects.equals(request.getStatus(), 0) && !Objects.equals(request.getStatus(), 1)) {
            throw new BusinessException("账号状态只能为 0 或 1");
        }
        MusicUser user = requireUser(id);
        user.setNickname(request.getNickname().trim());
        user.setAvatar(blankToNull(request.getAvatar()));
        user.setProfile(blankToNull(request.getProfile()));
        user.setStatus(request.getStatus());
        userMapper.updateById(user);
        return UserView.from(user);
    }

    public UserView updateStatus(Long id, Integer status) {
        if (!Objects.equals(status, 0) && !Objects.equals(status, 1)) {
            throw new BusinessException("账号状态只能为 0 或 1");
        }
        MusicUser user = requireUser(id);
        user.setStatus(status);
        userMapper.updateById(user);
        return UserView.from(user);
    }

    @Transactional
    public void delete(Long id) {
        if (userMapper.deleteById(id) == 0) {
            throw new BusinessException(404, "用户不存在");
        }
    }

    private MusicUser requireUser(Long id) {
        MusicUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(404, "用户不存在");
        return user;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
