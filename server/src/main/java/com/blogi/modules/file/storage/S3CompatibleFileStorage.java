package com.blogi.modules.file.storage;

import com.blogi.common.exception.ApiException;
import com.blogi.modules.file.config.UploadProperties;
import com.blogi.modules.file.config.UploadStorageType;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.util.StringUtils;

public class S3CompatibleFileStorage implements FileStorage {

    private final S3Client s3Client;
    private final UploadStorageType storageType;
    private final UploadProperties.S3 properties;

    public S3CompatibleFileStorage(
        S3Client s3Client,
        UploadStorageType storageType,
        UploadProperties.S3 properties
    ) {
        this.s3Client = s3Client;
        this.storageType = storageType;
        this.properties = properties;

        if (!StringUtils.hasText(properties.getBucket())) {
            throw new ApiException(500, "文件上传配置缺失: bucket");
        }
        if (!StringUtils.hasText(properties.getAccessKey()) || !StringUtils.hasText(properties.getSecretKey())) {
            throw new ApiException(500, "文件上传配置缺失: accessKey/secretKey");
        }
        if ((storageType == UploadStorageType.OSS || storageType == UploadStorageType.COS)
            && !StringUtils.hasText(properties.getEndpoint())) {
            throw new ApiException(500, "文件上传配置缺失: endpoint");
        }
    }

    @Override
    public StoredFile store(String key, byte[] content, String contentType) {
        var fullKey = prefixedKey(key);
        try {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(fullKey)
                    .contentType(contentType)
                    .build(),
                RequestBody.fromBytes(content)
            );
        } catch (Exception exception) {
            throw new ApiException(502, "远程文件存储失败");
        }

        return new StoredFile(fullKey, resolvePublicUrl(fullKey));
    }

    private String prefixedKey(String key) {
        if (!StringUtils.hasText(properties.getPrefix())) {
            return key;
        }
        return trimSlashes(properties.getPrefix()) + "/" + key;
    }

    private String resolvePublicUrl(String fullKey) {
        if (StringUtils.hasText(properties.getPublicBaseUrl())) {
            return joinUrl(properties.getPublicBaseUrl(), fullKey);
        }

        if (StringUtils.hasText(properties.getEndpoint())) {
            var endpoint = trimTrailingSlash(properties.getEndpoint());
            if (properties.isPathStyleAccess()) {
                return endpoint + "/" + properties.getBucket() + "/" + fullKey;
            }
            return endpoint + "/" + fullKey;
        }

        return "https://" + properties.getBucket()
            + ".s3." + properties.getRegion() + ".amazonaws.com/" + fullKey;
    }

    private String trimTrailingSlash(String value) {
        var normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String trimSlashes(String value) {
        var normalized = value.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String joinUrl(String baseUrl, String path) {
        return trimTrailingSlash(baseUrl) + "/" + path;
    }
}

