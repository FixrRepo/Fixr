package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.CredibilityEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CredibilityEventRepo extends JpaRepository<CredibilityEvent, UUID> {
}
