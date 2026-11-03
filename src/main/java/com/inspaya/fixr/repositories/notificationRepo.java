package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface notificationRepo extends JpaRepository<Notification, String> {
}
