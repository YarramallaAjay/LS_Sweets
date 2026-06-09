package com.lalitha.sweets.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lalitha.sweets.model.Order;
import com.lalitha.sweets.repository.OrderRepository;
import com.lalitha.sweets.service.AdminDashboardService;
import com.lalitha.sweets.service.OrderService;


@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

	@Autowired
	private AdminDashboardService dashboardService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderService orderService;
	
	@GetMapping
	public String dashboard(Model model) {
		
		model.addAttribute("totalOrders", dashboardService.getTotalOrders());
		model.addAttribute("totalRevenue", dashboardService.getTotalRevenue());
		model.addAttribute("pendingOrders", dashboardService.getPendingOrders());
		model.addAttribute("todayOrders", dashboardService.getTodayOrders());
		model.addAttribute("todayRevenue", dashboardService.getTodayRevenue());
		//model.addAttribute("monthlyOrders", dashboardService.getMonthlyOrders());
		
		List<Object[]> monthlyOrders = orderRepository.getMonthlyOrders();

		
	    int[] monthlyData = new int[12];

	    for(Object[] obj : monthlyOrders){
	        int month = (int) obj[0];
	        int count = ((Long) obj[1]).intValue();
	        monthlyData[month-1] = count;
	    }

	     
	    model.addAttribute("monthlyOrders", monthlyData);
	    model.addAttribute("topProducts", dashboardService.getTopSellingProducts());
		
		
		model.addAttribute("content", "admin/dashboard");
		model.addAttribute("title", "Dashboard");
		
		return "admin/layout";
		
	}
	
	@GetMapping("/recent-orders")
	public String recentOrders(Model model) {
		
		Pageable pageable = PageRequest.of(0, 5, Sort.by("id").descending());

	    List<Order> recentOrders = orderRepository.findAll(pageable).getContent();

	    model.addAttribute("orders", recentOrders);
	    
	    model.addAttribute("content", "admin/recent-orders");
	    model.addAttribute("title", "Recent Orders");
	    
	    return "admin/layout";
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
