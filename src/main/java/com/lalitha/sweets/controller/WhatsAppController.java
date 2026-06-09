package com.lalitha.sweets.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lalitha.sweets.service.WhatsAppService;

@Controller
public class WhatsAppController {

	@Autowired
	private WhatsAppService whatsAppService;
	
	@GetMapping("/test-whatsapp")
	@ResponseBody
	public String testWhatsApp() {
//
//	    whatsAppService.sendToCustomer("+919398657185",
//	            "🔥 Test message from your Spring Boot app!");

	    return "Message Sent!";
	}
}
