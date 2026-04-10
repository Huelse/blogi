package com.blogi;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class BlogiApiIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
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
}
