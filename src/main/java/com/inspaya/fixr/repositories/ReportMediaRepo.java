package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.ReportMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportMediaRepo extends JpaRepository<ReportMedia, UUID> {
}
