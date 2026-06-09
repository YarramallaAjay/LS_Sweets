package com.lalitha.sweets.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.lalitha.sweets.repository.*;


@Controller
public class AdminCustomerController {

    private final CustomerRepository customerRepository;

    public AdminCustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/admin/customers")
    public String customers(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("content","admin/customers");
        model.addAttribute("title","Customers");
        return "admin/layout";
    }
}
