package com.blogi.modules.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostSummaryResponse(
    Long id,
    String title,
    String summary,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    PostAuthorResponse author,
    PostCategoryResponse category,
    List<PostTagResponse> tags,
    long commentCount
) {
}
