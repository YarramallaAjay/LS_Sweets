package com.lalitha.sweets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.lalitha.sweets.service.AdminUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final AdminUserDetailsService adminUserDetailsService;
	
	public SecurityConfig(AdminUserDetailsService adminUserDetailsService) {
		
		this.adminUserDetailsService = adminUserDetailsService;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		
		return config.getAuthenticationManager();
	}
	
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		
		provider.setUserDetailsService(adminUserDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		
		return provider;
	}
	
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

	    http

	        .authorizeHttpRequests(auth -> auth

	            .requestMatchers(
	                    "/",
	                    "/home",
	                    "/products/**",
	                    "/sweets",
	                    "/hot",
	                    "/specials",
	                    "/pickels",
	                    "/test-whatsapp",
	                    "/cart/**",
	                    "/checkout/**",
	                    "/policies/**",
	                    "/order/**",
	                    "/checkout/invoice/**",
	                    "/orders/**",
	                    "/place-order",
	                    "/track/**",
	                    "/css/**",
	                    "/js/**",
	                    "/images/**"
	            ).permitAll()
	            
	            .requestMatchers(HttpMethod.POST, "/cart/**")
	            .permitAll()

	            .requestMatchers("/cart/**")
	            .permitAll()
	            

	            .requestMatchers("/admin/**").hasRole("ADMIN")

	            .anyRequest().authenticated()
	        )

	        .formLogin(form -> form
	                .loginPage("/admin/login")
	                .defaultSuccessUrl("/admin/orders", true)
	                .failureUrl("/admin/login?error=true")
	                .permitAll()
	        )

	        .logout(logout -> logout
	                .logoutUrl("/logout")
	                .logoutSuccessUrl("/admin/login")
	                .invalidateHttpSession(true)
	                .clearAuthentication(true)
	                .permitAll()
	        );

	    return http.build();
	}


}
	