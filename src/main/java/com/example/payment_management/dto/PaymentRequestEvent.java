package com.example.payment_management.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestEvent {
    private Long subscriptionId;
    private Long userId;
    private BigDecimal amount;
    private String planName;
    private String eventType;
}
