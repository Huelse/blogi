package com.blogi.modules.post.dto;

import java.time.LocalDateTime;

public record PostDetailResponse(
    Long id,
    String title,
    String summary,
    String contentMarkdown,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    PostAuthorResponse author
) {
}
