package com.disougie.blog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long>{

	@Query(
			value =  "select * from blog where match(content, title) against (:text in boolean mode)", 
			nativeQuery = true
	)
	Page<Blog> findByContent(@Param("text") String text, Pageable pageable);
	
}
