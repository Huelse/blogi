package com.blogi.modules.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpsertRequest(
    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题不能超过 120 个字符")
    String title,
    @Size(max = 280, message = "摘要不能超过 280 个字符")
    String summary,
    @NotBlank(message = "正文不能为空")
    @Size(max = 50000, message = "正文不能超过 50000 个字符")
    String contentMarkdown
) {
}
