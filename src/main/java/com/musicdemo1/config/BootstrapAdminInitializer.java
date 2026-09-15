package com.musicdemo1.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.musicdemo1.entity.Admin;
import com.musicdemo1.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapAdminInitializer {
    @Bean
    ApplicationRunner createBootstrapAdmin(AdminMapper adminMapper, PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.username}") String username,
            @Value("${app.bootstrap-admin.password}") String password) {
        return args -> {
            Long count = adminMapper.selectCount(Wrappers.<Admin>lambdaQuery().eq(Admin::getUsername, username));
            if (count != null && count == 0) {
                Admin admin = new Admin();
                admin.setUsername(username);
                admin.setPassword(passwordEncoder.encode(password));
                admin.setNickname("系统管理员");
                adminMapper.insert(admin);
            }
        };
    }
}
