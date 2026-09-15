package com.musicdemo1.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.AuthResponse;
import com.musicdemo1.dto.LoginRequest;
import com.musicdemo1.entity.Admin;
import com.musicdemo1.mapper.AdminMapper;
import com.musicdemo1.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthService(AdminMapper adminMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        Admin admin = adminMapper.selectOne(Wrappers.<Admin>lambdaQuery()
                .eq(Admin::getUsername, request.getUsername()));
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(401, "管理员账号或密码错误");
        }
        return new AuthResponse(admin.getId(), admin.getUsername(), admin.getNickname(), "ADMIN",
                jwtService.createToken(admin.getId(), admin.getUsername(), "ADMIN"));
    }
}
