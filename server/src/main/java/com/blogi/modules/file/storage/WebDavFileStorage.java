package com.blogi.modules.file.storage;

import com.blogi.common.exception.ApiException;
import com.blogi.modules.file.config.UploadProperties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.springframework.util.StringUtils;

public class WebDavFileStorage implements FileStorage {

    private final HttpClient httpClient;
    private final UploadProperties.WebDav properties;

    public WebDavFileStorage(UploadProperties.WebDav properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        if (!StringUtils.hasText(properties.getBaseUrl())) {
            throw new ApiException(500, "文件上传配置缺失: webdav.baseUrl");
        }
    }

    @Override
    public StoredFile store(String key, byte[] content, String contentType) {
        var fullKey = prefixedKey(key);
        ensureCollections(fullKey);
        var uploadUri = URI.create(joinUrl(properties.getBaseUrl(), fullKey));
        var request = withAuth(HttpRequest.newBuilder(uploadUri))
            .header("Content-Type", contentType)
            .PUT(HttpRequest.BodyPublishers.ofByteArray(content))
            .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(502, "WebDAV 文件上传失败");
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(502, "WebDAV 文件上传失败");
        }

        return new StoredFile(fullKey, resolvePublicUrl(fullKey));
    }

    private void ensureCollections(String fullKey) {
        var segments = fullKey.split("/");
        if (segments.length <= 1) {
            return;
        }

        var builder = new StringBuilder();
        for (var index = 0; index < segments.length - 1; index++) {
            var segment = segments[index];
            if (segment.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(segment);

            var collectionUri = URI.create(joinUrl(properties.getBaseUrl(), builder + "/"));
            var request = withAuth(HttpRequest.newBuilder(collectionUri))
                .method("MKCOL", HttpRequest.BodyPublishers.noBody())
                .build();

            try {
                var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                var status = response.statusCode();
                if (!(status == 201 || status == 301 || status == 405 || status == 409)) {
                    throw new ApiException(502, "WebDAV 目录创建失败");
                }
            } catch (ApiException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new ApiException(502, "WebDAV 目录创建失败");
            }
        }
    }

    private HttpRequest.Builder withAuth(HttpRequest.Builder builder) {
        if (!StringUtils.hasText(properties.getUsername())) {
            return builder;
        }
        var token = properties.getUsername() + ":" + properties.getPassword();
        var encoded = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        return builder.header("Authorization", "Basic " + encoded);
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
        return joinUrl(properties.getBaseUrl(), fullKey);
    }

    private String joinUrl(String baseUrl, String path) {
        return trimTrailingSlash(baseUrl) + "/" + trimLeadingSlash(path);
    }

    private String trimTrailingSlash(String value) {
        var normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String trimLeadingSlash(String value) {
        var normalized = value;
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private String trimSlashes(String value) {
        return trimTrailingSlash(trimLeadingSlash(value));
    }
}

