package com.blogi.modules.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blogi.common.exception.ApiException;
import com.blogi.modules.auth.entity.UserAccount;
import com.blogi.modules.auth.mapper.UserAccountMapper;
import com.blogi.modules.post.dto.PostAuthorResponse;
import com.blogi.modules.post.dto.PostDetailResponse;
import com.blogi.modules.post.dto.PostSummaryResponse;
import com.blogi.modules.post.dto.PostUpsertRequest;
import com.blogi.modules.post.entity.PostArticle;
import com.blogi.modules.post.mapper.PostArticleMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostArticleMapper postArticleMapper;
    private final UserAccountMapper userAccountMapper;

    public PostService(PostArticleMapper postArticleMapper, UserAccountMapper userAccountMapper) {
        this.postArticleMapper = postArticleMapper;
        this.userAccountMapper = userAccountMapper;
    }

    public List<PostSummaryResponse> listPosts() {
        var posts = postArticleMapper.selectList(new LambdaQueryWrapper<PostArticle>()
            .orderByDesc(PostArticle::getCreatedAt));
        return toSummaryResponses(posts);
    }

    public List<PostSummaryResponse> listPostsByAuthor(Long authorId) {
        var posts = postArticleMapper.selectList(new LambdaQueryWrapper<PostArticle>()
            .eq(PostArticle::getAuthorId, authorId)
            .orderByDesc(PostArticle::getCreatedAt));
        return toSummaryResponses(posts);
    }

    public PostDetailResponse getPost(Long postId) {
        return toDetailResponse(getRequiredPost(postId));
    }

    public PostDetailResponse createPost(Long authorId, PostUpsertRequest request) {
        var now = LocalDateTime.now();
        var post = new PostArticle();
        post.setAuthorId(authorId);
        post.setTitle(request.title().trim());
        post.setSummary(normalizeSummary(request.summary(), request.contentMarkdown()));
        post.setContentMarkdown(request.contentMarkdown().trim());
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        postArticleMapper.insert(post);
        return toDetailResponse(post);
    }

    public PostDetailResponse updatePost(Long postId, Long currentUserId, PostUpsertRequest request) {
        var post = getRequiredOwnedPost(postId, currentUserId);
        post.setTitle(request.title().trim());
        post.setSummary(normalizeSummary(request.summary(), request.contentMarkdown()));
        post.setContentMarkdown(request.contentMarkdown().trim());
        post.setUpdatedAt(LocalDateTime.now());
        postArticleMapper.updateById(post);
        return toDetailResponse(post);
    }

    public void deletePost(Long postId, Long currentUserId) {
        var post = getRequiredOwnedPost(postId, currentUserId);
        postArticleMapper.deleteById(post.getId());
    }

    private PostArticle getRequiredPost(Long postId) {
        var post = postArticleMapper.selectById(postId);
        if (post == null) {
            throw new ApiException(404, "文章不存在");
        }
        return post;
    }

    private PostArticle getRequiredOwnedPost(Long postId, Long currentUserId) {
        var post = getRequiredPost(postId);
        if (!Objects.equals(post.getAuthorId(), currentUserId)) {
            throw new ApiException(403, "无权修改该文章");
        }
        return post;
    }

    private List<PostSummaryResponse> toSummaryResponses(List<PostArticle> posts) {
        var usersById = getUsersById(posts);
        return posts.stream()
            .map(post -> new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSummary(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                toAuthor(usersById.get(post.getAuthorId()))
            ))
            .toList();
    }

    private PostDetailResponse toDetailResponse(PostArticle post) {
        var author = userAccountMapper.selectById(post.getAuthorId());
        return new PostDetailResponse(
            post.getId(),
            post.getTitle(),
            post.getSummary(),
            post.getContentMarkdown(),
            post.getCreatedAt(),
            post.getUpdatedAt(),
            toAuthor(author)
        );
    }

    private Map<Long, UserAccount> getUsersById(List<PostArticle> posts) {
        var authorIds = posts.stream()
            .map(PostArticle::getAuthorId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (authorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userAccountMapper.selectBatchIds(authorIds)
            .stream()
            .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
    }

    private PostAuthorResponse toAuthor(UserAccount user) {
        if (user == null) {
            throw new ApiException(404, "作者不存在");
        }
        return new PostAuthorResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }

    private String normalizeSummary(String summary, String contentMarkdown) {
        if (summary != null && !summary.isBlank()) {
            return summary.trim();
        }

        var plainText = contentMarkdown
            .replaceAll("(?m)^#{1,6}\\s*", "")
            .replaceAll("[*_>`#-]", " ")
            .replaceAll("\\[(.*?)]\\((.*?)\\)", "$1")
            .replaceAll("\\s+", " ")
            .trim();

        if (plainText.length() <= 140) {
            return plainText;
        }
        return plainText.substring(0, 137) + "...";
    }
}
