package com.example.payment_management.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.payment_management.dto.ApiResponse;
import com.example.payment_management.dto.CreatePaymentRequest;
import com.example.payment_management.dto.VerifyPaymentRequest;
import com.example.payment_management.entity.Payment;
import com.example.payment_management.service.PaymentService;
import com.example.payment_management.dto.UpdatePaymentStatusRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<Payment> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        Payment payment =
                paymentService.createPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(payment);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {

        paymentService.verifyPayment(
                request.orderId(),
                request.paymentId(),
                request.signature());

        return ResponseEntity.ok(
                new ApiResponse(
                        "Payment verified successfully"));
    }

        @GetMapping("/{paymentId}")
        public ResponseEntity<Payment> getPaymentById(
                @PathVariable Long paymentId) {

            Payment payment =
                    paymentService.getPayment(paymentId);

            return ResponseEntity.ok(payment);
        }

        @GetMapping("/")
        public ResponseEntity<?> getAllPayments() {

            return ResponseEntity.ok(
                    paymentService.getAllPayments());
        }

        @DeleteMapping("/delete/{paymentId}")
        public ResponseEntity<Void> deletePayment(@PathVariable Long paymentId) {
                paymentService.deletePayment(paymentId);
                return  ResponseEntity.noContent().build();
        }

            @PutMapping("/{paymentId}/status")
            public ResponseEntity<Payment> updatePaymentStatus(
                    @PathVariable Long paymentId,
                    @Valid @RequestBody UpdatePaymentStatusRequest request) {

                Payment updated = paymentService.updatePaymentStatus(
                        paymentId,
                        request.status(),
                        request.razorpayPaymentId());

                return ResponseEntity.ok(updated);
            }

}