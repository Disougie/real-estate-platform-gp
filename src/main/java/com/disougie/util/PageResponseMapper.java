package com.disougie.util;

import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper<T> implements Function<Page<T>, PageResponse<T>>{

	@Override
	public PageResponse<T> apply(Page<T> page) {
		return new PageResponse<T>(
				page.getContent(),
				page.getTotalPages(),
				page.getTotalElements(),
				page.getNumber(),
				page.getSize(),
				page.isLast()
		);
	}

}
