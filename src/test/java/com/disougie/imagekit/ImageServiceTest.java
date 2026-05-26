package com.disougie.imagekit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
import org.springframework.web.multipart.MultipartFile;

import com.disougie.property.entity.Image;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @Mock
    private ImageKit imageKit;

    @InjectMocks
    private ImageService imageService;

    private MultipartFile mockFile;
    private Result mockResult;

    @BeforeEach
    void setUp() throws Exception {
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test-image.jpg");
        when(mockFile.getBytes()).thenReturn("image-content".getBytes());

        mockResult = new Result();
        mockResult.setUrl("http://imagekit.io/test-image.jpg");
        mockResult.setFileId("file-123");
    }

    @Test
    @DisplayName("Should successfully upload images and return list of Image entities")
    void uploadImages_ShouldReturnImageList() throws Exception {
        // Given
        when(imageKit.upload(any(FileCreateRequest.class))).thenReturn(mockResult);

        // When
        List<Image> result = imageService.uploadImages(List.of(mockFile));

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("http://imagekit.io/test-image.jpg", result.get(0).getImageUrl());
        assertEquals("file-123", result.get(0).getFileId());
        verify(imageKit).upload(any(FileCreateRequest.class));
    }

    @Test
    @DisplayName("Should handle exceptions during upload gracefully and continue")
    void uploadImages_ShouldHandleExceptionsGracefully() throws Exception {
        // Given
        when(imageKit.upload(any(FileCreateRequest.class))).thenThrow(new InternalError("Upload failed"));

        // When
        List<Image> result = imageService.uploadImages(List.of(mockFile));

        // Then
        assertTrue(result.isEmpty());
        verify(imageKit).upload(any(FileCreateRequest.class));
    }

    @Test
    @DisplayName("Should delete image successfully")
    void deleteImage_ShouldCallImageKitDelete() throws Exception {
        // When
        imageService.deleteImage("file-123");

        // Then
        verify(imageKit).deleteFile("file-123");
    }

    @Test
    @DisplayName("Should handle exceptions during delete gracefully")
    void deleteImage_ShouldHandleExceptionsGracefully() throws Exception {
        // Given
        doThrow(new InternalError("Delete failed")).when(imageKit).deleteFile(anyString());

        // When
        imageService.deleteImage("file-123");

        // Then
        verify(imageKit).deleteFile("file-123");
    }
}
