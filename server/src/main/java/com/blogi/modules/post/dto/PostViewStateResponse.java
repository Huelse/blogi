package com.blogi.modules.post.dto;

public record PostViewStateResponse(
    long viewCount,
    boolean counted
) {
}
