package com.lalitha.sweets.controller;





import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.lalitha.sweets.model.Cart;
import com.lalitha.sweets.repository.ProductRepository;
import com.lalitha.sweets.service.ProductService;

import jakarta.servlet.http.HttpSession;


@Controller
public class HomeController {

	private final ProductService productService;
	private final ProductRepository productRepository;
	
	public HomeController(ProductService productService, ProductRepository productRepository) {
		this.productService = productService;
		this.productRepository = productRepository;
	}
	
	@GetMapping("/")
	public String home(Model model, HttpSession httpSession) {

	    model.addAttribute("products", productService.findAllWithPrices());
	    model.addAttribute("featured", productService.getFeatured());

	    model.addAttribute("sweet",
	            productService.getByCategory("Sweet")
	                    .stream()
	                    .limit(15)
	                    .toList());

	    model.addAttribute("hot",
	            productService.getByCategory("Hot")
	                    .stream()
	                    .limit(15)
	                    .toList());

	    Cart cart = (Cart) httpSession.getAttribute("cart");

	    if (cart == null) {
	        cart = new Cart();
	        
	        httpSession.setAttribute("cart", cart);
	    }

	    model.addAttribute("cart", cart);
	    model.addAttribute("cartCount", cart.getItems().size());

	    return "home";
	}
	
	
	@GetMapping("/sweets")
	public String sweets(Model model,
	                     HttpSession session) {

	    Cart cart =
	        (Cart) session.getAttribute("cart");

	    if (cart == null) {

	        cart = new Cart();

	        session.setAttribute("cart", cart);
	    }

	    model.addAttribute("cart", cart);

	    model.addAttribute(
	        "cartCount",
	        cart.getItems().size()
	    );

	    model.addAttribute(
	        "products",
	        productService.getSweets()
	    );

	    return "category";
	}

	@GetMapping("/heat")
	public ResponseEntity getHeat(Model model) {


		return ResponseEntity.ok("test api");
	}


	@GetMapping("/hot")
	public String hot(Model model) {
		
		model.addAttribute("products", productRepository.findByCategoryWithPrices("Hot"));
		
		return "category";
	}
	
	@GetMapping("/specials")
	public String specials(Model model) {
		
		model.addAttribute("products", productRepository.findByCategoryWithPrices("Special"));
		
		return "category";
	}
	
	@GetMapping("/pickels")
	public String pickels(Model model) {
		
		model.addAttribute("products", productRepository.findByCategoryWithPrices("Pickel"));
		
		return "category";
	}

}
