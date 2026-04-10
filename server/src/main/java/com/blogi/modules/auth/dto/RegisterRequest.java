package com.blogi.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度需为 3-32 位")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名仅支持字母、数字、下划线和连字符")
    String username,
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 40, message = "昵称长度需为 2-40 位")
    String displayName,
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 72, message = "密码长度需为 6-72 位")
    String password
) {
}
