package com.blogi.config;

import com.blogi.common.api.ApiResponse;
import com.blogi.modules.file.config.UploadProperties;
import com.blogi.modules.file.config.UploadStorageType;
import com.blogi.security.JwtAuthenticationFilter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        UploadProperties uploadProperties,
        AuthenticationEntryPoint authenticationEntryPoint,
        AccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .anonymous(AbstractHttpConfigurer::disable)
            .cors(cors -> {
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/api/health", "/api/auth/login", "/api/auth/register").permitAll();
                auth.requestMatchers("/api/visitors/**").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/api/files/upload").permitAll();
                var localPublicAssetPath = localPublicAssetPath(uploadProperties);
                if (localPublicAssetPath != null) {
                    auth.requestMatchers(HttpMethod.GET, localPublicAssetPath).permitAll();
                }
                auth.requestMatchers("/api/auth/me").authenticated();
                auth.requestMatchers(HttpMethod.GET, "/api/settings").permitAll();
                auth.requestMatchers("/api/posts/mine").authenticated();
                auth.requestMatchers(HttpMethod.GET, "/api/posts").permitAll();
                auth.requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/api/posts/*/comments").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/api/posts/*/likes").permitAll();
                auth.requestMatchers(HttpMethod.DELETE, "/api/posts/*/likes").permitAll();
                auth.anyRequest().authenticated();
            })
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(toJson(ApiResponse.error("Unauthorized")));
        };
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(toJson(ApiResponse.error("Forbidden")));
        };
    }

    private String toJson(ApiResponse<Void> response) {
        return """
            {"success":%s,"data":null,"message":"%s"}
            """.formatted(response.success(), response.message());
    }

    private String localPublicAssetPath(UploadProperties uploadProperties) {
        if (uploadProperties.getStorage() != UploadStorageType.LOCAL) {
            return null;
        }

        var configuredPath = uploadProperties.getLocal().getPublicPath();
        if (!StringUtils.hasText(configuredPath)) {
            return "/uploads/**";
        }

        var normalized = configuredPath.trim();
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return null;
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized + "/**";
    }
}
