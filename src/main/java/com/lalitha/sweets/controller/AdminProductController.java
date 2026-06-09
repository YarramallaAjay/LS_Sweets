package com.lalitha.sweets.controller;


import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.lalitha.sweets.model.Product;
import com.lalitha.sweets.model.ProductPrice;
import com.lalitha.sweets.repository.ProductRepository;
import com.lalitha.sweets.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
	
	private final ProductService productService;
	private final ProductRepository productRepository;

	public AdminProductController(ProductService productService, ProductRepository productRepository) {
		this.productService = productService;
		this.productRepository = productRepository;
	}
	
	
	@GetMapping
	public String list(Model model)
	{
		
		model.addAttribute("products", productService.findAllWithPrices());
		
		model.addAttribute("content","admin/products");
		model.addAttribute("title","Products");
		
		return "admin/layout";
	}
	
	@GetMapping("/add")
	public String addForm(Model model) {
		model.addAttribute("product", new Product());
		return "admin/product-form";
	}
	
	
	@PostMapping("/save")
	public String save(@ModelAttribute Product product, 
					   @RequestParam(required = false)List<String> priceLabels,
					   @RequestParam(required = false)List<BigDecimal> priceValues, Model model) {
		
		product.getPrices().clear();
		
		if(priceLabels != null && priceValues != null) {
			for(int i=0;i<priceLabels.size();i++) {
				if(priceValues.get(i)==null)continue;
				
				ProductPrice price = new ProductPrice();
				price.setLabel(priceLabels.get(i));
				price.setPrice(priceValues.get(i));
				price.setProduct(product);
				
				product.getPrices().add(price);
			}
		}
		
		try {
			productService.save(product);
		}catch(RuntimeException e) {
			model.addAttribute("error", e.getMessage());
			model.addAttribute("product", product);
			
			return "admin/product-form";
		}
		return "redirect:/admin/products";
	}
	
	@PostMapping("/toggle/{id}")
	public String toggleAvailability(@PathVariable Long id) {

	    Product p = productRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Product not found"));

	    p.setEnabled(!p.isEnabled());

	    productRepository.save(p);

	    return "redirect:/admin/products";
	}
	
	
	@GetMapping("/edit/{id}")
	public String editProduct(@PathVariable Long id, Model model) {
		Product product = productRepository.findByIdWithPrices(id).orElseThrow(() -> new RuntimeException("Product not Found"));
		model.addAttribute("product", product);
		return "admin/product-form";
	}

	
	@GetMapping("/delete/{id}")
	public String deleteProduct(@PathVariable Long id) {
		productRepository.deleteById(id);
		return "redirect:/admin/products";
	}
	
	
	
	
	
}
