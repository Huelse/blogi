package com.blogi.modules.post.dto;

import java.time.LocalDateTime;

public record PostSummaryResponse(
    Long id,
    String title,
    String summary,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    PostAuthorResponse author
) {
}
