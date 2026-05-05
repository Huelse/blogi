package com.blogi.modules.file.storage;

import com.blogi.common.exception.ApiException;
import com.blogi.modules.file.config.UploadProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.util.StringUtils;

public class LocalFileStorage implements FileStorage {

    private final Path rootDir;
    private final String publicPath;

    public LocalFileStorage(UploadProperties properties) {
        this.rootDir = Path.of(properties.getLocal().getRootDir())
            .toAbsolutePath()
            .normalize();
        this.publicPath = normalizePublicPath(properties.getLocal().getPublicPath());
    }

    @Override
    public StoredFile store(String key, byte[] content, String contentType) {
        var normalizedKey = key.replace('\\', '/');
        var target = rootDir.resolve(normalizedKey).normalize();
        if (!target.startsWith(rootDir)) {
            throw new ApiException(400, "文件路径非法");
        }

        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException exception) {
            throw new ApiException(500, "文件保存失败");
        }

        return new StoredFile(normalizedKey, publicPath + "/" + normalizedKey);
    }

    private String normalizePublicPath(String value) {
        if (!StringUtils.hasText(value)) {
            return "/uploads";
        }
        var normalized = value.trim();
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
