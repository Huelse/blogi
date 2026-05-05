package com.blogi.modules.post.dto;

public record PostSummaryGenerateResponse(
    String summary,
    boolean generatedByAi
) {
}
