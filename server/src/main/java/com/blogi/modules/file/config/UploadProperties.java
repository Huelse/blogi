package com.blogi.modules.file.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blogi.upload")
public class UploadProperties {

    private UploadStorageType storage = UploadStorageType.LOCAL;
    private long maxSizeBytes = 5 * 1024 * 1024;
    private List<String> allowedContentTypes = List.of(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif"
    );
    private final Local local = new Local();
    private final S3 s3 = new S3();
    private final WebDav webdav = new WebDav();

    public UploadStorageType getStorage() {
        return storage;
    }

    public void setStorage(UploadStorageType storage) {
        this.storage = storage;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }

    public Local getLocal() {
        return local;
    }

    public S3 getS3() {
        return s3;
    }

    public WebDav getWebdav() {
        return webdav;
    }

    public static class Local {

        private String rootDir = "./data/uploads";
        private String publicPath = "/uploads";

        public String getRootDir() {
            return rootDir;
        }

        public void setRootDir(String rootDir) {
            this.rootDir = rootDir;
        }

        public String getPublicPath() {
            return publicPath;
        }

        public void setPublicPath(String publicPath) {
            this.publicPath = publicPath;
        }
    }

    public static class S3 {

        private String endpoint = "";
        private String region = "us-east-1";
        private String bucket = "";
        private String accessKey = "";
        private String secretKey = "";
        private String prefix = "blogi";
        private String publicBaseUrl = "";
        private boolean pathStyleAccess = true;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
        }

        public boolean isPathStyleAccess() {
            return pathStyleAccess;
        }

        public void setPathStyleAccess(boolean pathStyleAccess) {
            this.pathStyleAccess = pathStyleAccess;
        }
    }

    public static class WebDav {

        private String baseUrl = "";
        private String username = "";
        private String password = "";
        private String prefix = "blogi";
        private String publicBaseUrl = "";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
        }
    }
}

