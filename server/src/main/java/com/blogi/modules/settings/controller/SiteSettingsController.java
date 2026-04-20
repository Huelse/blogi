package com.blogi.modules.settings.controller;

import com.blogi.common.api.ApiResponse;
import com.blogi.modules.settings.dto.BlogSettingsRequest;
import com.blogi.modules.settings.dto.BlogSettingsResponse;
import com.blogi.modules.settings.service.SiteSettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SiteSettingsController {

    private final SiteSettingsService siteSettingsService;

    public SiteSettingsController(SiteSettingsService siteSettingsService) {
        this.siteSettingsService = siteSettingsService;
    }

    @GetMapping
    public ApiResponse<BlogSettingsResponse> getSettings() {
        return ApiResponse.ok(siteSettingsService.getSettings());
    }

    @PutMapping
    public ApiResponse<BlogSettingsResponse> updateSettings(@Valid @RequestBody BlogSettingsRequest request) {
        return ApiResponse.ok(siteSettingsService.updateSettings(request));
    }
}
