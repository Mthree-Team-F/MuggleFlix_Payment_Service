package com.example.payment_management.producer;

import com.example.payment_management.dto.PaymentEvent;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    private static final String TOPIC =
            "payment-events";

    private final KafkaTemplate<String, PaymentEvent>
            kafkaTemplate;

    public PaymentEventProducer(
            KafkaTemplate<String, PaymentEvent> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(PaymentEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.paymentId().toString(),
                event);
    }
}