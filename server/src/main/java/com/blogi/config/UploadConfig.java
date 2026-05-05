package com.blogi.config;

import com.blogi.modules.file.config.UploadProperties;
import com.blogi.modules.file.config.UploadStorageType;
import com.blogi.modules.file.storage.FileStorage;
import com.blogi.modules.file.storage.LocalFileStorage;
import com.blogi.modules.file.storage.S3CompatibleFileStorage;
import com.blogi.modules.file.storage.WebDavFileStorage;
import java.net.URI;
import java.nio.file.Path;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
@EnableConfigurationProperties(UploadProperties.class)
public class UploadConfig implements WebMvcConfigurer {

    private final UploadProperties uploadProperties;

    public UploadConfig(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Bean
    FileStorage fileStorage() {
        var storageType = uploadProperties.getStorage();
        if (storageType == UploadStorageType.LOCAL) {
            return new LocalFileStorage(uploadProperties);
        }
        if (storageType == UploadStorageType.WEBDAV) {
            return new WebDavFileStorage(uploadProperties.getWebdav());
        }
        return new S3CompatibleFileStorage(buildS3Client(), storageType, uploadProperties.getS3());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (uploadProperties.getStorage() != UploadStorageType.LOCAL) {
            return;
        }

        var configuredPath = uploadProperties.getLocal().getPublicPath();
        if (configuredPath != null
            && (configuredPath.startsWith("http://") || configuredPath.startsWith("https://"))) {
            return;
        }

        var publicPath = normalizePublicPath(configuredPath);
        var rootDir = Path.of(uploadProperties.getLocal().getRootDir())
            .toAbsolutePath()
            .normalize()
            .toUri()
            .toString();
        registry.addResourceHandler(publicPath + "/**")
            .addResourceLocations(rootDir.endsWith("/") ? rootDir : rootDir + "/");
    }

    private S3Client buildS3Client() {
        var s3 = uploadProperties.getS3();
        var builder = S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())
            ))
            .region(Region.of(s3.getRegion()))
            .serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(s3.isPathStyleAccess())
                .build());

        if (StringUtils.hasText(s3.getEndpoint())) {
            builder.endpointOverride(URI.create(s3.getEndpoint().trim()));
        }
        return builder.build();
    }

    private String normalizePublicPath(String value) {
        if (!StringUtils.hasText(value)) {
            return "/uploads";
        }
        var normalized = value.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
