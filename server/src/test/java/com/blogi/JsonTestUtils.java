package com.blogi;

import com.jayway.jsonpath.JsonPath;

final class JsonTestUtils {

    private JsonTestUtils() {
    }

    static <T> T read(String json, String path) {
        return JsonPath.read(json, path);
    }
}
