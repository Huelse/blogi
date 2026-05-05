package com.blogi.modules.file.dto;

public record UploadResponse(
    String key,
    String url,
    String contentType,
    long size
) {
}

