package com.inspaya.fixr.security.details;

import com.inspaya.fixr.encryption.BlindIndexService;
import com.inspaya.fixr.entities.FixrUser;
import com.inspaya.fixr.repositories.FixrUserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixrUserSecurityDetailsService implements UserDetailsService {
    private final FixrUserRepo fixrUserRepo;

    @Override
    public FixrUserSecurityDetail loadUserByUsername(String blindIndex) throws UsernameNotFoundException {
        FixrUser user = fixrUserRepo.findByBlindEmailIndex(blindIndex)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));
        return new FixrUserSecurityDetail(user);
    }
}
