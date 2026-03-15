package com.disougie.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.disougie.app_user.AppUser;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Page<Notification> findByRecipient(AppUser recipient, Pageable pageable);
}
