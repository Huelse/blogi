package com.blogi.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 40, message = "昵称长度需为 2-40 位")
    String displayName,

    @Size(max = 1024, message = "头像地址不能超过 1024 个字符")
    String avatarUrl
) {
}

