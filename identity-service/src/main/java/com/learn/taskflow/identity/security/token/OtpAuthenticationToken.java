package com.learn.taskflow.identity.security.token;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class OtpAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;

    public OtpAuthenticationToken(
            UUID challengeUuid,
            String otp) {

        super(List.of());

        this.principal = challengeUuid;
        this.credentials = otp;
    }

    public OtpAuthenticationToken(
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