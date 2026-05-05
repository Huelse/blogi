package com.blogi.modules.post.service;

import com.blogi.modules.post.dto.PostViewStateResponse;
import com.blogi.modules.post.entity.PostArticle;
import com.blogi.modules.post.mapper.PostArticleMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PostStatsService {

    private static final String LIKE_COUNT_KEY_PREFIX = "post:stats:likes:count:";
    private static final String VIEW_COUNT_KEY_PREFIX = "post:stats:views:count:";
    private static final String VIEW_PENDING_KEY_PREFIX = "post:stats:views:pending:";
    private static final String VIEW_DEDUP_KEY_PREFIX = "post:stats:views:dedup:";

    private static final Duration LIKE_COUNT_CACHE_TTL = Duration.ofMinutes(20);
    private static final Duration VIEW_COUNT_CACHE_TTL = Duration.ofMinutes(20);
    private static final Duration VIEW_PENDING_TTL = Duration.ofHours(6);
    private static final Duration VIEW_DEDUP_WINDOW = Duration.ofMinutes(10);
    private static final long VIEW_FLUSH_BATCH_SIZE = 20L;
    private static final int LOCAL_DEDUP_MAX_SIZE = 20000;

    private final PostArticleMapper postArticleMapper;
    private final StringRedisTemplate redisTemplate;
    private final ConcurrentMap<String, Instant> localViewDedupExpiry = new ConcurrentHashMap<>();

    public PostStatsService(
        PostArticleMapper postArticleMapper,
        ObjectProvider<StringRedisTemplate> redisTemplateProvider
    ) {
        this.postArticleMapper = postArticleMapper;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
    }

    public Map<Long, Long> resolveLikeCounts(List<Long> postIds, Map<Long, Long> persistedCounts) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        var resolved = new LinkedHashMap<Long, Long>();
        for (var postId : postIds) {
            var persistedCount = persistedCounts.getOrDefault(postId, 0L);
            resolved.put(postId, resolveLikeCount(postId, persistedCount));
        }
        return resolved;
    }

    public long resolveLikeCount(Long postId, long persistedCount) {
        var cacheKey = likeCountKey(postId);
        var cached = getCachedLong(cacheKey);
        if (cached != null) {
            return cached;
        }

        cacheLong(cacheKey, persistedCount, LIKE_COUNT_CACHE_TTL);
        return persistedCount;
    }

    public void evictLikeCount(Long postId) {
        deleteKey(likeCountKey(postId));
    }

    public Map<Long, Long> resolveViewCounts(List<PostArticle> posts) {
        if (posts.isEmpty()) {
            return Map.of();
        }

        var resolved = new LinkedHashMap<Long, Long>();
        for (var post : posts) {
            var persistedCount = post.getViewCount() == null ? 0L : post.getViewCount();
            resolved.put(post.getId(), resolveViewCount(post.getId(), persistedCount));
        }
        return resolved;
    }

    public long resolveViewCount(Long postId, long persistedCount) {
        var cacheKey = viewCountKey(postId);
        var cached = getCachedLong(cacheKey);
        if (cached != null) {
            return cached;
        }

        var pendingDelta = parseLongOrZero(getCachedValue(viewPendingKey(postId)));
        var resolved = persistedCount + Math.max(0L, pendingDelta);
        cacheLong(cacheKey, resolved, VIEW_COUNT_CACHE_TTL);
        return resolved;
    }

    public PostViewStateResponse trackView(Long postId, long persistedCount, String fingerprintHash) {
        var counted = shouldCountView(postId, fingerprintHash);
        if (counted) {
            var incrementedInRedis = incrementViewCountInRedis(postId, persistedCount);
            if (!incrementedInRedis) {
                postArticleMapper.incrementViewCount(postId, 1L);
                persistedCount += 1L;
            }
        }
        return new PostViewStateResponse(resolveViewCount(postId, persistedCount), counted);
    }

    @Scheduled(fixedDelayString = "${blogi.post-stats.view-flush-interval-ms:60000}")
    public void flushPendingViewCounts() {
        if (redisTemplate == null) {
            return;
        }

        try {
            var keys = redisTemplate.keys(VIEW_PENDING_KEY_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }

            for (var key : keys) {
                var postId = parsePostIdFromPendingKey(key);
                if (postId == null) {
                    continue;
                }
                flushPendingViewCount(postId);
            }
        } catch (Exception ignored) {
        }
    }

    private boolean incrementViewCountInRedis(Long postId, long persistedCount) {
        if (redisTemplate == null) {
            return false;
        }

        try {
            var valueOps = redisTemplate.opsForValue();
            var pendingKey = viewPendingKey(postId);
            var viewCountCacheKey = viewCountKey(postId);

            var pendingBefore = parseLongOrZero(valueOps.get(pendingKey));
            var seed = persistedCount + Math.max(0L, pendingBefore);
            valueOps.setIfAbsent(viewCountCacheKey, Long.toString(seed), VIEW_COUNT_CACHE_TTL);

            valueOps.increment(viewCountCacheKey);
            redisTemplate.expire(viewCountCacheKey, VIEW_COUNT_CACHE_TTL);

            var pendingNow = valueOps.increment(pendingKey);
            redisTemplate.expire(pendingKey, VIEW_PENDING_TTL);
            if (pendingNow != null && pendingNow >= VIEW_FLUSH_BATCH_SIZE) {
                flushPendingViewCount(postId);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void flushPendingViewCount(Long postId) {
        if (redisTemplate == null) {
            return;
        }

        try {
            var deltaText = redisTemplate.opsForValue().getAndDelete(viewPendingKey(postId));
            var delta = parseLongOrZero(deltaText);
            if (delta <= 0) {
                return;
            }
            postArticleMapper.incrementViewCount(postId, delta);
        } catch (Exception ignored) {
        }
    }

    private boolean shouldCountView(Long postId, String fingerprintHash) {
        var key = viewDedupKey(postId, fingerprintHash);
        if (redisTemplate != null) {
            try {
                var created = redisTemplate.opsForValue().setIfAbsent(key, "1", VIEW_DEDUP_WINDOW);
                if (created != null) {
                    return created;
                }
            } catch (Exception ignored) {
            }
        }
        return shouldCountViewLocally(key);
    }

    private boolean shouldCountViewLocally(String key) {
        var now = Instant.now();
        var expiresAt = now.plus(VIEW_DEDUP_WINDOW);

        while (true) {
            var current = localViewDedupExpiry.get(key);
            if (current == null) {
                if (localViewDedupExpiry.putIfAbsent(key, expiresAt) == null) {
                    pruneLocalViewDedup(now);
                    return true;
                }
                continue;
            }
            if (current.isBefore(now)) {
                if (localViewDedupExpiry.replace(key, current, expiresAt)) {
                    pruneLocalViewDedup(now);
                    return true;
                }
                continue;
            }
            return false;
        }
    }

    private void pruneLocalViewDedup(Instant now) {
        if (localViewDedupExpiry.size() < LOCAL_DEDUP_MAX_SIZE) {
            return;
        }
        for (var entry : localViewDedupExpiry.entrySet()) {
            if (entry.getValue().isBefore(now)) {
                localViewDedupExpiry.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    private String likeCountKey(Long postId) {
        return LIKE_COUNT_KEY_PREFIX + postId;
    }

    private String viewCountKey(Long postId) {
        return VIEW_COUNT_KEY_PREFIX + postId;
    }

    private String viewPendingKey(Long postId) {
        return VIEW_PENDING_KEY_PREFIX + postId;
    }

    private String viewDedupKey(Long postId, String fingerprintHash) {
        return VIEW_DEDUP_KEY_PREFIX + postId + ":" + fingerprintHash;
    }

    private Long parsePostIdFromPendingKey(String pendingKey) {
        if (!StringUtils.hasText(pendingKey) || !pendingKey.startsWith(VIEW_PENDING_KEY_PREFIX)) {
            return null;
        }
        var rawPostId = pendingKey.substring(VIEW_PENDING_KEY_PREFIX.length());
        try {
            return Long.parseLong(rawPostId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Long getCachedLong(String key) {
        var value = getCachedValue(key);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String getCachedValue(String key) {
        if (redisTemplate == null) {
            return null;
        }

        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void cacheLong(String key, long value, Duration ttl) {
        if (redisTemplate == null) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(key, Long.toString(value), ttl);
        } catch (Exception ignored) {
        }
    }

    private void deleteKey(String key) {
        if (redisTemplate == null) {
            return;
        }

        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
        }
    }

    private long parseLongOrZero(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return 0L;
        }
        try {
            return Long.parseLong(rawValue);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}
