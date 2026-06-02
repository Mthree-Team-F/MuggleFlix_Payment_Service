package com.example.payment_management.service;

import com.example.payment_management.entity.Payment;
import com.example.payment_management.dto.CreatePaymentRequest;
public interface PaymentService {
    Payment createPayment(CreatePaymentRequest request);

    void verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String signature);
}
