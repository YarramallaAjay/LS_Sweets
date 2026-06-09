package com.lalitha.sweets.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lalitha.sweets.model.Order;
import com.lalitha.sweets.model.OrderStatus;
import com.lalitha.sweets.repository.OrderRepository;
import com.lalitha.sweets.service.OrderService;

@Controller
public class PaymentSuccessController {
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private OrderRepository orderRepository;

	@GetMapping("/payment-success")
	public String success(@RequestParam Long orderId) {

	    Order order = orderService.getOrder(orderId);
	    order.setStatus(OrderStatus.CONFIRMED);

	    orderRepository.save(order);

	 // return "redirect:/checkout/order-success/" + savedOrder.getId();

	    return "redirect:/checkout/order-success/" + orderId;
	}
	
}
