package com.learn.taskflow.identity.security.provider;

import java.util.Collection;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.security.service.UserAuthenticationService;
import com.learn.taskflow.identity.security.service.UserAuthorityResolverService;
import com.learn.taskflow.identity.security.token.PasswordEmailAuthenticationToken;
import com.learn.taskflow.identity.service.AccountStatePolicy;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PasswordEmailAuthenticationProvider implements AuthenticationProvider{
    private final UserAuthenticationService userAuthenticationService;
    private final PasswordEncoder passwordEncoder;
    private final UserAuthorityResolverService userAuthorityResolverService;
    private final AccountStatePolicy accountStatePolicy;

    @Override
    public Authentication authenticate(Authentication authentication) {

        String email = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        User user = userAuthenticationService.loadByEmail(email);
        accountStatePolicy.requireActive(user);

        if(!user.isEmailVerified())
            throw new BadCredentialsException("Account is not active");

        boolean isMatch = passwordEncoder.matches(
                password,
                user.getPasswordHash()
        );

        if (!isMatch) {
            throw new BadCredentialsException("Invalid credentials");
        }
        Collection<? extends GrantedAuthority> authorities = userAuthorityResolverService.resolve(user);
        return new PasswordEmailAuthenticationToken(
               user,
               authorities
        );
    }

    @Override
    public boolean supports (Class<?> authType){
        return PasswordEmailAuthenticationToken.class.isAssignableFrom(authType);
    }
}
