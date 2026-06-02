package com.example.payment_management.dto;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        Long userId,
        BigDecimal amount
) {
}