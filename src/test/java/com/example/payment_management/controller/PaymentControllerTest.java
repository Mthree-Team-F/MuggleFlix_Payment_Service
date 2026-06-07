package com.example.payment_management.controller;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.payment_management.entity.Payment;
import com.example.payment_management.entity.PaymentStatus;
import com.example.payment_management.exception.PaymentNotFoundException;
import com.example.payment_management.service.PaymentService;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    void shouldCreatePaymentSuccessfully() throws Exception {

        Payment payment = new Payment();

        payment.setId(1L);
        payment.setUserId(101L);
        payment.setAmount(BigDecimal.valueOf(5000));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRazorpayOrderId("order_123");

        when(paymentService.createPayment(any()))
                .thenReturn(payment);

        String request = """
                {
                    "userId":101,
                    "amount":5000,
                    "currency": "INR"
                }
                """;

        mockMvc.perform(
                post("/payments/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(101))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.razorpayOrderId")
                        .value("order_123"));
    }

    @Test
    void shouldCallServiceWhenCreatingPayment()
            throws Exception {

        Payment payment = new Payment();

        when(paymentService.createPayment(any()))
                .thenReturn(payment);

        String request = """
                {
                    "userId":101,
                    "amount":5000,
                    "currency": "INR"
                }
                """;

        mockMvc.perform(
                post("/payments/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request));

        verify(paymentService)
                .createPayment(any());
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsMissing()
            throws Exception {

        String request = """
                {
                    "userId":101
                }
                """;

        mockMvc.perform(
                post("/payments/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyInvalid()
            throws Exception {

        String request = """
                {
                    "userId":"abc",
                    "amount":"xyz"
                }
                """;

        mockMvc.perform(
                post("/payments/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldVerifyPaymentSuccessfully()
            throws Exception {

        Payment payment = new Payment();

payment.setStatus(
        PaymentStatus.SUCCESS);

when(paymentService.verifyPayment(
        any(),
        any(),
        any()))
        .thenReturn(payment);

        String request = """
                {
                    "orderId":"order_123",
                    "paymentId":"pay_123",
                    "signature":"signature"
                }
                """;

        mockMvc.perform(
                post("/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(paymentService)
                .verifyPayment(
                        "order_123",
                        "pay_123",
                        "signature");
    }

    @Test
    void shouldReturnNotFoundWhenPaymentDoesNotExist()
            throws Exception {

        doThrow(
                new PaymentNotFoundException(
                        "Payment not found"))
                .when(paymentService)
                .verifyPayment(
                        any(),
                        any(),
                        any());

        String request = """
                {
                    "orderId":"order_123",
                    "paymentId":"pay_123",
                    "signature":"signature"
                }
                """;

        mockMvc.perform(
                post("/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenVerifyRequestInvalid()
            throws Exception {

        String request = """
                {
                    "orderId":"",
                    "paymentId":"",
                    "signature":""
                }
                """;

        mockMvc.perform(
                post("/payments/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
    @Test
void shouldGetPaymentById()
        throws Exception {

    Payment payment = new Payment();

    payment.setId(1L);
    payment.setUserId(101L);

    when(paymentService.getPayment(1L))
            .thenReturn(payment);

    mockMvc.perform(
            get("/payments/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id")
                    .value(1));
}
@Test
void shouldGetAllPayments()
        throws Exception {

    when(paymentService.getAllPayments())
            .thenReturn(List.of(
                    new Payment(),
                    new Payment()));

    mockMvc.perform(
            get("/payments/"))
            .andExpect(status().isOk());
}
@Test
void shouldDeletePayment()
        throws Exception {

    doNothing()
            .when(paymentService)
            .deletePayment(1L);

    mockMvc.perform(
            delete(
                    "/payments/delete/1"))
            .andExpect(status().isNoContent());

    verify(paymentService)
            .deletePayment(1L);
}
}