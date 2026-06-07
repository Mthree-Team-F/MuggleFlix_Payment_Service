package com.example.payment_management.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePaymentStatusRequest(
        @NotBlank
        String status,

        String razorpayPaymentId
) {
}
