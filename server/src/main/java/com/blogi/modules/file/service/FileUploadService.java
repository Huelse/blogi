package com.blogi.modules.file.service;

import com.blogi.common.exception.ApiException;
import com.blogi.modules.file.config.UploadProperties;
import com.blogi.modules.file.dto.UploadResponse;
import com.blogi.modules.file.dto.UploadUsage;
import com.blogi.modules.file.storage.FileStorage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

    private final FileStorage fileStorage;
    private final UploadProperties properties;

    public FileUploadService(FileStorage fileStorage, UploadProperties properties) {
        this.fileStorage = fileStorage;
        this.properties = properties;
    }

    public UploadResponse upload(MultipartFile file, UploadUsage usage) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(400, "文件不能为空");
        }
        if (file.getSize() > properties.getMaxSizeBytes()) {
            throw new ApiException(400, "文件大小超出限制");
        }

        var contentType = normalizeContentType(file.getContentType());
        if (!allowedContentTypes().contains(contentType)) {
            throw new ApiException(400, "不支持的文件类型");
        }

        var extension = resolveExtension(file.getOriginalFilename(), contentType);
        var now = LocalDate.now();
        var key = usage.folder()
            + "/" + now.getYear()
            + "/" + String.format("%02d", now.getMonthValue())
            + "/" + UUID.randomUUID().toString().replace("-", "")
            + extension;

        try {
            var storedFile = fileStorage.store(key, file.getBytes(), contentType);
            return new UploadResponse(storedFile.key(), storedFile.url(), contentType, file.getSize());
        } catch (IOException exception) {
            throw new ApiException(500, "读取上传文件失败");
        }
    }

    private Set<String> allowedContentTypes() {
        var configuredTypes = properties.getAllowedContentTypes();
        if (configuredTypes == null || configuredTypes.isEmpty()) {
            configuredTypes = java.util.List.of("image/jpeg", "image/png", "image/webp", "image/gif");
        }

        return configuredTypes.stream()
            .map(this::normalizeContentType)
            .collect(java.util.stream.Collectors.toSet());
    }

    private String normalizeContentType(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (originalFilename != null) {
            var lastDot = originalFilename.lastIndexOf('.');
            if (lastDot > -1 && lastDot < originalFilename.length() - 1) {
                var ext = originalFilename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
                if (ext.matches("[a-z0-9]{1,10}")) {
                    return "." + ext;
                }
            }
        }

        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}
