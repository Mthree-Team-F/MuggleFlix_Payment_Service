package com.example.payment_management.producer;

import com.example.payment_management.dto.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentResultProducer {

    private static final String TOPIC = "payment-result";

    private final KafkaTemplate<String, PaymentResultEvent> kafkaTemplate;

    public void publish(PaymentResultEvent event) {
        log.info("Publishing payment result event to topic '{}': {}", TOPIC, event);
        kafkaTemplate.send(TOPIC, event.getPaymentId().toString(), event);
        log.info("Payment result event published successfully");
    }
}
