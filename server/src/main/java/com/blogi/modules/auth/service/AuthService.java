package com.blogi.modules.auth.service;

import com.blogi.common.exception.ApiException;
import com.blogi.modules.auth.dto.AuthResponse;
import com.blogi.modules.auth.dto.LoginRequest;
import com.blogi.modules.auth.dto.RegisterRequest;
import com.blogi.modules.auth.dto.UserProfileResponse;
import com.blogi.modules.auth.entity.UserAccount;
import com.blogi.modules.auth.mapper.UserAccountMapper;
import com.blogi.security.JwtService;
import com.blogi.security.UserPrincipal;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserAccountMapper userAccountMapper,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        var username = request.username().trim();
        if (userAccountMapper.findByUsername(username) != null) {
            throw new ApiException(409, "用户名已存在");
        }

        var now = LocalDateTime.now();
        var user = new UserAccount();
        user.setUsername(username);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userAccountMapper.insert(user);

        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        var user = userAccountMapper.findByUsername(request.username().trim());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(401, "用户名或密码错误");
        }
        return toAuthResponse(user);
    }

    public UserProfileResponse getCurrentUser(UserPrincipal principal) {
        if (principal == null || principal.id() == null) {
            throw new ApiException(401, "未登录");
        }
        return new UserProfileResponse(principal.id(), principal.username(), principal.displayName());
    }

    public UserAccount getRequiredUser(Long userId) {
        var user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new ApiException(401, "用户不存在");
        }
        return user;
    }

    private AuthResponse toAuthResponse(UserAccount user) {
        var token = jwtService.generateToken(user);
        return new AuthResponse(token, toUserProfile(user));
    }

    public static UserProfileResponse toUserProfile(UserAccount user) {
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
