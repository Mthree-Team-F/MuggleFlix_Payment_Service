package com.example.payment_management.exception;

public class PaymentNotFoundException
        extends RuntimeException {

    public PaymentNotFoundException(
            String message) {

        super(message);
    }
}