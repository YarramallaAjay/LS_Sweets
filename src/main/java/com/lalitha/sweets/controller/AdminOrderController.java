package com.lalitha.sweets.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lalitha.sweets.model.Order;
import com.lalitha.sweets.model.OrderStatus;
import com.lalitha.sweets.model.OrderStatusHistory;
import com.lalitha.sweets.repository.OrderRepository;
import com.lalitha.sweets.repository.OrderStatusHistoryRepository;
import com.lalitha.sweets.service.EmailService;
import com.lalitha.sweets.service.OrderService;
import com.lalitha.sweets.service.WhatsAppService;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;



@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private OrderStatusHistoryRepository historyRepository;
	
	@Autowired
	private WhatsAppService whatsAppService;
	
//	@GetMapping("")
//	public String viewOrders(Model model) {
//		
//		System.out.println("ADMIN ORDERS CONTROLLER HIT");
//		model.addAttribute("orders", orderRepository.findAll());
//		model.addAttribute("statuses", OrderStatus.values());
//		
//		model.addAttribute("content", "admin/orders");
//		model.addAttribute("title", "Orders");
//		
//		return "admin/layout";
//	}
	
	
	@GetMapping("/filter")
	public String filterOrders(@RequestParam(required = false) OrderStatus status, Model model) {
		
		List<Order> orders = (status == null) 
			? orderRepository.findAll()
			: orderRepository.findByStatus(status);
				
		model.addAttribute("orders", orders);
		model.addAttribute("statuses", OrderStatus.values());
		
		model.addAttribute("content", "admin/orders::content");
		model.addAttribute("title", "Orders");
		return "admin/layout";
	}
	
	
	@GetMapping("/{id}")
	public String viewOrderDetails(@PathVariable Long id, Model model) {
		
		Order order = orderRepository.findById(id).orElseThrow(()-> new RuntimeException("Order not found"));
		
		Optional<OrderStatusHistory> cancelledHistory =
			    historyRepository.findTopByOrderAndStatusOrderByUpdatedTimeDesc(
			        order, OrderStatus.CANCELLED);

		cancelledHistory.ifPresent(h -> {
		    model.addAttribute("cancelledAt", h.getUpdatedTime());
		    model.addAttribute("cancelledBy", h.getUpdatedBy());
		});
		
		model.addAttribute("order", order);
		
		model.addAttribute("content", "admin/order-details");
	    model.addAttribute("title", "Order Details");

		return "admin/layout";
	}
	
	
	@GetMapping("/{id}/status")
	public String showStatusForm(@PathVariable Long id, Model model) {
		
		Order order = orderRepository.findById(id).orElseThrow(()-> new RuntimeException("Order not found"));
		model.addAttribute("order", order);
		model.addAttribute("status", OrderStatus.values());
		
		return "admin-order-status";
	}
	
	
	@PostMapping("/{id}/status")
	public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
		//TODO: process POST request
		Order order = orderService.getOrder(id);
		
		order.setStatus(status);
		
		orderRepository.save(order);

		Order savedOrder = orderRepository.save(order);

		// ✅ Customer number
		String phone = savedOrder.getCustomer().getPhone();

		// ✅ Message builder
		String message = "";
		
		String baseUrl = "https://esophagus-udder-senorita.ngrok-free.dev";

	    String trackUrl = baseUrl + "/track/" + savedOrder.getId();
	    

		switch (status) {

		    case CONFIRMED:
		    	message = "🎉 *Order Confirmed!*\n\n" +
		    	        "Hi " + order.getCustomer().getName() + ",\n\n" +
		    	        "Your order *#" + id + "* has been successfully confirmed.\n\n" +
		    	        "🧾 *Amount:* ₹" + order.getTotalAmount() + "\n" +
		    	        "📍 *Delivery Address:*\n" + order.getAddress() + "\n\n" +
		    	        "👨‍🍳 We’ll start preparing your sweets shortly.\n\n" +
		    	        "🔎 Track your order:\n" +
		    	        trackUrl + "\n\n" +
		    	        "🙏 Thank you for choosing *Lalitha Surya Sweets*!";
		         break;

		    case PREPARING:
		    	message = "👨‍🍳 *Preparing Your Order*\n\n" +
		    	        "Hi " + order.getCustomer().getName() + ",\n\n" +
		    	        "Great news! Your order *#" + id + "* is now being freshly prepared.\n\n" +
		    	        "🍬 Our team is carefully making your sweets with love ❤️\n\n" +
		    	        "⏳ We’ll notify you once it’s ready for shipment.\n\n" +
		    	        "🔎 Track your order:\n" +
		    	        trackUrl + "\n\n" +
		    	        "Thank you for your patience!";
		    	break;

		    case PACKED:
		        message = "📦 *Order Packed!*\n\n" +
		        		"Hi " + order.getCustomer().getName() + ",\n\n" +
		        		"Your order *#" + id + "* has been packed and is ready for dispatch.\n\n" +
		        		"🚀 It will be shipped shortly.\n\n" +
		        		"Thank you for your patience!";
		        break;

		    case SHIPPED:
		    	message = "🚚 *Order Shipped!*\n\n" +
		    	        "Hi " + order.getCustomer().getName() + ",\n\n" +
		    	        "Your order *#" + id + "* has been shipped and is on the way! 🎉\n\n" +
		    	        "📦 It will be delivered to you soon.\n\n" +
		    	        "📍 Delivery Address:\n" + order.getAddress() + "\n\n" +
		    	        "🔎 Track your order:\n" +
		    	        trackUrl + "\n\n" +
		    	        "🙏 Thank you for shopping with us!";
		        break;

		    case OUT_FOR_DELIVERY:
		        message = "🚴 *Out for Delivery!*\n\n" +
		        		"Hi " + order.getCustomer().getName() + ",\n\n" +
		        		"Your order *#" + id + "* is on the way!\n\n" +
		        		"📦 Please be available to receive it.\n\n" +
		        		"Thank you for shopping with us!";
		        break;

		    case DELIVERED:
		        message = "✅🎉  *Order Delivered!*\n\n" +
		        		"Hi " + order.getCustomer().getName() + ",\n\n" +
		        		"Your order *#" + id + "* has been delivered successfully 🎉\n\n" +
		        		"We hope you enjoy our sweets 🍬\n\n" +
		        		"⭐ Please share your feedback!\n\n" +
		        		"Thank you ❤️";
		        break;

		    case CANCELLED:
		        message = "❌ Your order #" + id + " cancelled.";
		        break;

		    default:
		        break;
		}

		// ✅ Send to customer
		if (!message.isEmpty()) {
		    whatsAppService.sendWhatsApp(phone, message);
		}

		// ✅ Send to admin (VERY IMPORTANT)
//		whatsAppService.sendWhatsApp(
//		    adminNumber,
//		    "📢 Order #" + id + " updated to " + status
//		);
	    
	    
		OrderStatusHistory history = new OrderStatusHistory();

		history.setOrder(order);
		history.setStatus(status);
		history.setUpdatedTime(LocalDateTime.now());
		history.setUpdatedBy("ADMIN");

		historyRepository.save(history);
		
		try {
			emailService.sendStatusUpdate(order);
		} catch(Exception e) {
			System.out.println("Email sending failed: " + e.getMessage());
		}
		return "redirect:/admin/orders";
	}

	@GetMapping("")
	public String getOrders(

	        @RequestParam(required = false) String keyword,
	        @RequestParam(required = false) OrderStatus status,
	        @RequestParam(required = false) String fromDate,
	        @RequestParam(required = false) String toDate,

	        @RequestParam(required = false, defaultValue = "0") int page,
	        @RequestParam(required = false, defaultValue = "20") int size,

	        Model model) {

	    Pageable pageable = PageRequest.of(page, size);

	    LocalDateTime from = null;
	    LocalDateTime to = null;

	    try {

	        if (fromDate != null && !fromDate.isEmpty()) {
	            from = LocalDate.parse(fromDate)
	                    .atStartOfDay();
	        }

	        if (toDate != null && !toDate.isEmpty()) {
	            to = LocalDate.parse(toDate)
	                    .atTime(23, 59, 59);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    Page<Order> orderPage =
	            orderRepository.findOrdersWithFilters(
	                    keyword,
	                    status,
	                    from,
	                    to,
	                    pageable
	            );

	    Map<Long, List<OrderStatus>> allowedStatuses = new HashMap<>();

	    for (Order order : orderPage.getContent()) {

	        allowedStatuses.put(
	                order.getId(),
	                order.getStatus().nextAllowedStatuses()
	        );
	    }

	    model.addAttribute("orders", orderPage.getContent());
	    model.addAttribute("orderPage", orderPage);

	    model.addAttribute("allowedStatuses", allowedStatuses);

	    model.addAttribute("statuses", OrderStatus.values());

	    model.addAttribute("keyword", keyword);
	    model.addAttribute("status", status);
	    model.addAttribute("fromDate", fromDate);
	    model.addAttribute("toDate", toDate);

	    model.addAttribute("content", "admin/orders");

	    return "admin/layout";
	}
	 
//	@GetMapping("/admin/orders/{id}")
//	public String orderDetails(@PathVariable Long id, Model model) {
//		
//		Order order  = orderService.getOrder(id);
//		
//		model.addAttribute("order", order);
//		model.addAttribute("content", "admin/order-details");
//		model.addAttribute("title", "Order Details");
//		
//		return "admin/layout";
//		
//	}
	
}
