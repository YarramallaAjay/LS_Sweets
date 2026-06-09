package com.lalitha.sweets.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.lalitha.sweets.repository.ProductRepository;

@Controller
public class CustomerProductController {

	private final ProductRepository productRepository;

	public CustomerProductController(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}
	
	@GetMapping("/products")
	public String viewProducts(Model model) {
		model.addAttribute("products", productRepository.findByEnabledTrue());
		return "products";
	}
	
}
