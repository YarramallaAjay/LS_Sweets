package com.lalitha.sweets.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lalitha.sweets.model.Product;
import com.lalitha.sweets.repository.ProductRepository;

@Controller
@RequestMapping("/shop")
public class ShopController {

    private final ProductRepository productRepository;

    public ShopController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public String home(Model model) {

        model.addAttribute("products",
                productRepository.findByEnabledTrue());

        return "index";
    }

    @GetMapping("/category/{category}")
    public String byCategory(@PathVariable String category, Model model) {

        model.addAttribute("products",
                productRepository.findByCategoryWithPrices(category)
                        .stream()
                        .filter(Product::isEnabled)
                        .toList());

        return "category";
    }
}

