package com.blogi.modules.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostSummaryGenerateRequest(
    @Size(max = 120, message = "标题不能超过 120 个字符")
    String title,
    @NotBlank(message = "正文不能为空")
    @Size(max = 50000, message = "正文不能超过 50000 个字符")
    String contentMarkdown
) {
}
