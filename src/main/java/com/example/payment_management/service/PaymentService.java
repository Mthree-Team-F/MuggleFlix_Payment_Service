package com.example.payment_management.service;
import java.util.List;
import com.example.payment_management.entity.Payment;
import com.example.payment_management.dto.CreatePaymentRequest;
public interface PaymentService {
    Payment createPayment(CreatePaymentRequest request);

    Payment verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String signature);

    Payment getPayment(Long paymentId);

        Payment updatePaymentStatus(Long paymentId, String status, String razorpayPaymentId);

    List<Payment> getAllPayments();

    void deletePayment(Long paymentId);
}
