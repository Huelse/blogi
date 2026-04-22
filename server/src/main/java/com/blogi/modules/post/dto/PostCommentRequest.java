package com.blogi.modules.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCommentRequest(
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1200, message = "评论内容不能超过 1200 个字符")
    String content
) {
}
