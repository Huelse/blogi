package com.blogi.modules.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PostUpsertRequest(
    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题不能超过 120 个字符")
    String title,
    @Size(max = 280, message = "摘要不能超过 280 个字符")
    String summary,
    @Size(max = 1024, message = "封面地址不能超过 1024 个字符")
    String coverUrl,
    @NotBlank(message = "正文不能为空")
    @Size(max = 50000, message = "正文不能超过 50000 个字符")
    String contentMarkdown,
    @Size(max = 40, message = "分类不能超过 40 个字符")
    String category,
    @Size(max = 12, message = "标签不能超过 12 个")
    List<String> tags
) {
}
