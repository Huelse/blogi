package com.blogi.modules.file.storage;

public interface FileStorage {

    StoredFile store(String key, byte[] content, String contentType);
}

