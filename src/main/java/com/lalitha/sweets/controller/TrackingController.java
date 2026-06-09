package com.lalitha.sweets.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lalitha.sweets.model.Order;
import com.lalitha.sweets.model.OrderStatus;
import com.lalitha.sweets.model.OrderStatusHistory;
import com.lalitha.sweets.repository.OrderStatusHistoryRepository;
import com.lalitha.sweets.service.OrderService;

@Controller
public class TrackingController {
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private OrderStatusHistoryRepository historyRepository;
	
	public TrackingController(OrderService orderService) {
		this.orderService = orderService;
	}




	@GetMapping("/track/{id}")
	public String track(@PathVariable Long id, Model model) {
			
			Order order = orderService.getOrder(id);
			if(order == null) {
				return "redirect:/?error=orderNotFound";
			}
			
			List<OrderStatusHistory> history = historyRepository.findByOrderIdOrderByUpdatedTimeAsc(id);
		       
	    	 
	        int step = getStepNumber(order.getStatus());
	        
	        LocalDate estimatedDelivery = orderService.getEstimatedDelivery(order);

	        model.addAttribute("order", order);
	        model.addAttribute("step", step);
	        model.addAttribute("estimatedDelivery", estimatedDelivery);
	        model.addAttribute("history", history);
	        
	        return "track-order";
	}

	private int getStepNumber(OrderStatus status) {
	    return switch (status) {
	        case PLACED -> 1;
	        case CONFIRMED -> 2;
	        case PREPARING -> 3;
	        case PACKED -> 4;
	        case SHIPPED -> 5;
	        case OUT_FOR_DELIVERY -> 6;
	        case DELIVERED -> 7;
	        case CANCELLED -> -1;
	        default -> 0;
	    };
	}
	
	@PostMapping("/orders/cancel/{id}")
	public String cancelOrder(@RequestParam  Long orderId,
							  @RequestParam String reason){
		
		Order order = orderService.cancelOrder(orderId, reason);
		
		String message = "Hello" + order.getCustomerNameSnapshot()+
					", Your LalithaSuryaSweets Order #" + order.getId()+
					" has been Cancelled. \nReason: "+ reason;
		
		String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
		
		return "redirect:/track/" + orderId + "?cancelled=true";
//		return "redirect:https://wa.me/91"
//        + order.getCustomerPhoneSnapshot()
//        + "?text=" + encoded;
	}
	
}
