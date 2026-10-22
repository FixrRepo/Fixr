package com.inspaya.fixr.repositories;

import com.inspaya.fixr.entities.FixrUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface FixrUserRepo extends JpaRepository<FixrUser, UUID> {

//    @Query(value = """
//    SELECT *
//    FROM fixr_user
//    WHERE blind_email_index = decode(:blindEmailIndex, 'hex')
//    """, nativeQuery = true)
//    Optional<FixrUser> findBlindEmailIndex(B blindEmailIndex);

    @Query(value = """
    SELECT *
    FROM fixr_user
    WHERE blind_email_index = decode(:blindEmailIndex, 'hex')
    """, nativeQuery = true)
    Optional<FixrUser> getBlindEmailIndexFromHex(byte[] blindEmailIndex);

    Optional<FixrUser> findByBlindEmailIndex(String blindEmailIndex);

    boolean existsByUsername(String username);
}
