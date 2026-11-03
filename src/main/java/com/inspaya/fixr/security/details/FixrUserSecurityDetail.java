package com.inspaya.fixr.security.details;

import com.inspaya.fixr.entities.FixrUser;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class FixrUserSecurityDetail implements UserDetails {

    private final List<SimpleGrantedAuthority> authorities;
    private final String password;
    private final String username;
    @Setter
    @Getter
    private UUID sessionId;
    private final Boolean isEnabled;
    private final boolean isEmailVerified;
    @Getter
    private final FixrUser user;

    public FixrUserSecurityDetail(FixrUser fixrUser){
        this(fixrUser, List.of("FixrUser"));
    }


    public FixrUserSecurityDetail(FixrUser fixrUser, List<String> authorities){
        this.username = fixrUser.getBlindEmailIndex();
        this.password = fixrUser.getPassword();
        this.isEnabled = fixrUser.getIsActive();
        this.isEmailVerified = fixrUser.getEmailVerified();
        this.authorities = authorities.stream().map(SimpleGrantedAuthority::new).toList();
        this.user = fixrUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public boolean isAccountNonLocked(){
        return isEmailVerified;
    }
}
