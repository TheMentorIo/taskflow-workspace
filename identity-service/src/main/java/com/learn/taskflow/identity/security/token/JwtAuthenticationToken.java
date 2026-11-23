package com.learn.taskflow.identity.security.token;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.learn.taskflow.identity.entity.User;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final User principal;

    public JwtAuthenticationToken(
            User principal,
            Collection<? extends GrantedAuthority> authorities) {

        super(authorities);

        this.principal = principal;

        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}