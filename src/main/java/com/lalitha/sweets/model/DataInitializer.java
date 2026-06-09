package com.lalitha.sweets.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.lalitha.sweets.repository.AdminRepository;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

	@Autowired
	private AdminRepository adminRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@PostConstruct
	public void createAdmin() {
		
		if (adminRepository.findByUsername("admin").isPresent()) {
			System.out.println("✅ Admin already exists — skipping creation");
			return;
		}
		
		Admin admin = new Admin();
		admin.setUsername("admin");
		admin.setPassword(passwordEncoder.encode("admin123"));
		admin.setRole("ADMIN");
		
		
		adminRepository.save(admin);
		System.out.println("Default admin created");
	}
	
}
