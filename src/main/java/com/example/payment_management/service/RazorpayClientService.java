package com.example.payment_management.service;

import com.example.payment_management.dto.CreatePaymentRequest;
import com.example.payment_management.dto.CreatePaymentRequest;
import com.example.payment_management.dto.RazorpayOrderResponse;
public interface RazorpayClientService {
    RazorpayOrderResponse createOrder(
            CreatePaymentRequest request);

    boolean verifySignature(
            String orderId,
            String paymentId,
            String signature);
}
