package com.example.payment_management.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequest(
         @NotNull
        Long userId,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotBlank
        String currency
) {
}