package com.blogi.modules.post.service;

import com.blogi.modules.post.config.PostAiProperties;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class PostAiSummaryService {

    private static final String SYSTEM_PROMPT = """
        你是专业中文编辑。请为博客文章生成 1 段摘要：
        1) 长度控制在 80-220 个中文字符
        2) 客观准确，避免夸张语气
        3) 不要输出 Markdown、标题或前缀，只返回摘要正文
        """;

    private final PostAiProperties properties;
    private final JsonParser jsonParser = JsonParserFactory.getJsonParser();

    public PostAiSummaryService(PostAiProperties properties) {
        this.properties = properties;
    }

    public Optional<String> generateSummary(String title, String contentMarkdown) {
        if (!isReady()) {
            return Optional.empty();
        }

        try {
            var response = requestSummary(title, contentMarkdown);
            var summary = sanitizeSummary(extractSummary(response));
            if (!StringUtils.hasText(summary)) {
                return Optional.empty();
            }
            return Optional.of(clipSummary(summary, properties.getSummaryMaxChars()));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String requestSummary(String title, String contentMarkdown) {
        var payload = Map.of(
            "model", properties.getModel().trim(),
            "messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", buildUserPrompt(title, contentMarkdown))
            ),
            "temperature", 0.3,
            "max_tokens", 220
        );

        var client = RestClient.builder().baseUrl(normalizeBaseUrl(properties.getBaseUrl())).build();
        return client.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(headers -> headers.setBearerAuth(properties.getApiKey().trim()))
            .body(payload)
            .retrieve()
            .body(String.class);
    }

    private String extractSummary(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "";
        }
        var parsed = jsonParser.parseMap(responseBody);
        var choices = parsed.get("choices");
        if (!(choices instanceof List<?> choiceList) || choiceList.isEmpty()) {
            return "";
        }

        var firstChoice = choiceList.get(0);
        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            return "";
        }

        var message = choiceMap.get("message");
        if (!(message instanceof Map<?, ?> messageMap)) {
            return "";
        }

        var content = messageMap.get("content");
        return content == null ? "" : content.toString();
    }

    private String sanitizeSummary(String rawSummary) {
        if (!StringUtils.hasText(rawSummary)) {
            return "";
        }
        var normalized = rawSummary
            .replaceAll("^\"+|\"+$", "")
            .replaceAll("\\s+", " ")
            .trim();
        if (normalized.startsWith("摘要：")) {
            normalized = normalized.substring(3).trim();
        }
        if (normalized.startsWith("Summary:")) {
            normalized = normalized.substring("Summary:".length()).trim();
        }
        return normalized;
    }

    private String clipSummary(String summary, int configuredMaxChars) {
        var maxChars = Math.max(80, Math.min(configuredMaxChars, 280));
        if (summary.length() <= maxChars) {
            return summary;
        }
        return summary.substring(0, maxChars - 3).trim() + "...";
    }

    private String buildUserPrompt(String title, String contentMarkdown) {
        var normalizedTitle = title == null ? "" : title.trim();
        return """
            标题：
            %s

            正文（Markdown）：
            %s
            """.formatted(normalizedTitle, contentMarkdown);
    }

    private boolean isReady() {
        return properties.isEnabled()
            && StringUtils.hasText(properties.getApiKey())
            && StringUtils.hasText(properties.getModel())
            && StringUtils.hasText(properties.getBaseUrl());
    }

    private String normalizeBaseUrl(String value) {
        var baseUrl = value.trim();
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
