package com.example.payment_management.consumer;

import com.example.payment_management.dto.PaymentRequestEvent;
import com.example.payment_management.dto.CreatePaymentRequest;
import com.example.payment_management.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "payment-request",
            groupId = "payment-group")
    public void consumePaymentRequest(PaymentRequestEvent event) {
        log.info("Received payment request event: {}", event);

        try {
            // Create a payment request from the subscription event
            CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                    event.getSubscriptionId(),
                    event.getUserId(),
                    event.getAmount(),
                    event.getPlanName()
            );

            log.info("Creating payment for subscription: {}, amount: {}", 
                    event.getSubscriptionId(), event.getAmount());

            paymentService.createPayment(paymentRequest);

            log.info("Payment created successfully for subscription: {}", 
                    event.getSubscriptionId());

        } catch (Exception e) {
            log.error("Error processing payment request event: {}", event, e);
            // Implement retry logic or dead-letter queue here
        }
    }
}
