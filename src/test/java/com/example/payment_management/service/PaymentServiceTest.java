package com.example.payment_management.service;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.payment_management.entity.Payment;
import com.example.payment_management.entity.PaymentStatus;
import com.example.payment_management.repository.PaymentRepository;
import com.example.payment_management.service.PaymentServiceImpl;
import com.example.payment_management.service.RazorpayClientServiceImpl;
import com.example.payment_management.dto.CreatePaymentRequest;
import com.example.payment_management.dto.RazorpayOrderResponse;
import java.math.BigDecimal;
import java.util.Optional;
import com.example.payment_management.exception.PaymentNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
private PaymentRepository paymentRepository;

@Mock
private RazorpayClientServiceImpl razorpayClientService;

@InjectMocks
private PaymentServiceImpl paymentService;

@Test
void shouldCreatePaymentSuccessfully() {

    CreatePaymentRequest request =
            new CreatePaymentRequest(
                    101L,
                    BigDecimal.valueOf(5000)
            );

    RazorpayOrderResponse razorpayResponse =
            new RazorpayOrderResponse(
                    "order_123",
                    "5000"
            );

    when(razorpayClientService.createOrder(any()))
            .thenReturn(razorpayResponse);

    when(paymentRepository.save(any(Payment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    Payment payment =
            paymentService.createPayment(request);

    assertThat(payment).isNotNull();

    assertThat(payment.getUserId())
            .isEqualTo(101L);

    assertThat(payment.getStatus())
            .isEqualTo(PaymentStatus.PENDING);

    assertThat(payment.getRazorpayOrderId())
            .isEqualTo("order_123");
}

@Test
void shouldThrowExceptionWhenAmountIsZero() {

    CreatePaymentRequest request =
            new CreatePaymentRequest(
                    101L,
                    BigDecimal.ZERO
            );

    assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.createPayment(request)
    );
}

@Test
void shouldThrowExceptionWhenRazorpayOrderCreationFails() {

    CreatePaymentRequest request =
            new CreatePaymentRequest(
                    101L,
                    BigDecimal.valueOf(1000)
            );

    when(razorpayClientService.createOrder(any()))
            .thenThrow(
                    new RuntimeException("Razorpay unavailable")
            );

    assertThrows(
            RuntimeException.class,
            () -> paymentService.createPayment(request)
    );

    verify(paymentRepository, never())
            .save(any());
}

@Test
void shouldSavePaymentInDatabase() {

    CreatePaymentRequest request =
            new CreatePaymentRequest(
                    101L,
                    BigDecimal.valueOf(2000)
            );

    when(razorpayClientService.createOrder(any()))
            .thenReturn(
                    new RazorpayOrderResponse(
                            "order_456",
                            "2000"
                    )
            );

    when(paymentRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

    paymentService.createPayment(request);

    verify(paymentRepository)
            .save(any(Payment.class));
}

@Test
void shouldMarkPaymentAsSuccessWhenSignatureIsValid() {

    Payment payment = new Payment();

    payment.setId(1L);
    payment.setStatus(PaymentStatus.PENDING);

    when(paymentRepository.findByRazorpayOrderId("order_123"))
            .thenReturn(Optional.of(payment));

    when(
            razorpayClientService.verifySignature(
                    any(),
                    any(),
                    any()
            )
    ).thenReturn(true);

    paymentService.verifyPayment(
            "order_123",
            "pay_123",
            "signature"
    );

    assertThat(payment.getStatus())
            .isEqualTo(PaymentStatus.SUCCESS);

    verify(paymentRepository)
            .save(payment);
}

@Test
void shouldMarkPaymentAsFailedWhenSignatureIsInvalid() {

    Payment payment = new Payment();

    payment.setStatus(PaymentStatus.PENDING);

    when(paymentRepository.findByRazorpayOrderId(any()))
            .thenReturn(Optional.of(payment));

    when(
            razorpayClientService.verifySignature(
                    any(),
                    any(),
                    any()
            )
    ).thenReturn(false);

    paymentService.verifyPayment(
            "order_123",
            "pay_123",
            "invalid"
    );

    assertThat(payment.getStatus())
            .isEqualTo(PaymentStatus.FAILED);
}

@Test
void shouldThrowExceptionWhenPaymentNotFound() {

    when(paymentRepository.findByRazorpayOrderId(any()))
            .thenReturn(Optional.empty());

    assertThrows(
            PaymentNotFoundException.class,
            () -> paymentService.verifyPayment(
                    "order_123",
                    "pay_123",
                    "signature"
            )
    );
}
}
