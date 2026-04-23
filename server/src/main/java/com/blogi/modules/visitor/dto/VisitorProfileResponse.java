package com.blogi.modules.visitor.dto;

import java.time.LocalDateTime;

public record VisitorProfileResponse(
    Long id,
    String fingerprintHash,
    String displayName,
    String email,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
