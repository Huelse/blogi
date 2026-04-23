package com.blogi.modules.visitor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VisitorProfileRequest(
    @NotBlank(message = "浏览器指纹不能为空")
    @Pattern(regexp = "^[a-f0-9]{64}$", message = "浏览器指纹格式不正确")
    String fingerprintHash,

    @NotBlank(message = "昵称不能为空")
    @Size(max = 40, message = "昵称不能超过 40 个字符")
    String displayName,

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 254, message = "邮箱不能超过 254 个字符")
    String email
) {
}
