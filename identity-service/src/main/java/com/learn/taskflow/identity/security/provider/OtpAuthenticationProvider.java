package com.learn.taskflow.identity.security.provider;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.security.service.OtpAuthenticationService;
import com.learn.taskflow.identity.security.service.UserAuthorityResolverService;
import com.learn.taskflow.identity.security.token.OtpAuthenticationToken;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OtpAuthenticationProvider implements AuthenticationProvider {

    private final OtpAuthenticationService otpAuthenticationService;
    private final UserAuthorityResolverService userAuthorityResolverService;

    @Override
    public Authentication authenticate(Authentication authentication) {

        UUID challengeId = (UUID) authentication.getPrincipal();
        String otp = (String) authentication.getCredentials();

        User user = otpAuthenticationService.verify(challengeId, otp);

        Collection<? extends GrantedAuthority> authorities =
                userAuthorityResolverService.resolve(user);

        return new OtpAuthenticationToken(user, authorities);
    }

    @Override
    public boolean supports(Class<?> authType) {
        return OtpAuthenticationToken.class.isAssignableFrom(authType);
    }
}