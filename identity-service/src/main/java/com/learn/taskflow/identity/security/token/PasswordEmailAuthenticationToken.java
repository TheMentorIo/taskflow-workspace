package com.learn.taskflow.identity.security.token;
import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
public class PasswordEmailAuthenticationToken
        extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;

    public PasswordEmailAuthenticationToken(
            String email,
            String password) {

        super(List.of());
        this.principal = email;
        this.credentials = password;
    }

    public PasswordEmailAuthenticationToken(
            Object principal,
            Collection<? extends GrantedAuthority> authorities) {

        super(authorities);
        this.principal = principal;
        this.credentials = null;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}