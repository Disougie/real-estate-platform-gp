package com.disougie.blog;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/blogs")
@RequiredArgsConstructor
@SecurityRequirement(
		name = "BearerAuth"
)
public class BlogController {
	
	private final BlogService blogService;
	
	@GetMapping
	public ResponseEntity<List<BlogUsersReponse>> getBolgs(){
		List<BlogUsersReponse> response = blogService.getBlogs();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
}
