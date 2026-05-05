package com.blogi.modules.auth.dto;

public record UserProfileResponse(Long id, String username, String displayName, String avatarUrl) {
}
