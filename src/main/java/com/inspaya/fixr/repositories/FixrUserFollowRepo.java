package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.FixrUserFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FixrUserFollowRepo extends JpaRepository<FixrUserFollow, UUID> {
}
