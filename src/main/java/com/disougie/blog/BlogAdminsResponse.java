package com.disougie.blog;

import java.time.LocalDateTime;

public record BlogAdminsResponse(
		long id, 
		String writer, 
		String title, 
		String content,
		LocalDateTime createdAt
) {

}
