package com.example.payment_management.service;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.payment_management.dto.CreatePaymentRequest;
import com.example.payment_management.dto.RazorpayOrderResponse;
import com.example.payment_management.entity.Payment;
import com.example.payment_management.entity.PaymentStatus;
import com.example.payment_management.exception.PaymentNotFoundException;
import com.example.payment_management.repository.PaymentRepository;
import com.example.payment_management.service.PaymentService;
import com.example.payment_management.service.RazorpayClientService;
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClientService razorpayClientService;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            RazorpayClientService razorpayClientService) {

        this.paymentRepository = paymentRepository;
        this.razorpayClientService = razorpayClientService;
    }
     @Override
    public Payment createPayment(CreatePaymentRequest request) {

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }

        RazorpayOrderResponse razorpayOrder =
                razorpayClientService.createOrder(request);

        Payment payment = new Payment();

        payment.setUserId(request.userId());
        payment.setAmount(request.amount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRazorpayOrderId(
                razorpayOrder.orderId());

        return paymentRepository.save(payment);
    }
    @Override
    public void verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String signature) {

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(razorpayOrderId)
                        .orElseThrow(
                                () -> new PaymentNotFoundException(
                                        "Payment not found"));

        boolean valid =
                razorpayClientService.verifySignature(
                        razorpayOrderId,
                        razorpayPaymentId,
                        signature);

        if (valid) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);}
}
