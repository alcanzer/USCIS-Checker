package com.alcnzr.checker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.alcnzr.checker.model.Constants;

@EnableWebFluxSecurity
public class SecurityConfig {
	
	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
				.csrf().disable()
				.authorizeExchange()
				.pathMatchers("/ab/")
				.hasAnyAuthority("ROLE_ADMIN")
				.anyExchange()
				.authenticated()
				.and().formLogin()
				.and()
				.httpBasic()
				.and()
				.build();
	}
	
	@Bean
	public MapReactiveUserDetailsService userdetailsService() {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		UserDetails user = User
				.withUsername(Constants.USERNAME)
				.password(encoder.encode(Constants.PASSWORD))
				.roles("ADMIN")
				.build();
		return new MapReactiveUserDetailsService(user);
	}
}
