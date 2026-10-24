package com.inspaya.fixr.security.details;

import com.inspaya.fixr.encryption.BlindIndexService;
import com.inspaya.fixr.entities.FixrUser;
import com.inspaya.fixr.repositories.FixrUserRepo;
import com.inspaya.fixr.configurations.properties.FixrUserSecurityProperties;
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
    private final FixrUserSecurityProperties securityProperties;

    @Override
    public FixrUserSecurityDetail loadUserByUsername(String blindIndex) throws UsernameNotFoundException {
        FixrUser user = fixrUserRepo.findByBlindEmailIndex(blindIndex)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));
        if (securityProperties.moderatorUsernames() != null
                && java.util.Arrays.stream(securityProperties.moderatorUsernames().split(","))
                .map(String::strip).anyMatch(user.getUsername()::equals)) {
            return new FixrUserSecurityDetail(user, java.util.List.of("FixrUser", "MODERATOR"));
        }
        return new FixrUserSecurityDetail(user);
    }
}
