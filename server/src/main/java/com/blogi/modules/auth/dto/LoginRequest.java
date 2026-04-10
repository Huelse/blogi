package com.blogi.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度需为 3-32 位")
    String username,
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 72, message = "密码长度需为 6-72 位")
    String password
) {
}
