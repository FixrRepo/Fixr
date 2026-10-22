package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StateRepo extends JpaRepository<State, Long> {
    Optional<State> findByName(String stateName);
}
