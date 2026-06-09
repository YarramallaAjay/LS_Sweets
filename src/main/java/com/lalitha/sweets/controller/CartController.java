package com.lalitha.sweets.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lalitha.sweets.model.Cart;
import com.lalitha.sweets.model.CartItem;
import com.lalitha.sweets.model.Product;
import com.lalitha.sweets.model.ProductPrice;
import com.lalitha.sweets.repository.ProductPriceRepository;
import com.lalitha.sweets.repository.ProductRepository;
import com.lalitha.sweets.service.CartService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ProductPriceRepository productPriceRepository;
	
	private final CartService cartService;
	
	
	
	
	public CartController(ProductRepository productRepository, ProductPriceRepository productPriceRepository,
			CartService cartService) {
		
		this.productRepository = productRepository;
		this.productPriceRepository = productPriceRepository;
		this.cartService = cartService;
	}

	
	
	
	@PostMapping("/add")
	public String addToCart(@RequestParam Long productId,
	                        @RequestParam Long priceId,
	                        @RequestParam(defaultValue = "1") int quantity,
	                        HttpSession httpSession,
	                        RedirectAttributes redirectAttributes,
	                        HttpServletRequest request) {

	    Product product = productRepository.findById(productId).orElseThrow();

	    ProductPrice price = productPriceRepository.findById(priceId).orElseThrow();

	    cartService.addTocart(product, price, quantity);

	    redirectAttributes.addFlashAttribute("addedProduct", productId);

	    redirectAttributes.addFlashAttribute("openMiniCart", true);
	    
	    String referer = request.getHeader("Referer");

	    if (referer != null && !referer.isEmpty()) {
	        return "redirect:" + referer;
	    }

	    return "redirect:/";
	}

	    @PostMapping("/update")
	    public String update(@RequestParam Long productId,
	    					 @RequestParam Long priceId,
	                         @RequestParam int quantity,
	                         HttpServletRequest request) {

	        cartService.updateQuantity(productId, priceId, quantity);

	        String referer = request.getHeader("Referer");
	        
	        return "redirect:" + referer;
	    }

	    @PostMapping("/remove")
	    public String remove(@RequestParam Long productId, @RequestParam Long priceId) {

	        cartService.remove(productId, priceId);

	        return "redirect:/cart";
	    }

	    @GetMapping
	    public String view(Model model) {

	        //model.addAttribute("items", cartService.getItems());
	        //model.addAttribute("total", cartService.getTotal());
	    	model.addAttribute("cart", cartService);
	        return "cart";
	    }
	
	
	    @GetMapping("/clear")
	    public String clearCart() {
	    	cartService.clear();
	    	return "redirect:/cart";
	    }
	    
	    @GetMapping("/mini")
	    public String miniCart() {
	        return "fragments/minicart :: minicartContent";
	    }

	    @GetMapping("/count")
	    @ResponseBody
	    public int cartCount() {
	        return cartService.getCartCount();
	    }
	    
//	@PostMapping("/add")
//	public String addToCart(@RequestParam Long productId,
//							@RequestParam Long priceId,
//							@RequestParam int quantity,
//							@ModelAttribute("cart") Cart cart) {
//		
//		Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
//		
//		ProductPrice price = productPriceRepository.findById(priceId).orElseThrow(() -> new RuntimeException("Price not found"));
//		
//		CartItem item = new CartItem();
//		
//		
//		item.setProductId(product.getId());
//		item.setProductName(product.getName());
//		item.setPriceLabel(price.getLabel());
//		item.setPrice(price.getPrice());
//		item.setQuantity(quantity);
//		item.setImageUrl(product.getImageUrl());
//		
//		cart.addItem(item);
//		
//		return "redirect:/cart";
//	}
//	
//	
//	@GetMapping("")
//	public String viewCart(Model model, HttpSession session ) {
//		Cart cart = (Cart) session.getAttribute("cart");
//		if (cart == null) {
//			cart = new Cart();
//			session.setAttribute("cart", cart);
//		}
//		model.addAttribute("cart", cart);
//		return "cart";
//	}
//	
//	@GetMapping("/remove/{index}")
//	public String removeItem(@PathVariable int index, HttpSession session) {
//		Cart cart = (Cart) session.getAttribute("cart");
//		if (cart != null) {
//			cart.getItems().remove(index);
//		}
//		return "redirect:/cart";
//	}
//	
//	
//	@GetMapping("/clear")
//	public String clearCart(@ModelAttribute("cart") Cart cart) {
//		cart.getItems().clear();
//		return "redirect:/cart";
//	}
}
