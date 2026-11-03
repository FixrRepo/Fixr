package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.report.VideoEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoEmbeddingRepo extends JpaRepository<VideoEmbedding, UUID> {
}
