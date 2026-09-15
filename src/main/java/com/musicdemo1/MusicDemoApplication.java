package com.musicdemo1;

import com.musicdemo1.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.musicdemo1.mapper")
@EnableConfigurationProperties(JwtProperties.class)
public class MusicDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicDemoApplication.class, args);
    }
}
