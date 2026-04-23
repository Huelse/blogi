package com.blogi.modules.visitor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blogi.common.exception.ApiException;
import com.blogi.modules.visitor.dto.VisitorProfileRequest;
import com.blogi.modules.visitor.dto.VisitorProfileResponse;
import com.blogi.modules.visitor.dto.VisitorResolveRequest;
import com.blogi.modules.visitor.entity.VisitorIdentity;
import com.blogi.modules.visitor.mapper.VisitorIdentityMapper;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class VisitorService {

    private final VisitorIdentityMapper visitorIdentityMapper;

    public VisitorService(VisitorIdentityMapper visitorIdentityMapper) {
        this.visitorIdentityMapper = visitorIdentityMapper;
    }

    public VisitorProfileResponse resolve(VisitorResolveRequest request) {
        var visitor = findByFingerprint(request.fingerprintHash());
        return visitor == null ? null : toResponse(visitor);
    }

    public VisitorProfileResponse saveProfile(VisitorProfileRequest request) {
        var fingerprintHash = normalizeFingerprint(request.fingerprintHash());
        var displayName = normalizeDisplayName(request.displayName());
        var email = normalizeEmail(request.email());

        var emailOwner = findByEmail(email);
        if (emailOwner != null && !Objects.equals(emailOwner.getFingerprintHash(), fingerprintHash)) {
            throw new ApiException(409, "该邮箱已被使用");
        }

        var now = LocalDateTime.now();
        var visitor = findByFingerprint(fingerprintHash);
        if (visitor == null) {
            visitor = new VisitorIdentity();
            visitor.setFingerprintHash(fingerprintHash);
            visitor.setDisplayName(displayName);
            visitor.setEmail(email);
            visitor.setCreatedAt(now);
            visitor.setUpdatedAt(now);
            visitorIdentityMapper.insert(visitor);
            return toResponse(visitor);
        }

        visitor.setDisplayName(displayName);
        visitor.setEmail(email);
        visitor.setUpdatedAt(now);
        visitorIdentityMapper.updateById(visitor);
        return toResponse(visitor);
    }

    public VisitorIdentity requireVisitor(String fingerprintHash) {
        var visitor = findByFingerprint(fingerprintHash);
        if (visitor == null) {
            throw new ApiException(404, "请先填写昵称和邮箱");
        }
        return visitor;
    }

    public VisitorIdentity findByFingerprint(String fingerprintHash) {
        if (!hasText(fingerprintHash)) {
            return null;
        }
        return visitorIdentityMapper.selectOne(new LambdaQueryWrapper<VisitorIdentity>()
            .eq(VisitorIdentity::getFingerprintHash, normalizeFingerprint(fingerprintHash))
            .last("LIMIT 1"));
    }

    private VisitorIdentity findByEmail(String email) {
        return visitorIdentityMapper.selectOne(new LambdaQueryWrapper<VisitorIdentity>()
            .eq(VisitorIdentity::getEmail, email)
            .last("LIMIT 1"));
    }

    private VisitorProfileResponse toResponse(VisitorIdentity visitor) {
        return new VisitorProfileResponse(
            visitor.getId(),
            visitor.getFingerprintHash(),
            visitor.getDisplayName(),
            visitor.getEmail(),
            visitor.getCreatedAt(),
            visitor.getUpdatedAt()
        );
    }

    private String normalizeFingerprint(String fingerprintHash) {
        return fingerprintHash.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeDisplayName(String displayName) {
        var normalized = displayName.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            throw new ApiException(400, "昵称不能为空");
        }
        return normalized;
    }

    private String normalizeEmail(String email) {
        var normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new ApiException(400, "邮箱不能为空");
        }
        return normalized;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
