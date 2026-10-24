package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.UserValidityProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserValidityProfileRepo extends JpaRepository<UserValidityProfile, UUID> {
}
