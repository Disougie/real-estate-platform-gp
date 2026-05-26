package com.disougie.blog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @InjectMocks
    private BlogService blogService;

    private Blog mockBlog;

    @BeforeEach
    void setUp() {
        mockBlog = new Blog();
        mockBlog.setId(1L);
        mockBlog.setTitle("Sample Blog");
        mockBlog.setContent("This is the content of the blog");
    }

    @Test
    @DisplayName("Should return list of blogs mapped to BlogUsersResponse")
    void getBlogs_ShouldReturnListOfBlogUsersResponse() {
        // Given
        when(blogRepository.findAll()).thenReturn(List.of(mockBlog));

        // When
        List<BlogUsersReponse> result = blogService.getBlogs();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sample Blog", result.get(0).title());
        assertEquals("This is the content of the blog", result.get(0).content());
        
        verify(blogRepository).findAll();
    }
    
    @Test
    @DisplayName("Should return empty list when no blogs exist")
    void getBlogs_ShouldReturnEmptyList() {
        // Given
        when(blogRepository.findAll()).thenReturn(List.of());

        // When
        List<BlogUsersReponse> result = blogService.getBlogs();

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(blogRepository).findAll();
    }
}
