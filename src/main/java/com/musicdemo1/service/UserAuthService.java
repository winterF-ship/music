package com.musicdemo1.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.AuthResponse;
import com.musicdemo1.dto.LoginRequest;
import com.musicdemo1.dto.RegisterRequest;
import com.musicdemo1.entity.MusicUser;
import com.musicdemo1.mapper.MusicUserMapper;
import com.musicdemo1.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {
    private final MusicUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserAuthService(MusicUserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        Long count = userMapper.selectCount(Wrappers.<MusicUser>lambdaQuery()
                .eq(MusicUser::getUsername, request.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("该用户名已被注册");
        }
        MusicUser user = new MusicUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(isBlank(request.getNickname()) ? request.getUsername() : request.getNickname().trim());
        user.setStatus(1);
        userMapper.insert(user);
        return response(user);
    }

    public AuthResponse login(LoginRequest request) {
        MusicUser user = userMapper.selectOne(Wrappers.<MusicUser>lambdaQuery()
                .eq(MusicUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(403, "该账号已被停用");
        }
        return response(user);
    }

    private AuthResponse response(MusicUser user) {
        return new AuthResponse(user.getId(), user.getUsername(), user.getNickname(), "USER",
                jwtService.createToken(user.getId(), user.getUsername(), "USER"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
