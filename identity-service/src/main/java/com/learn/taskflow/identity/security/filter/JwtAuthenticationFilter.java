package com.learn.taskflow.identity.security.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.repository.UserRepository;
import com.learn.taskflow.identity.security.service.JwtAuthenticationService;
import com.learn.taskflow.identity.security.service.UserAuthorityResolverService;
import com.learn.taskflow.identity.security.token.JwtAuthenticationToken;
import com.learn.taskflow.identity.service.AccountStatePolicy;
import com.learn.taskflow.identity.service.SessionService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationService jwtAuthenticationService;
    private final UserRepository userRepository;
    private final UserAuthorityResolverService userAuthorityResolverService;
    private final AccountStatePolicy accountStatePolicy;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        filterChain.doFilter(request, response);
                        return;
                }

                Claims claims = jwtAuthenticationService.parseToken(token);
                        if (!jwtAuthenticationService.isAccessToken(claims)) {
                                throw new SecurityException("Invalid token type");
                        }

                UUID userId = UUID.fromString(claims.getSubject());
                UUID sessionId = jwtAuthenticationService.getSessionId(claims);
                
                sessionService.requireActive(sessionId, userId);

                User user = userRepository
                        .findWithAuthorizationById(userId)
                        .orElseThrow();
                accountStatePolicy.requireActive(user);

                Collection<? extends GrantedAuthority> authorities =
                        userAuthorityResolverService.resolve(user);

                JwtAuthenticationToken authentication =
                        new JwtAuthenticationToken(
                                user,
                                authorities
                        );
                authentication.setDetails(sessionId);

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

                } catch (io.jsonwebtoken.JwtException | IllegalArgumentException | SecurityException e) {
                SecurityContextHolder.clearContext();
                } catch (RuntimeException e) {
                        SecurityContextHolder.clearContext();
                        throw e;
        }

        filterChain.doFilter(request, response);
    }
}