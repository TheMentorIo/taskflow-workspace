package com.learn.taskflow.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

import com.learn.taskflow.identity.security.filter.JwtAuthenticationFilter;
import com.learn.taskflow.identity.security.provider.OtpAuthenticationProvider;
import com.learn.taskflow.identity.security.provider.PasswordEmailAuthenticationProvider;
import com.learn.taskflow.identity.security.service.OtpAuthenticationService;
import com.learn.taskflow.identity.security.service.UserAuthenticationService;
import com.learn.taskflow.identity.security.service.UserAuthorityResolverService;
import com.learn.taskflow.identity.service.AccountStatePolicy;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public ForwardedHeaderFilter forwardedHeaderFilter() {
		return new ForwardedHeaderFilter();
	}

    @Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public PasswordEmailAuthenticationProvider passwordEmailAuthenticationProvider(
			UserAuthenticationService userAuthenticationService,
			PasswordEncoder passwordEncoder,
			UserAuthorityResolverService userAuthorityResolverService,
			AccountStatePolicy accountStatePolicy) {
		return new PasswordEmailAuthenticationProvider(
				userAuthenticationService,
				passwordEncoder,
				userAuthorityResolverService,
				accountStatePolicy);
	}

	@Bean
	public OtpAuthenticationProvider otpAuthenticationProvider(
			OtpAuthenticationService otpAuthenticationService,
			UserAuthorityResolverService userAuthorityResolverService) {
		return new OtpAuthenticationProvider(
				otpAuthenticationService,
				userAuthorityResolverService);
	}

	@Bean
	public AuthenticationManager authenticationManager(
			PasswordEmailAuthenticationProvider passwordEmailAuthenticationProvider,
			OtpAuthenticationProvider otpAuthenticationProvider) {
		return new ProviderManager(passwordEmailAuthenticationProvider, otpAuthenticationProvider);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								"/actuator/health",
								"/api/auth/register",
								"/api/auth/login",
								"/api/auth/refresh",
								"/api/auth/challenges/*/verify").permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
