package com.blogi.modules.auth.dto;

public record AuthResponse(String token, UserProfileResponse user) {
}
