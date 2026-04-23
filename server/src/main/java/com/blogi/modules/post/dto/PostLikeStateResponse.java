package com.blogi.modules.post.dto;

public record PostLikeStateResponse(
    long likeCount,
    boolean liked
) {
}
