package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.Lga;
import com.inspaya.fixr.entities.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LgaRepo extends JpaRepository<Lga, Long> {
    Optional<Lga> findByNameAndState(String lgaName, State state);
}


