package com.blogi.modules.visitor.controller;

import com.blogi.common.api.ApiResponse;
import com.blogi.modules.visitor.dto.VisitorProfileRequest;
import com.blogi.modules.visitor.dto.VisitorProfileResponse;
import com.blogi.modules.visitor.dto.VisitorResolveRequest;
import com.blogi.modules.visitor.service.VisitorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @PostMapping("/resolve")
    public ApiResponse<VisitorProfileResponse> resolve(@Valid @RequestBody VisitorResolveRequest request) {
        return ApiResponse.ok(visitorService.resolve(request));
    }

    @PutMapping("/profile")
    public ApiResponse<VisitorProfileResponse> saveProfile(@Valid @RequestBody VisitorProfileRequest request) {
        return ApiResponse.ok(visitorService.saveProfile(request));
    }
}
