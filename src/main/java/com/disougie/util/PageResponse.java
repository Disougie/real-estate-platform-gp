package com.disougie.util;

import java.util.List;

public record PageResponse<T>(
		
		List<T> content,
		int totalPages,
		long totalElements,
		int page,
		int size,
		boolean last
			
) {

}
