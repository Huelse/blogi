package com.blogi.modules.file.dto;

public enum UploadUsage {
    POST_COVER("post-covers"),
    USER_AVATAR("user-avatars"),
    VISITOR_AVATAR("visitor-avatars");

    private final String folder;

    UploadUsage(String folder) {
        this.folder = folder;
    }

    public String folder() {
        return folder;
    }
}

