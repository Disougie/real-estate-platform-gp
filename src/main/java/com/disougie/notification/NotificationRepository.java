package com.disougie.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.disougie.app_user.AppUser;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	@Query("from Notification where recipient = :user order by created_at desc")
	Page<Notification> findByRecipient(@Param("user") AppUser recipient, Pageable pageable);
}
