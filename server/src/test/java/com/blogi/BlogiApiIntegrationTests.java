package com.blogi;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class BlogiApiIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        clearDatabase();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
    }

    private void clearDatabase() {
        jdbcTemplate.execute("DELETE FROM post_likes");
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM post_tags");
        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM categories");
        jdbcTemplate.execute("DELETE FROM visitors");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM site_settings");
    }

    @Test
    void userCanRegisterLoginAndManageOwnPost() throws Exception {
        var registerResponse = mockMvc.perform(post("/api/auth/register")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "username": "alice",
                      "displayName": "Alice",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.user.username", is("alice")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        var token = JsonTestUtils.read(registerResponse, "$.data.token");

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username", is("alice")))
            .andExpect(jsonPath("$.data.displayName", is("Alice")));

        mockMvc.perform(post("/api/auth/login")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "username": "alice",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.user.displayName", is("Alice")));

        var createResponse = mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "title": "First Post",
                      "summary": "Initial summary",
                      "contentMarkdown": "# Hello\\n\\nThis is my first post."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title", is("First Post")))
            .andExpect(jsonPath("$.data.author.username", is("alice")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        var postId = JsonTestUtils.read(createResponse, "$.data.id");

        mockMvc.perform(get("/api/posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].title", is("First Post")));

        mockMvc.perform(get("/api/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.contentMarkdown", is("# Hello\n\nThis is my first post.")));

        mockMvc.perform(put("/api/posts/" + postId)
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "title": "Updated Post",
                      "summary": "",
                      "contentMarkdown": "## Updated\\n\\nBody after edit."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title", is("Updated Post")))
            .andExpect(jsonPath("$.data.summary", is("Updated Body after edit.")));

        mockMvc.perform(get("/api/posts/mine")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].title", is("Updated Post")));

        mockMvc.perform(delete("/api/posts/" + postId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));

        mockMvc.perform(get("/api/posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void userCanCategorizeTagCommentAndLikePost() throws Exception {
        var writerResponse = mockMvc.perform(post("/api/auth/register")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "username": "techwriter",
                      "displayName": "Tech Writer",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        var writerToken = JsonTestUtils.read(writerResponse, "$.data.token");

        var createResponse = mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + writerToken)
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "title": "Taxonomy Post",
                      "summary": "Post with category and tags",
                      "contentMarkdown": "# Taxonomy\\n\\nPost body.",
                      "category": "Engineering",
                      "tags": ["Spring Boot", "Vue", "vue"]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.category.name", is("Engineering")))
            .andExpect(jsonPath("$.data.category.slug", is("engineering")))
            .andExpect(jsonPath("$.data.tags", hasSize(2)))
            .andExpect(jsonPath("$.data.tags[0].name", is("Spring Boot")))
            .andExpect(jsonPath("$.data.tags[1].name", is("Vue")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        var postId = JsonTestUtils.read(createResponse, "$.data.id");

        mockMvc.perform(get("/api/posts/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.slug == 'engineering')]", hasSize(1)));

        mockMvc.perform(get("/api/posts/tags"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.slug == 'spring-boot')]", hasSize(1)))
            .andExpect(jsonPath("$.data[?(@.slug == 'vue')]", hasSize(1)));

        mockMvc.perform(get("/api/posts?category=engineering&tag=spring-boot"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].title", is("Taxonomy Post")))
            .andExpect(jsonPath("$.data[0].commentCount", is(0)))
            .andExpect(jsonPath("$.data[0].viewCount", is(0)))
            .andExpect(jsonPath("$.data[0].likeCount", is(0)));

        var visitorFingerprint = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        var anotherFingerprint = "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd";

        mockMvc.perform(post("/api/posts/" + postId + "/views")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "fingerprintHash": "%s"
                    }
                    """.formatted(visitorFingerprint)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.counted", is(true)))
            .andExpect(jsonPath("$.data.viewCount", is(1)));

        mockMvc.perform(post("/api/posts/" + postId + "/views")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "fingerprintHash": "%s"
                    }
                    """.formatted(visitorFingerprint)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.counted", is(false)))
            .andExpect(jsonPath("$.data.viewCount", is(1)));

        mockMvc.perform(post("/api/posts/" + postId + "/views")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "fingerprintHash": "%s"
                    }
                    """.formatted(anotherFingerprint)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.counted", is(true)))
            .andExpect(jsonPath("$.data.viewCount", is(2)));

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "fingerprintHash": "%s",
                      "content": "Needs a visitor profile."
                    }
                    """.formatted(visitorFingerprint)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("请先填写昵称和邮箱")));

        mockMvc.perform(put("/api/visitors/profile")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "fingerprintHash": "%s",
                      "displayName": "Visitor One",
                      "email": "VISITOR@example.com"
                    }
                    """.formatted(visitorFingerprint)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.displayName", is("Visitor One")))
            .andExpect(jsonPath("$.data.email", is("visitor@example.com")));

        var commentResponse = mockMvc.perform(post("/api/posts/" + postId + "/comments")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "fingerprintHash": "%s",
                      "content": "Great article."
                    }
                    """.formatted(visitorFingerprint)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content", is("Great article.")))
            .andExpect(jsonPath("$.data.author.displayName", is("Visitor One")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        var commentId = JsonTestUtils.read(commentResponse, "$.data.id");

        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].content", is("Great article.")));

        mockMvc.perform(get("/api/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.commentCount", is(1)))
            .andExpect(jsonPath("$.data.viewCount", is(2)))
            .andExpect(jsonPath("$.data.likeCount", is(0)));

        mockMvc.perform(get("/api/posts/" + postId + "/likes")
                .queryParam("fingerprintHash", visitorFingerprint))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.likeCount", is(0)))
            .andExpect(jsonPath("$.data.liked", is(false)));

        mockMvc.perform(post("/api/posts/" + postId + "/likes")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "fingerprintHash": "%s"
                    }
                    """.formatted(visitorFingerprint)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.likeCount", is(1)))
            .andExpect(jsonPath("$.data.liked", is(true)));

        mockMvc.perform(post("/api/posts/" + postId + "/likes")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "fingerprintHash": "%s"
                    }
                    """.formatted(visitorFingerprint)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.likeCount", is(1)))
            .andExpect(jsonPath("$.data.liked", is(true)));

        mockMvc.perform(get("/api/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.likeCount", is(1)));

        mockMvc.perform(put("/api/visitors/profile")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "fingerprintHash": "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                      "displayName": "Visitor Two",
                      "email": "visitor@example.com"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message", is("该邮箱已被使用")));

        mockMvc.perform(delete("/api/posts/" + postId + "/likes")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "fingerprintHash": "%s"
                    }
                    """.formatted(visitorFingerprint)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.likeCount", is(0)))
            .andExpect(jsonPath("$.data.liked", is(false)));

        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + commentId)
                .header("Authorization", "Bearer " + writerToken))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(0)));

        mockMvc.perform(delete("/api/posts/" + postId)
                .header("Authorization", "Bearer " + writerToken))
            .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserCanUpdateFooterSettings() throws Exception {
        mockMvc.perform(get("/api/settings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));

        mockMvc.perform(put("/api/settings")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "footerHtml": "<p>Public footer</p>"
                    }
                    """))
            .andExpect(status().isUnauthorized());

        var registerResponse = mockMvc.perform(post("/api/auth/register")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "username": "settingsuser",
                      "displayName": "Settings User",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        var token = JsonTestUtils.read(registerResponse, "$.data.token");

        mockMvc.perform(put("/api/settings")
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "footerHtml": "<p>Public footer</p>"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.footerHtml", is("<p>Public footer</p>")));

        mockMvc.perform(get("/api/settings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.footerHtml", is("<p>Public footer</p>")));
    }

    @Test
    void authenticatedUserCanGenerateSummaryWithFallbackWhenAiDisabled() throws Exception {
        mockMvc.perform(post("/api/posts/summary/generate")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "title": "No Auth",
                      "contentMarkdown": "## Heading\\n\\nBody"
                    }
                    """))
            .andExpect(status().isUnauthorized());

        var registerResponse = mockMvc.perform(post("/api/auth/register")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "username": "summaryuser",
                      "displayName": "Summary User",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        var token = JsonTestUtils.read(registerResponse, "$.data.token");

        mockMvc.perform(post("/api/posts/summary/generate")
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "title": "AI Draft",
                      "contentMarkdown": "## Heading\\n\\nSummary fallback body."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.generatedByAi", is(false)))
            .andExpect(jsonPath("$.data.summary", is("Heading Summary fallback body.")));
    }

    @Test
    void userAndVisitorCanUploadImagesAndBindCoverAvatar() throws Exception {
        var registerResponse = mockMvc.perform(post("/api/auth/register")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "username": "uploaduser",
                      "displayName": "Upload User",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        var token = JsonTestUtils.read(registerResponse, "$.data.token");
        var coverFile = new MockMultipartFile(
            "file",
            "cover.png",
            "image/png",
            "fake-png-content".getBytes()
        );

        var uploadCoverResponse = mockMvc.perform(multipart("/api/files/upload")
                .file(coverFile)
                .param("usage", "POST_COVER")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.contentType", is("image/png")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        var coverUrl = JsonTestUtils.read(uploadCoverResponse, "$.data.url");

        mockMvc.perform(post("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "title": "Post with cover",
                      "summary": "Has image",
                      "coverUrl": "%s",
                      "contentMarkdown": "# Cover\\n\\nBody."
                    }
                    """.formatted(coverUrl)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.coverUrl", is(coverUrl)));

        var userAvatarFile = new MockMultipartFile(
            "file",
            "user-avatar.webp",
            "image/webp",
            "fake-webp-content".getBytes()
        );
        var uploadUserAvatarResponse = mockMvc.perform(multipart("/api/files/upload")
                .file(userAvatarFile)
                .param("usage", "USER_AVATAR")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        var userAvatarUrl = JsonTestUtils.read(uploadUserAvatarResponse, "$.data.url");

        mockMvc.perform(put("/api/auth/me")
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "displayName": "Upload User Updated",
                      "avatarUrl": "%s"
                    }
                    """.formatted(userAvatarUrl)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.displayName", is("Upload User Updated")))
            .andExpect(jsonPath("$.data.avatarUrl", is(userAvatarUrl)));

        var visitorFingerprint = "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc";
        var avatarFile = new MockMultipartFile(
            "file",
            "avatar.jpg",
            "image/jpeg",
            "fake-jpg-content".getBytes()
        );

        var uploadAvatarResponse = mockMvc.perform(multipart("/api/files/upload")
                .file(avatarFile)
                .param("usage", "VISITOR_AVATAR")
                .param("fingerprintHash", visitorFingerprint))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.contentType", is("image/jpeg")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        var avatarUrl = JsonTestUtils.read(uploadAvatarResponse, "$.data.url");

        mockMvc.perform(put("/api/visitors/profile")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "fingerprintHash": "%s",
                      "displayName": "Avatar Visitor",
                      "email": "avatar-visitor@example.com",
                      "avatarUrl": "%s"
                    }
                    """.formatted(visitorFingerprint, avatarUrl)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.avatarUrl", is(avatarUrl)));

        mockMvc.perform(multipart("/api/files/upload")
                .file(new MockMultipartFile("file", "bad.txt", "text/plain", "oops".getBytes()))
                .param("usage", "POST_COVER")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("不支持的文件类型")));
    }
}
