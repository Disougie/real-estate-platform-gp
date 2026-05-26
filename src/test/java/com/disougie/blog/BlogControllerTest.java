package com.disougie.blog;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = BlogController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class)
)
public class BlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BlogService blogService;

    @Test
    @DisplayName("GET /api/v1/blogs - Should return list of blogs")
    void getBlogs_ShouldReturn200() throws Exception {
        // Given
        BlogUsersReponse blog1 = new BlogUsersReponse("First Blog", "First Content");
        BlogUsersReponse blog2 = new BlogUsersReponse("Second Blog", "Second Content");
        when(blogService.getBlogs()).thenReturn(List.of(blog1, blog2));

        // When & Then
        mockMvc.perform(get("/api/v1/blogs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("First Blog"))
                .andExpect(jsonPath("$[1].title").value("Second Blog"));
    }

    @Test
    @DisplayName("GET /api/v1/blogs - Should return empty list if no blogs exist")
    void getBlogs_ShouldReturn200AndEmptyList() throws Exception {
        // Given
        when(blogService.getBlogs()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/blogs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
}
