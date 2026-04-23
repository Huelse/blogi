package com.blogi.modules.post.controller;

import com.blogi.common.api.ApiResponse;
import com.blogi.common.exception.ApiException;
import com.blogi.modules.post.dto.PostCategoryResponse;
import com.blogi.modules.post.dto.PostCommentRequest;
import com.blogi.modules.post.dto.PostCommentResponse;
import com.blogi.modules.post.dto.PostDetailResponse;
import com.blogi.modules.post.dto.PostLikeRequest;
import com.blogi.modules.post.dto.PostLikeStateResponse;
import com.blogi.modules.post.dto.PostSummaryResponse;
import com.blogi.modules.post.dto.PostTagResponse;
import com.blogi.modules.post.dto.PostUpsertRequest;
import com.blogi.modules.post.service.PostService;
import com.blogi.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ApiResponse<List<PostSummaryResponse>> listPosts(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String tag
    ) {
        return ApiResponse.ok(postService.listPosts(category, tag));
    }

    @GetMapping("/categories")
    public ApiResponse<List<PostCategoryResponse>> listCategories() {
        return ApiResponse.ok(postService.listCategories());
    }

    @GetMapping("/tags")
    public ApiResponse<List<PostTagResponse>> listTags() {
        return ApiResponse.ok(postService.listTags());
    }

    @GetMapping("/mine")
    public ApiResponse<List<PostSummaryResponse>> listOwnPosts(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(postService.listPostsByAuthor(requireUserId(principal)));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> getPost(@PathVariable Long postId) {
        return ApiResponse.ok(postService.getPost(postId));
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<List<PostCommentResponse>> listComments(@PathVariable Long postId) {
        return ApiResponse.ok(postService.listComments(postId));
    }

    @PostMapping("/{postId}/comments")
    public ApiResponse<PostCommentResponse> createComment(
        @PathVariable Long postId,
        @Valid @RequestBody PostCommentRequest request
    ) {
        return ApiResponse.ok(postService.createComment(postId, request));
    }

    @GetMapping("/{postId}/likes")
    public ApiResponse<PostLikeStateResponse> getLikeState(
        @PathVariable Long postId,
        @RequestParam(required = false) String fingerprintHash
    ) {
        return ApiResponse.ok(postService.getLikeState(postId, fingerprintHash));
    }

    @PostMapping("/{postId}/likes")
    public ApiResponse<PostLikeStateResponse> likePost(
        @PathVariable Long postId,
        @Valid @RequestBody PostLikeRequest request
    ) {
        return ApiResponse.ok(postService.likePost(postId, request));
    }

    @DeleteMapping("/{postId}/likes")
    public ApiResponse<PostLikeStateResponse> unlikePost(
        @PathVariable Long postId,
        @Valid @RequestBody PostLikeRequest request
    ) {
        return ApiResponse.ok(postService.unlikePost(postId, request));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
        @PathVariable Long postId,
        @PathVariable Long commentId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        postService.deleteComment(postId, commentId, requireUserId(principal));
        return ApiResponse.ok(null);
    }

    @PostMapping
    public ApiResponse<PostDetailResponse> createPost(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody PostUpsertRequest request
    ) {
        return ApiResponse.ok(postService.createPost(requireUserId(principal), request));
    }

    @PutMapping("/{postId}")
    public ApiResponse<PostDetailResponse> updatePost(
        @PathVariable Long postId,
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody PostUpsertRequest request
    ) {
        return ApiResponse.ok(postService.updatePost(postId, requireUserId(principal), request));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
        @PathVariable Long postId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        postService.deletePost(postId, requireUserId(principal));
        return ApiResponse.ok(null);
    }

    private Long requireUserId(UserPrincipal principal) {
        if (principal == null || principal.id() == null) {
            throw new ApiException(401, "未登录");
        }
        return principal.id();
    }
}
