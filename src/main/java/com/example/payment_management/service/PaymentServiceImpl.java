package com.example.payment_management.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.payment_management.dto.CreatePaymentRequest;
import com.example.payment_management.dto.RazorpayOrderResponse;
import com.example.payment_management.entity.Payment;
import com.example.payment_management.entity.PaymentStatus;
import com.example.payment_management.exception.PaymentNotFoundException;
import com.example.payment_management.dto.PaymentEvent;
import com.example.payment_management.dto.PaymentResultEvent;
import com.example.payment_management.producer.PaymentEventProducer;
import com.example.payment_management.producer.PaymentResultProducer;
import com.example.payment_management.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentServiceImpl
        implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final RazorpayClientService
            razorpayClientService;

    private final PaymentEventProducer
            paymentEventProducer;

    private final PaymentResultProducer
            paymentResultProducer;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            RazorpayClientService razorpayClientService,
            PaymentEventProducer paymentEventProducer,
            PaymentResultProducer paymentResultProducer) {

        this.paymentRepository = paymentRepository;
        this.razorpayClientService = razorpayClientService;
        this.paymentEventProducer = paymentEventProducer;
        this.paymentResultProducer = paymentResultProducer;
    }

    @Override
    public Payment createPayment(
            CreatePaymentRequest request) {

        if (request.amount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero");
        }

        RazorpayOrderResponse razorpayOrder =
                razorpayClientService
                        .createOrder(request);

        Payment payment = new Payment();

        payment.setSubscriptionId(request.subscriptionId());
        payment.setUserId(request.userId());
        payment.setAmount(request.amount());
        payment.setPlanName(request.planName());
        payment.setCurrency("INR");

        payment.setStatus(
                PaymentStatus.PENDING);

        payment.setRazorpayOrderId(
                razorpayOrder.orderId());

        payment.setCreatedAt(
                LocalDateTime.now());

        payment.setUpdatedAt(
                LocalDateTime.now());

        Payment saved =
                paymentRepository.save(payment);

        paymentEventProducer.publish(
                new PaymentEvent(
                        saved.getId(),
                        saved.getUserId(),
                        "PAYMENT_CREATED",
                        saved.getStatus().name(),
                        saved.getAmount()
                ));

        return saved;
    }

    @Override
    public Payment verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String signature) {

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(
                                razorpayOrderId)
                        .orElseThrow(
                                () ->
                                        new PaymentNotFoundException(
                                                "Payment not found"));

        boolean valid =
                razorpayClientService
                        .verifySignature(
                                razorpayOrderId,
                                razorpayPaymentId,
                                signature);

        payment.setRazorpayPaymentId(
                razorpayPaymentId);

        payment.setUpdatedAt(
                LocalDateTime.now());

        if (valid) {

            payment.setStatus(
                    PaymentStatus.SUCCESS);

        } else {

            payment.setStatus(
                    PaymentStatus.FAILED);
        }

        Payment updated =
                paymentRepository.save(payment);

        // Publish to payment-events topic (for internal payment service listeners)
        paymentEventProducer.publish(
                new PaymentEvent(
                        updated.getId(),
                        updated.getUserId(),
                        valid
                                ? "PAYMENT_SUCCESS"
                                : "PAYMENT_FAILED",
                        updated.getStatus().name(),
                        updated.getAmount()
                ));

        // Publish to payment-result topic (for subscription service)
        PaymentResultEvent resultEvent = new PaymentResultEvent(
                updated.getId(),
                updated.getUserId(),
                valid ? "PAYMENT_SUCCESS" : "PAYMENT_FAILED",
                updated.getStatus().name(),
                updated.getAmount()
        );
        
        log.info("Publishing payment result to subscription service: {}", resultEvent);
        paymentResultProducer.publish(resultEvent);

        return updated;
    }

        @Override
        public Payment updatePaymentStatus(Long paymentId, String status, String razorpayPaymentId) {

                Payment payment = getPayment(paymentId);

                PaymentStatus statusEnum;

                try {
                        statusEnum = PaymentStatus.valueOf(status.toUpperCase());
                } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid payment status: " + status);
                }

                if (razorpayPaymentId != null && !razorpayPaymentId.isBlank()) {
                        payment.setRazorpayPaymentId(razorpayPaymentId);
                }

                payment.setStatus(statusEnum);

                payment.setUpdatedAt(LocalDateTime.now());

                Payment updated = paymentRepository.save(payment);

                paymentEventProducer.publish(
                                new PaymentEvent(
                                                updated.getId(),
                                                updated.getUserId(),
                                                "PAYMENT_" + statusEnum.name(),
                                                updated.getStatus().name(),
                                                updated.getAmount()
                                ));

                return updated;
        }

    @Override
    public Payment getPayment(
            Long paymentId) {

        return paymentRepository
                .findById(paymentId)
                .orElseThrow(
                        () ->
                                new PaymentNotFoundException(
                                        "Payment not found"));
    }

    @Override
    public List<Payment> getAllPayments() {

        return paymentRepository.findAll();
    }

    @Override
    public void deletePayment(
            Long paymentId) {

        Payment payment =
                getPayment(paymentId);

        paymentRepository.delete(payment);

        paymentEventProducer.publish(
                new PaymentEvent(
                        payment.getId(),
                        payment.getUserId(),
                        "PAYMENT_DELETED",
                        payment.getStatus().name(),
                        payment.getAmount()
                ));
    }
}