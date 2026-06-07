package com.example.payment_management.dto;

import java.math.BigDecimal;

public record PaymentEvent(
        Long paymentId,
        Long userId,
        String eventType,
        String status,
        BigDecimal amount
) {}