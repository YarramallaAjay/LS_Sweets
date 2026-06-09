package com.lalitha.sweets.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;


@Service
public class WhatsAppService {

	public void sendWhatsApp(String phone, String message) {

	    RestTemplate restTemplate = new RestTemplate();

	    String url = "http://localhost:3000/send";

	    Map<String, String> body = new HashMap<>();
	    body.put("number", phone);
	    body.put("message", message);

	    // ✅ SET HEADERS
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);

	    HttpEntity<Map<String, String>> request =
	            new HttpEntity<>(body, headers);

	    restTemplate.postForEntity(url, request, String.class);
	}
}