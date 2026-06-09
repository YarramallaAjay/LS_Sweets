package com.lalitha.sweets.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PolicyController {

	@GetMapping("/policies")
	public String polciespage() {
		return "policies";
	}
	
	
}
