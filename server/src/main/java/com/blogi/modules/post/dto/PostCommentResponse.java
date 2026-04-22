package com.blogi.modules.post.dto;

import java.time.LocalDateTime;

public record PostCommentResponse(
    Long id,
    Long postId,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    PostAuthorResponse author
) {
}
