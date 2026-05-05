package com.blogi.modules.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostSummaryResponse(
    Long id,
    String title,
    String summary,
    String coverUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    PostAuthorResponse author,
    PostCategoryResponse category,
    List<PostTagResponse> tags,
    long commentCount,
    long viewCount,
    long likeCount
) {
}
