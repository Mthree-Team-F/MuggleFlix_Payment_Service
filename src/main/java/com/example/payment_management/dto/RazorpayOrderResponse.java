package com.example.payment_management.dto;


public record RazorpayOrderResponse(
        String orderId,
        String amount) {
}
