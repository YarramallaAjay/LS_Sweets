package com.lalitha.sweets.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.lalitha.sweets.model.CartItem;
import com.lalitha.sweets.service.CartService;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;

    @ModelAttribute
    public void globalData(Model model) {

        Collection<CartItem> cartItems =
                cartService.getItems();

        model.addAttribute("cartItems", cartItems);

        model.addAttribute(
                "cartCount",
                cartService.getCartCount()
        );

        model.addAttribute(
                "cartTotal",
                cartService.getTotal()
        );
    }
}