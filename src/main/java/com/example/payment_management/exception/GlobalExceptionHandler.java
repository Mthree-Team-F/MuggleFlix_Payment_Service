package com.example.payment_management.exception;

import com.example.payment_management.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse>
    handlePaymentNotFound(
            PaymentNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(
                        ex.getMessage()));
    }

    @ExceptionHandler(
            IllegalArgumentException.class)
    public ResponseEntity<ApiResponse>
    handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity
                .badRequest()
                .body(new ApiResponse(
                        ex.getMessage()));
    }
}