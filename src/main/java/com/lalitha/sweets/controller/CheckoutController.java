package com.lalitha.sweets.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties.Request;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.lalitha.sweets.model.*;
import com.lalitha.sweets.repository.CustomerRepository;
import com.lalitha.sweets.repository.OrderRepository;
import com.lalitha.sweets.repository.PincodeRepository;
import com.lalitha.sweets.repository.historyRepository;
import com.lalitha.sweets.service.CartService;
import com.lalitha.sweets.service.EmailService;
import com.lalitha.sweets.service.InvoiceService;
import com.lalitha.sweets.service.OrderService;
import com.lalitha.sweets.service.PaymentService;
import com.lalitha.sweets.service.WhatsAppService;
import com.razorpay.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private InvoiceService invoiceService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private WhatsAppService whatsAppService;
	
	@Autowired
	private PaymentService paymentService;
	
	@Autowired
	private PincodeRepository pincodeRepository;
	
	@Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
	
	@Value("${whatsapp.admin.number}")
	private String adminNumber;
	
	@PostMapping("/place-order")
	public String placeOrder(
	        @RequestParam String customerName,
	        @RequestParam String phone,
	        @RequestParam String address,
	        @RequestParam String email,
	        @RequestParam String state,
	        @RequestParam String pincode,
	        Model model
	) {

	    if (cartService.isEmpty()) {
	        return "redirect:/cart";
	    }

	    Customer customer = customerRepository
	            .findByEmail(email)
	            .orElse(new Customer());

	    //Customer customer = new Customer();
	    
	    String fullPhone = phone;
	    
	    if (!phone.startsWith("91")) {
			fullPhone = "91"+phone;
		}
	    
	    customer.setName(customerName);
	    customer.setEmail(email);
	    customer.setPhone(fullPhone);
	    customer.setAddress(address);
	    customer.setState(state);
	    customer.setPincode(pincode);
	     
	    customer = customerRepository.save(customer);

	    Order order = new Order();

	    
	    System.out.println("STATE RECEIVED: " + state);
	    order.setAddress(address);
	    order.setState(state);
	    order.setCustomerNameSnapshot(customerName);
	    order.setCustomerPhoneSnapshot(fullPhone);
	    
	    order.setCustomer(customer);
	    order.setOrderDate(LocalDateTime.now());
	    order.setStatus(OrderStatus.PLACED);
	    order.setState(state);
	    
	    orderService.calculateCharges(order, cartService.getItems(), state);
	    System.out.println("AFTER CALCULATION DELIVERY: " + order.getDeliveryCharges());

	    
	    for (CartItem cartItem : cartService.getItems()) {

	        OrderItem item = new OrderItem();
	        item.setProductName(cartItem.getProductName());
	        item.setPriceLabel(cartItem.getPriceLabel());
	        item.setPrice(cartItem.getPrice());
	        item.setQuantity(cartItem.getQuantity());
	        item.setOrder(order);

	        order.getItems().add(item);
	    }
	    System.out.println("🔥 WhatsApp sending started...");
	    Order savedOrder = orderRepository.save(order);
	    
	    model.addAttribute("customerName", customerName);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);
        model.addAttribute("orderId", savedOrder.getId());
        
	    
	    try {
	        // 🔥 create Razorpay order
	        com.razorpay.Order razorpayOrder =
	                paymentService.createOrder(order.getTotalAmount().doubleValue());
	        System.out.println("Razorpay Order Id: "+ razorpayOrder.get("id"));
	        
	        model.addAttribute("razorpayOrderId", razorpayOrder.get("id"));
	        model.addAttribute("amount", order.getTotalAmount().multiply(new java.math.BigDecimal(100)).intValue());
	        model.addAttribute("key", razorpayKeyId);

	    } catch (Exception e) {
	        e.printStackTrace();
	        return "redirect:/cart"; 
	    }

	    	    
	   
	    
	   return "payment"; // 👉 new page
	   //return "redirect:/checkout/order-success/" + savedOrder.getId();
	}



	@GetMapping
	public String checkout(Model model) {

	    Collection<CartItem> items = cartService.getItems();

	    if (items.isEmpty()) {
	        return "redirect:/cart";
	    }

	    BigDecimal subtotal = cartService.getTotal();

	    BigDecimal handling = BigDecimal.valueOf(20);

	    BigDecimal delivery = BigDecimal.ZERO;  // Default until state selected

	    BigDecimal total = subtotal.add(handling).add(delivery);

	    model.addAttribute("items", items);
	    model.addAttribute("subtotal", subtotal);
	    model.addAttribute("handling", handling);
	    model.addAttribute("delivery", delivery);
	    model.addAttribute("total", total);
	    
	    System.out.println("Subtotal: " + subtotal);
	    System.out.println("Handling: " + handling);
	    System.out.println("Delivery: " + delivery);
	    System.out.println("Total: " + total);


	    return "checkout";
	}
	
	
	@GetMapping("/order-success/{id}")
	public String orderSuccess(@PathVariable Long id, Model model) {
		
		//Order lastOrder = orderRepository.findTopByOrderByIdDesc();
		Order order = orderRepository.findById(id).orElse(null);
		model.addAttribute("order", order);
		
		return "order-success";
	}
	
	
	
	@GetMapping("/invoice/{id}")
	public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {

	    Order order = orderRepository.findByIdWithItems(id)
	            .orElseThrow();

	    byte[] pdf = invoiceService.generateInvoice(order);

	    return ResponseEntity.ok()
	            .header(HttpHeaders.CONTENT_DISPOSITION,
	                    "attachment; filename=invoice-" + id + ".pdf")
	            .contentType(MediaType.APPLICATION_PDF)
	            .body(pdf);
	}
	
	
//	@GetMapping("/calculate-delivery")
//	@ResponseBody
//	public ResponseEntity<?> calculateDelivery(@RequestParam String state) {
//
//	    Collection<CartItem> items = cartService.getItems();
//
//	    Order tempOrder = new Order();
//
//	    orderService.calculateCharges(tempOrder, items, state);
//
//	    return ResponseEntity;
//	}


	
	@GetMapping("/payment-success")
	public String paymentSuccess(
	        @RequestParam("paymentId") String paymentId,
	        @RequestParam("razorpayOrderId") String razorpayOrderId,
	        @RequestParam("signature") String signature,
	        @RequestParam("orderId") Long orderId,
	        Model model
	) {
		
		 try {

		        //String secret = "hOmoZ6EX5R4XX3ct34C7xSUs";

		        JSONObject options = new JSONObject();
		        options.put("razorpay_order_id", razorpayOrderId);
		        options.put("razorpay_payment_id", paymentId);
		        options.put("razorpay_signature", signature);

		        boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

		        if (!isValid) {
		            return "redirect:/checkout/payment-failed";
		        }
		

	    Order order = orderRepository.findById(orderId).orElseThrow();

	    order.setPaymentId(paymentId);
	    order.setPaymentStatus(PaymentStatus.SUCCESS);
	    order.setStatus(OrderStatus.PLACED);
	    
	    order.setRazorpayOrderId(razorpayOrderId);
	    order.setRazorpayPaymentId(paymentId);
	    order.setRazorpaySignature(signature);

	    orderRepository.save(order);

	    OrderStatusHistory history = new OrderStatusHistory();
	    history.setOrder(order);
	    history.setStatus(order.getStatus());
	    history.setUpdatedTime(LocalDateTime.now());
	    history.setUpdatedBy("CUSTOMER");
	    historyRepository.save(history);

	    

	    byte[] invoicePdf = invoiceService.generateInvoice(order);

	    emailService.sendOrderConfirmation(
	            order,
	            "Order Placed - Lalitha Surya Sweets",
	            "Track Order: https://esophagus-udder-senorita.ngrok-free.dev/track/" + order.getId(),
	            invoicePdf
	    );

	    String invoiceUrl = "https://esophagus-udder-senorita.ngrok-free.dev/checkout/invoice/" + order.getId();

	    whatsAppService.sendWhatsApp(
	            order.getCustomerPhoneSnapshot(),
	            "🎉 *Payment Successful!*\n\n" +
	            "Hi " + order.getCustomerNameSnapshot() + ",\n\n" +
	            "Your payment was successful and your order is Placed.\n\n" +
	            "🧾 *Order ID:* #" + order.getId() + "\n" +
	            "💰 *Amount:* ₹" + order.getTotalAmount() + "\n\n" +
	            "📍 *Address:*\n" + order.getAddress() + "\n\n" +
	            "📄 *Invoice:*\n" + invoiceUrl + "\n\n" +
	            "🔎 Track Order:\n" +
	            "https://esophagus-udder-senorita.ngrok-free.dev/track/" + order.getId()
	    );

	    whatsAppService.sendWhatsApp(
	            adminNumber,
	            "🚨 *New Paid Order Received!*\n\n" +
	            "🧾 *Order ID:* #" + order.getId() + "\n" +
	            "👤 *Customer:* " + order.getCustomerNameSnapshot() + "\n" +
	            "📞 *Phone:* " + order.getCustomerPhoneSnapshot() + "\n" +
	            "💰 *Amount:* ₹" + order.getTotalAmount()+"\n\n"+
	            "📍 *Address:*\n" + order.getAddress() 
	    );

	    cartService.clear();
	    
	    model.addAttribute("order", order);

	    return "order-success";
	    
		 }catch(Exception e) {
			 e.printStackTrace();
			 return "redirect:/checkout/payment-failed";
		 }
	}
	
	
	@GetMapping("/payment-failed")
	public String paymentFailed() {
	    return "payment-failed";
	}
	
	

	@GetMapping("/api/pincode/{pincode}")
	@ResponseBody
	public Map<String, String> getLocationByPincode(@PathVariable String pincode) {

	    Map<String, String> result = new HashMap<>();

	    Optional<PincodeData> existing = pincodeRepository.findByPincode(pincode);

	    if (existing.isPresent()) {
	        PincodeData data = existing.get();

	        result.put("city", data.getCity());
	        result.put("district", data.getDistrict());
	        result.put("state", data.getState());

	        return result;
	    }

	    try {
	        RestTemplate restTemplate = new RestTemplate();

	        String url = "https://pinlookup.in/api/pincode?pincode=" + pincode;

	        Map response = restTemplate.getForObject(url, Map.class);

	        if (response != null && response.get("success") != null) {

	            Map data = (Map) response.get("data");

	            String city = String.valueOf(data.get("office_name"));
	            String district = String.valueOf(data.get("district"));
	            String state = String.valueOf(data.get("state"));

	            PincodeData saveData = new PincodeData();
	            saveData.setPincode(pincode);
	            saveData.setCity(city);
	            saveData.setDistrict(district);
	            saveData.setState(state);

	            pincodeRepository.save(saveData);

	            result.put("city", city);
	            result.put("district", district);
	            result.put("state", state);

	            return result;
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    result.put("city", "");
	    result.put("district", "");
	    result.put("state", "");

	    return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
