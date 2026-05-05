package com.blogi.modules.file.controller;

import com.blogi.common.api.ApiResponse;
import com.blogi.common.exception.ApiException;
import com.blogi.modules.file.dto.UploadResponse;
import com.blogi.modules.file.dto.UploadUsage;
import com.blogi.modules.file.service.FileUploadService;
import com.blogi.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Pattern FINGERPRINT_PATTERN = Pattern.compile("^[a-f0-9]{64}$");

    private final FileUploadService fileUploadService;

    public FileController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponse> upload(
        @RequestParam UploadUsage usage,
        @RequestParam("file") MultipartFile file,
        @RequestParam(required = false) String fingerprintHash,
        HttpServletRequest servletRequest,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        validateUsagePermission(usage, fingerprintHash, principal);
        var uploaded = fileUploadService.upload(file, usage);
        return ApiResponse.ok(toAbsoluteUrl(uploaded, servletRequest));
    }

    private void validateUsagePermission(UploadUsage usage, String fingerprintHash, UserPrincipal principal) {
        if (usage == UploadUsage.VISITOR_AVATAR) {
            if (fingerprintHash == null || !FINGERPRINT_PATTERN.matcher(fingerprintHash).matches()) {
                throw new ApiException(400, "访客头像上传需要有效的浏览器指纹");
            }
            return;
        }

        if (principal == null || principal.id() == null) {
            throw new ApiException(401, "未登录");
        }
    }

    private UploadResponse toAbsoluteUrl(UploadResponse uploaded, HttpServletRequest request) {
        var url = uploaded.url();
        if (url == null || !url.startsWith("/")) {
            return uploaded;
        }

        var port = request.getServerPort();
        var includePort = !("http".equalsIgnoreCase(request.getScheme()) && port == 80)
            && !("https".equalsIgnoreCase(request.getScheme()) && port == 443);
        var origin = request.getScheme() + "://" + request.getServerName() + (includePort ? ":" + port : "");
        return new UploadResponse(uploaded.key(), origin + url, uploaded.contentType(), uploaded.size());
    }
}
