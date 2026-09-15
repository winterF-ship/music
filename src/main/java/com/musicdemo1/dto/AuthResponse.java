package com.musicdemo1.dto;

public record AuthResponse(Long id, String username, String nickname, String role, String token) {
}
