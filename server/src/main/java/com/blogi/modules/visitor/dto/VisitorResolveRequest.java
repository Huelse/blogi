package com.blogi.modules.visitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VisitorResolveRequest(
    @NotBlank(message = "浏览器指纹不能为空")
    @Pattern(regexp = "^[a-f0-9]{64}$", message = "浏览器指纹格式不正确")
    String fingerprintHash
) {
}
