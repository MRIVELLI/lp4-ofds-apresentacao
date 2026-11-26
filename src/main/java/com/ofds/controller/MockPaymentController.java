package com.ofds.controller;

import com.ofds.model.*;
import com.ofds.repository.OrderRepository;
import com.ofds.repository.PaymentRepository;
import com.ofds.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
@RequestMapping("/payment")
public class MockPaymentController {

    @Autowired private UserService userService;
    @Autowired private RestaurantService restaurantService;
    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;
    private Random random = new Random();

    @PostMapping("/checkout")
    @Transactional
    public String checkout(@RequestParam Long restaurantId,
                           @RequestParam List<Long> itemIds,
                           @RequestParam List<Integer> quantities,
                           @RequestParam PaymentMethod paymentMethod,
                           @AuthenticationPrincipal UserDetails ud,
                           Model model) {
        try {
            Customer customer = userService.findCustomerByEmail(ud.getUsername()).orElseThrow();
            Order order = orderService.placeOrder(customer, restaurantId,
                    itemIds, quantities, paymentMethod, restaurantService);

            model.addAttribute("order", order);
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("customer", customer);
            return "payment/checkout";
        } catch (Exception e) {
            return "redirect:/customer/restaurant/" + restaurantId + "/menu?error=true";
        }
    }

    @PostMapping("/process")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> process(
            @RequestParam Long orderId,
            @RequestParam String cardOrUpiInput,
            @AuthenticationPrincipal UserDetails ud) {

        boolean success;
        String input = cardOrUpiInput.trim().replaceAll("\\s", "");

        if (input.equals("0000000000000000") || input.equals("fail@upi")) {
            success = false;
        } else if (input.startsWith("4") || input.contains("@") || input.length() >= 6) {
            success = random.nextInt(10) > 0;
        } else {
            success = false;
        }

        Customer customer = userService.findCustomerByEmail(ud.getUsername()).orElseThrow();
        Order order = orderService.getCustomerOrder(orderId, customer);
        Payment payment = order.getPayment();

        if (payment == null || payment.getPaymentStatus() != PaymentStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "failed",
                    "message", "Payment has already been processed or is unavailable.",
                    "orderId", orderId));
        }

        if (payment.getAmount() != order.getTotalAmount()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "failed",
                    "message", "Payment amount does not match order total.",
                    "orderId", orderId));
        }

        if (success) payment.makePayment();
        else payment.refundPayment();
        paymentRepository.save(payment);

        return ResponseEntity.ok(Map.of(
            "status", success ? "success" : "failed",
            "orderId", orderId,
            "txnId", "TXN" + System.currentTimeMillis()
        ));
    }
}
