package com.disougie.blog;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlogService {
	
	private final BlogRepository blogRepository;

	public List<BlogUsersReponse> getBlogs() {
		return blogRepository.findAll()
				.stream()
				.map(blog -> new BlogUsersReponse(blog.getTitle(), blog.getContent()))
				.toList();
	}

}
