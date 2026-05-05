package com.blogi.modules.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
    Long id,
    String title,
    String summary,
    String coverUrl,
    String contentMarkdown,
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
