package com.example.payment_management.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultEvent {
    private Long paymentId;
    private Long userId;
    private String eventType;
    private String status;
    private BigDecimal amount;
}
