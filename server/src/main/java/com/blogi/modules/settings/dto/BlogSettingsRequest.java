package com.blogi.modules.settings.dto;

import jakarta.validation.constraints.Size;

public record BlogSettingsRequest(
    @Size(max = 20000, message = "页脚 HTML 不能超过 20000 个字符")
    String footerHtml
) {
}
