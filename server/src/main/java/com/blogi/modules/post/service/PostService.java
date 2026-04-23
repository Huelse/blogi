package com.blogi.modules.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blogi.common.exception.ApiException;
import com.blogi.modules.auth.entity.UserAccount;
import com.blogi.modules.auth.mapper.UserAccountMapper;
import com.blogi.modules.post.dto.PostAuthorResponse;
import com.blogi.modules.post.dto.PostCategoryResponse;
import com.blogi.modules.post.dto.PostCommentRequest;
import com.blogi.modules.post.dto.PostCommentResponse;
import com.blogi.modules.post.dto.PostDetailResponse;
import com.blogi.modules.post.dto.PostLikeRequest;
import com.blogi.modules.post.dto.PostLikeStateResponse;
import com.blogi.modules.post.dto.PostSummaryResponse;
import com.blogi.modules.post.dto.PostTagResponse;
import com.blogi.modules.post.dto.PostUpsertRequest;
import com.blogi.modules.post.entity.PostArticle;
import com.blogi.modules.post.entity.PostCategory;
import com.blogi.modules.post.entity.PostComment;
import com.blogi.modules.post.entity.PostLike;
import com.blogi.modules.post.entity.PostTag;
import com.blogi.modules.post.entity.PostTagRelation;
import com.blogi.modules.post.mapper.PostArticleMapper;
import com.blogi.modules.post.mapper.PostCategoryMapper;
import com.blogi.modules.post.mapper.PostCommentMapper;
import com.blogi.modules.post.mapper.PostLikeMapper;
import com.blogi.modules.post.mapper.PostTagMapper;
import com.blogi.modules.post.mapper.PostTagRelationMapper;
import com.blogi.modules.visitor.entity.VisitorIdentity;
import com.blogi.modules.visitor.mapper.VisitorIdentityMapper;
import com.blogi.modules.visitor.service.VisitorService;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private static final int MAX_TAGS_PER_POST = 12;

    private final PostArticleMapper postArticleMapper;
    private final UserAccountMapper userAccountMapper;
    private final PostCategoryMapper postCategoryMapper;
    private final PostTagMapper postTagMapper;
    private final PostTagRelationMapper postTagRelationMapper;
    private final PostCommentMapper postCommentMapper;
    private final PostLikeMapper postLikeMapper;
    private final VisitorIdentityMapper visitorIdentityMapper;
    private final VisitorService visitorService;

    public PostService(
        PostArticleMapper postArticleMapper,
        UserAccountMapper userAccountMapper,
        PostCategoryMapper postCategoryMapper,
        PostTagMapper postTagMapper,
        PostTagRelationMapper postTagRelationMapper,
        PostCommentMapper postCommentMapper,
        PostLikeMapper postLikeMapper,
        VisitorIdentityMapper visitorIdentityMapper,
        VisitorService visitorService
    ) {
        this.postArticleMapper = postArticleMapper;
        this.userAccountMapper = userAccountMapper;
        this.postCategoryMapper = postCategoryMapper;
        this.postTagMapper = postTagMapper;
        this.postTagRelationMapper = postTagRelationMapper;
        this.postCommentMapper = postCommentMapper;
        this.postLikeMapper = postLikeMapper;
        this.visitorIdentityMapper = visitorIdentityMapper;
        this.visitorService = visitorService;
    }

    public List<PostSummaryResponse> listPosts(String categorySlug, String tagSlug) {
        var query = new LambdaQueryWrapper<PostArticle>()
            .orderByDesc(PostArticle::getCreatedAt);

        var category = findCategoryBySlug(categorySlug);
        if (hasText(categorySlug)) {
            if (category == null) {
                return List.of();
            }
            query.eq(PostArticle::getCategoryId, category.getId());
        }

        var tag = findTagBySlug(tagSlug);
        if (hasText(tagSlug)) {
            if (tag == null) {
                return List.of();
            }

            var postIds = postTagRelationMapper.selectList(new LambdaQueryWrapper<PostTagRelation>()
                    .eq(PostTagRelation::getTagId, tag.getId()))
                .stream()
                .map(PostTagRelation::getPostId)
                .distinct()
                .toList();
            if (postIds.isEmpty()) {
                return List.of();
            }
            query.in(PostArticle::getId, postIds);
        }

        return toSummaryResponses(postArticleMapper.selectList(query));
    }

    public List<PostSummaryResponse> listPostsByAuthor(Long authorId) {
        var posts = postArticleMapper.selectList(new LambdaQueryWrapper<PostArticle>()
            .eq(PostArticle::getAuthorId, authorId)
            .orderByDesc(PostArticle::getCreatedAt));
        return toSummaryResponses(posts);
    }

    public List<PostCategoryResponse> listCategories() {
        return postCategoryMapper.selectList(new LambdaQueryWrapper<PostCategory>()
                .orderByAsc(PostCategory::getName))
            .stream()
            .map(this::toCategory)
            .toList();
    }

    public List<PostTagResponse> listTags() {
        return postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                .orderByAsc(PostTag::getName))
            .stream()
            .map(this::toTag)
            .toList();
    }

    public PostDetailResponse getPost(Long postId) {
        return toDetailResponse(getRequiredPost(postId));
    }

    public PostDetailResponse createPost(Long authorId, PostUpsertRequest request) {
        var now = LocalDateTime.now();
        var category = findOrCreateCategory(request.category());
        var tags = findOrCreateTags(request.tags());
        var post = new PostArticle();
        post.setAuthorId(authorId);
        post.setCategoryId(category == null ? null : category.getId());
        post.setTitle(request.title().trim());
        post.setSummary(normalizeSummary(request.summary(), request.contentMarkdown()));
        post.setContentMarkdown(request.contentMarkdown().trim());
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        postArticleMapper.insert(post);
        replacePostTags(post.getId(), tags);
        return toDetailResponse(post);
    }

    public PostDetailResponse updatePost(Long postId, Long currentUserId, PostUpsertRequest request) {
        var post = getRequiredOwnedPost(postId, currentUserId);
        var category = findOrCreateCategory(request.category());
        var tags = findOrCreateTags(request.tags());
        post.setCategoryId(category == null ? null : category.getId());
        post.setTitle(request.title().trim());
        post.setSummary(normalizeSummary(request.summary(), request.contentMarkdown()));
        post.setContentMarkdown(request.contentMarkdown().trim());
        post.setUpdatedAt(LocalDateTime.now());
        postArticleMapper.updateById(post);
        replacePostTags(post.getId(), tags);
        return toDetailResponse(post);
    }

    public void deletePost(Long postId, Long currentUserId) {
        var post = getRequiredOwnedPost(postId, currentUserId);
        postArticleMapper.deleteById(post.getId());
    }

    public List<PostCommentResponse> listComments(Long postId) {
        getRequiredPost(postId);
        var comments = postCommentMapper.selectList(new LambdaQueryWrapper<PostComment>()
            .eq(PostComment::getPostId, postId)
            .orderByAsc(PostComment::getCreatedAt));
        return toCommentResponses(comments);
    }

    public PostCommentResponse createComment(Long postId, PostCommentRequest request) {
        getRequiredPost(postId);
        var visitor = visitorService.requireVisitor(request.fingerprintHash());
        var now = LocalDateTime.now();
        var comment = new PostComment();
        comment.setPostId(postId);
        comment.setVisitorId(visitor.getId());
        comment.setContent(request.content().trim());
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);
        postCommentMapper.insert(comment);
        return toCommentResponse(comment, null, visitor);
    }

    public PostLikeStateResponse getLikeState(Long postId, String fingerprintHash) {
        getRequiredPost(postId);
        var visitor = visitorService.findByFingerprint(fingerprintHash);
        return toLikeState(postId, visitor);
    }

    public PostLikeStateResponse likePost(Long postId, PostLikeRequest request) {
        getRequiredPost(postId);
        var visitor = visitorService.requireVisitor(request.fingerprintHash());
        if (!hasLike(postId, visitor.getId())) {
            var like = new PostLike();
            like.setPostId(postId);
            like.setVisitorId(visitor.getId());
            like.setCreatedAt(LocalDateTime.now());
            postLikeMapper.insert(like);
        }
        return toLikeState(postId, visitor);
    }

    public PostLikeStateResponse unlikePost(Long postId, PostLikeRequest request) {
        getRequiredPost(postId);
        var visitor = visitorService.requireVisitor(request.fingerprintHash());
        postLikeMapper.delete(new LambdaQueryWrapper<PostLike>()
            .eq(PostLike::getPostId, postId)
            .eq(PostLike::getVisitorId, visitor.getId()));
        return toLikeState(postId, visitor);
    }

    public void deleteComment(Long postId, Long commentId, Long currentUserId) {
        var post = getRequiredPost(postId);
        var comment = postCommentMapper.selectById(commentId);
        if (comment == null || !Objects.equals(comment.getPostId(), postId)) {
            throw new ApiException(404, "评论不存在");
        }

        var isCommentAuthor = Objects.equals(comment.getAuthorId(), currentUserId);
        var isPostAuthor = Objects.equals(post.getAuthorId(), currentUserId);
        if (!isCommentAuthor && !isPostAuthor) {
            throw new ApiException(403, "无权删除该评论");
        }

        postCommentMapper.deleteById(commentId);
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
        if (posts.isEmpty()) {
            return List.of();
        }

        var usersById = getUsersById(posts);
        var categoriesById = getCategoriesById(posts);
        var tagsByPostId = getTagsByPostIds(posts);
        var commentCountsByPostId = getCommentCountsByPostIds(posts);
        var likeCountsByPostId = getLikeCountsByPostIds(posts);
        return posts.stream()
            .map(post -> new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSummary(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                toAuthor(usersById.get(post.getAuthorId())),
                toCategory(categoriesById.get(post.getCategoryId())),
                tagsByPostId.getOrDefault(post.getId(), List.of()),
                commentCountsByPostId.getOrDefault(post.getId(), 0L),
                likeCountsByPostId.getOrDefault(post.getId(), 0L)
            ))
            .toList();
    }

    private PostDetailResponse toDetailResponse(PostArticle post) {
        var author = userAccountMapper.selectById(post.getAuthorId());
        var category = post.getCategoryId() == null ? null : postCategoryMapper.selectById(post.getCategoryId());
        return new PostDetailResponse(
            post.getId(),
            post.getTitle(),
            post.getSummary(),
            post.getContentMarkdown(),
            post.getCreatedAt(),
            post.getUpdatedAt(),
            toAuthor(author),
            toCategory(category),
            getTagsByPostId(post.getId()),
            postCommentMapper.selectCount(new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, post.getId())),
            getLikeCount(post.getId())
        );
    }

    private List<PostCommentResponse> toCommentResponses(List<PostComment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }

        var usersById = getUsersByIdFromComments(comments);
        var visitorsById = getVisitorsByIdFromComments(comments);
        return comments.stream()
            .map(comment -> toCommentResponse(
                comment,
                usersById.get(comment.getAuthorId()),
                visitorsById.get(comment.getVisitorId())
            ))
            .toList();
    }

    private PostCommentResponse toCommentResponse(PostComment comment, UserAccount author, VisitorIdentity visitor) {
        return new PostCommentResponse(
            comment.getId(),
            comment.getPostId(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getUpdatedAt(),
            toCommentAuthor(author, visitor)
        );
    }

    private Map<Long, UserAccount> getUsersById(List<PostArticle> posts) {
        var authorIds = posts.stream()
            .map(PostArticle::getAuthorId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        return getUsersByAuthorIds(authorIds);
    }

    private Map<Long, UserAccount> getUsersByIdFromComments(List<PostComment> comments) {
        var authorIds = comments.stream()
            .map(PostComment::getAuthorId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        return getUsersByAuthorIds(authorIds);
    }

    private Map<Long, UserAccount> getUsersByAuthorIds(List<Long> authorIds) {
        if (authorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userAccountMapper.selectBatchIds(authorIds)
            .stream()
            .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
    }

    private Map<Long, VisitorIdentity> getVisitorsByIdFromComments(List<PostComment> comments) {
        var visitorIds = comments.stream()
            .map(PostComment::getVisitorId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (visitorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return visitorIdentityMapper.selectBatchIds(visitorIds)
            .stream()
            .collect(Collectors.toMap(VisitorIdentity::getId, Function.identity()));
    }

    private Map<Long, PostCategory> getCategoriesById(List<PostArticle> posts) {
        var categoryIds = posts.stream()
            .map(PostArticle::getCategoryId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return postCategoryMapper.selectBatchIds(categoryIds)
            .stream()
            .collect(Collectors.toMap(PostCategory::getId, Function.identity()));
    }

    private List<PostTagResponse> getTagsByPostId(Long postId) {
        var post = new PostArticle();
        post.setId(postId);
        return getTagsByPostIds(List.of(post)).getOrDefault(postId, List.of());
    }

    private Map<Long, List<PostTagResponse>> getTagsByPostIds(List<PostArticle> posts) {
        var postIds = posts.stream()
            .map(PostArticle::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        var relations = postTagRelationMapper.selectList(new LambdaQueryWrapper<PostTagRelation>()
            .in(PostTagRelation::getPostId, postIds)
            .orderByAsc(PostTagRelation::getPostId)
            .orderByAsc(PostTagRelation::getSortOrder)
            .orderByAsc(PostTagRelation::getTagId));
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }

        var tagIds = relations.stream()
            .map(PostTagRelation::getTagId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        var tagsById = postTagMapper.selectBatchIds(tagIds)
            .stream()
            .collect(Collectors.toMap(PostTag::getId, Function.identity()));

        var tagsByPostId = new LinkedHashMap<Long, List<PostTagResponse>>();
        for (var relation : relations) {
            var tag = tagsById.get(relation.getTagId());
            if (tag == null) {
                continue;
            }
            tagsByPostId.computeIfAbsent(relation.getPostId(), ignored -> new ArrayList<>())
                .add(toTag(tag));
        }
        return tagsByPostId;
    }

    private Map<Long, Long> getCommentCountsByPostIds(List<PostArticle> posts) {
        var postIds = posts.stream()
            .map(PostArticle::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return postCommentMapper.selectList(new LambdaQueryWrapper<PostComment>()
                .select(PostComment::getPostId)
                .in(PostComment::getPostId, postIds))
            .stream()
            .collect(Collectors.groupingBy(PostComment::getPostId, Collectors.counting()));
    }

    private Map<Long, Long> getLikeCountsByPostIds(List<PostArticle> posts) {
        var postIds = posts.stream()
            .map(PostArticle::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return postLikeMapper.selectList(new LambdaQueryWrapper<PostLike>()
                .select(PostLike::getPostId)
                .in(PostLike::getPostId, postIds))
            .stream()
            .collect(Collectors.groupingBy(PostLike::getPostId, Collectors.counting()));
    }

    private PostLikeStateResponse toLikeState(Long postId, VisitorIdentity visitor) {
        return new PostLikeStateResponse(
            getLikeCount(postId),
            visitor != null && hasLike(postId, visitor.getId())
        );
    }

    private long getLikeCount(Long postId) {
        return postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
            .eq(PostLike::getPostId, postId));
    }

    private boolean hasLike(Long postId, Long visitorId) {
        return postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
            .eq(PostLike::getPostId, postId)
            .eq(PostLike::getVisitorId, visitorId)) > 0;
    }

    private PostAuthorResponse toAuthor(UserAccount user) {
        if (user == null) {
            throw new ApiException(404, "作者不存在");
        }
        return new PostAuthorResponse(user.getId(), user.getUsername(), user.getDisplayName());
    }

    private PostAuthorResponse toCommentAuthor(UserAccount user, VisitorIdentity visitor) {
        if (user != null) {
            return toAuthor(user);
        }
        if (visitor != null) {
            return new PostAuthorResponse(visitor.getId(), "visitor-" + visitor.getId(), visitor.getDisplayName());
        }
        throw new ApiException(404, "评论作者不存在");
    }

    private PostCategoryResponse toCategory(PostCategory category) {
        if (category == null) {
            return null;
        }
        return new PostCategoryResponse(category.getId(), category.getName(), category.getSlug());
    }

    private PostTagResponse toTag(PostTag tag) {
        return new PostTagResponse(tag.getId(), tag.getName(), tag.getSlug());
    }

    private PostCategory findOrCreateCategory(String rawName) {
        var name = normalizeOptionalName(rawName);
        if (name == null) {
            return null;
        }
        if (name.length() > 40) {
            throw new ApiException(400, "分类不能超过 40 个字符");
        }

        var existing = postCategoryMapper.findByNameIgnoreCase(name);
        if (existing != null) {
            return existing;
        }

        var now = LocalDateTime.now();
        var category = new PostCategory();
        category.setName(name);
        category.setSlug(nextCategorySlug(name));
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        postCategoryMapper.insert(category);
        return category;
    }

    private List<PostTag> findOrCreateTags(List<String> rawTags) {
        var names = normalizeTagNames(rawTags);
        if (names.isEmpty()) {
            return List.of();
        }

        var tags = new ArrayList<PostTag>();
        for (var name : names) {
            var existing = postTagMapper.findByNameIgnoreCase(name);
            if (existing != null) {
                tags.add(existing);
                continue;
            }

            var now = LocalDateTime.now();
            var tag = new PostTag();
            tag.setName(name);
            tag.setSlug(nextTagSlug(name));
            tag.setCreatedAt(now);
            tag.setUpdatedAt(now);
            postTagMapper.insert(tag);
            tags.add(tag);
        }
        return tags;
    }

    private void replacePostTags(Long postId, List<PostTag> tags) {
        postTagRelationMapper.delete(new LambdaQueryWrapper<PostTagRelation>()
            .eq(PostTagRelation::getPostId, postId));

        for (var index = 0; index < tags.size(); index++) {
            var relation = new PostTagRelation();
            relation.setPostId(postId);
            relation.setTagId(tags.get(index).getId());
            relation.setSortOrder(index);
            postTagRelationMapper.insert(relation);
        }
    }

    private List<String> normalizeTagNames(List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return List.of();
        }

        var namesByKey = new LinkedHashMap<String, String>();
        for (var rawTag : rawTags) {
            var name = normalizeOptionalName(rawTag);
            if (name == null) {
                continue;
            }
            if (name.length() > 32) {
                throw new ApiException(400, "标签不能超过 32 个字符");
            }
            namesByKey.putIfAbsent(name.toLowerCase(Locale.ROOT), name);
        }

        if (namesByKey.size() > MAX_TAGS_PER_POST) {
            throw new ApiException(400, "每篇文章最多设置 12 个标签");
        }
        return List.copyOf(namesByKey.values());
    }

    private PostCategory findCategoryBySlug(String slug) {
        if (!hasText(slug)) {
            return null;
        }
        return postCategoryMapper.selectOne(new LambdaQueryWrapper<PostCategory>()
            .eq(PostCategory::getSlug, slug.trim())
            .last("LIMIT 1"));
    }

    private PostTag findTagBySlug(String slug) {
        if (!hasText(slug)) {
            return null;
        }
        return postTagMapper.selectOne(new LambdaQueryWrapper<PostTag>()
            .eq(PostTag::getSlug, slug.trim())
            .last("LIMIT 1"));
    }

    private String nextCategorySlug(String name) {
        var baseSlug = slugBase(name, "category");
        var slug = baseSlug;
        var suffix = 2;
        while (postCategoryMapper.selectCount(new LambdaQueryWrapper<PostCategory>()
                .eq(PostCategory::getSlug, slug)) > 0) {
            slug = withSuffix(baseSlug, suffix++);
        }
        return slug;
    }

    private String nextTagSlug(String name) {
        var baseSlug = slugBase(name, "tag");
        var slug = baseSlug;
        var suffix = 2;
        while (postTagMapper.selectCount(new LambdaQueryWrapper<PostTag>()
                .eq(PostTag::getSlug, slug)) > 0) {
            slug = withSuffix(baseSlug, suffix++);
        }
        return slug;
    }

    private String slugBase(String name, String fallbackPrefix) {
        var normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^\\p{L}\\p{N}]+", "-")
            .replaceAll("(^-+|-+$)", "");
        if (normalized.isBlank()) {
            normalized = fallbackPrefix + "-" + Integer.toUnsignedString(name.hashCode(), 36);
        }
        if (normalized.length() <= 72) {
            return normalized;
        }
        return normalized.substring(0, 72).replaceAll("-+$", "");
    }

    private String withSuffix(String baseSlug, int suffix) {
        var suffixText = "-" + suffix;
        var maxBaseLength = 80 - suffixText.length();
        var trimmedBase = baseSlug.length() <= maxBaseLength
            ? baseSlug
            : baseSlug.substring(0, maxBaseLength).replaceAll("-+$", "");
        return trimmedBase + suffixText;
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

    private String normalizeOptionalName(String rawName) {
        if (rawName == null) {
            return null;
        }
        var name = rawName.replaceAll("\\s+", " ").trim();
        return name.isEmpty() ? null : name;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
