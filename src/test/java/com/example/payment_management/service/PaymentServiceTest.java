package com.example.payment_management.service;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import com.example.payment_management.producer.PaymentEventProducer;
import com.example.payment_management.producer.PaymentResultProducer;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.payment_management.entity.Payment;
import com.example.payment_management.entity.PaymentStatus;
import com.example.payment_management.repository.PaymentRepository;
import com.example.payment_management.service.PaymentServiceImpl;
import com.example.payment_management.service.RazorpayClientService;
import com.example.payment_management.dto.CreatePaymentRequest;
import com.example.payment_management.dto.RazorpayOrderResponse;
import java.math.BigDecimal;
import java.util.Optional;
import java.time.LocalDateTime;
import com.example.payment_management.exception.PaymentNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentEventProducer paymentEventProducer;
    @Mock
    private PaymentResultProducer paymentResultProducer;
    @Mock
    private RazorpayClientService razorpayClientService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

@Test
void shouldCreatePaymentSuccessfully() {

    CreatePaymentRequest request =
            new CreatePaymentRequest(
                    1L,
                    101L,
                    BigDecimal.valueOf(5000),
                    "MONTHLY"
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

    verify(paymentEventProducer)
        .publish(any());
}

@Test
void shouldThrowExceptionWhenAmountIsZero() {

    CreatePaymentRequest request =
            new CreatePaymentRequest(
                    1L,
                    101L,
                    BigDecimal.ZERO,
                    "MONTHLY"
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
                    1L,
                    101L,
                    BigDecimal.valueOf(1000),
                    "MONTHLY"
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
                    1L,
                    101L,
                    BigDecimal.valueOf(2000),
                    "MONTHLY"
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
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    paymentService.verifyPayment(
            "order_123",
            "pay_123",
            "signature"
    );

    assertThat(payment.getStatus())
            .isEqualTo(PaymentStatus.SUCCESS);

    verify(paymentRepository)
            .save(payment);
    verify(paymentEventProducer)
        .publish(any());
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
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    paymentService.verifyPayment(
            "order_123",
            "pay_123",
            "invalid"
    );

    assertThat(payment.getStatus())
            .isEqualTo(PaymentStatus.FAILED);

    verify(paymentEventProducer)
        .publish(any());
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
@Test
void shouldGetPaymentById() {

    Payment payment = new Payment();
    payment.setId(1L);

    when(paymentRepository.findById(1L))
            .thenReturn(Optional.of(payment));

    Payment result =
            paymentService.getPayment(1L);

    assertThat(result.getId())
            .isEqualTo(1L);
}
@Test
void shouldThrowExceptionWhenGetPaymentNotFound() {

    when(paymentRepository.findById(1L))
            .thenReturn(Optional.empty());

    assertThrows(
            PaymentNotFoundException.class,
            () -> paymentService.getPayment(1L)
    );
}
@Test
void shouldReturnAllPayments() {

    Payment p1 = new Payment();
    Payment p2 = new Payment();

    when(paymentRepository.findAll())
            .thenReturn(List.of(p1, p2));

    assertThat(
            paymentService.getAllPayments())
            .hasSize(2);
}
@Test
void shouldDeletePayment() {

    Payment payment = new Payment();

    payment.setId(1L);
    payment.setUserId(101L);
    payment.setAmount(
            BigDecimal.valueOf(1000));
    payment.setStatus(PaymentStatus.SUCCESS);
    when(paymentRepository.findById(1L))
            .thenReturn(Optional.of(payment));

    paymentService.deletePayment(1L);

    verify(paymentRepository)
            .delete(payment);

    verify(paymentEventProducer)
            .publish(any());
}

@Test
void shouldUpdatePaymentStatusAndTimestampAndRazorpayId() {

    Payment payment = new Payment();
    payment.setId(1L);
    payment.setStatus(PaymentStatus.PENDING);
    LocalDateTime prev = LocalDateTime.now().minusDays(1);
    payment.setUpdatedAt(prev);

    when(paymentRepository.findById(1L))
            .thenReturn(Optional.of(payment));

    when(paymentRepository.save(any(Payment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    Payment updated = paymentService.updatePaymentStatus(1L, "SUCCESS", "pay_123");

    assertThat(updated.getStatus())
            .isEqualTo(PaymentStatus.SUCCESS);

    assertThat(updated.getRazorpayPaymentId())
            .isEqualTo("pay_123");

    assertThat(updated.getUpdatedAt())
            .isAfter(prev);

    verify(paymentEventProducer)
            .publish(any());
}

@Test
void shouldUpdateStatusWithoutRazorpayPaymentId() {

    Payment payment = new Payment();
    payment.setId(2L);
    payment.setStatus(PaymentStatus.PENDING);
    payment.setRazorpayPaymentId(null);

    when(paymentRepository.findById(2L))
            .thenReturn(Optional.of(payment));

    when(paymentRepository.save(any(Payment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    paymentService.updatePaymentStatus(2L, "CANCELLED", null);

    assertThat(payment.getStatus())
            .isEqualTo(PaymentStatus.CANCELLED);

    assertThat(payment.getRazorpayPaymentId())
            .isNull();

    verify(paymentEventProducer)
            .publish(any());
}
}
