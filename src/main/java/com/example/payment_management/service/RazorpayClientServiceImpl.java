package com.example.payment_management.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.example.payment_management.dto.CreatePaymentRequest;
import com.example.payment_management.dto.RazorpayOrderResponse;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.springframework.beans.factory.annotation.Value;

@Service
public class RazorpayClientServiceImpl
        implements RazorpayClientService {

    private final RazorpayClient razorpayClient;
    private final String keySecret;
    public RazorpayClientServiceImpl(
            RazorpayClient razorpayClient,@Value("${razorpay.key.secret}") String keySecret) {

        this.razorpayClient = razorpayClient;
         this.keySecret = keySecret;
    }

    @Override
    public RazorpayOrderResponse createOrder(
            CreatePaymentRequest request) {

        try {

            JSONObject orderRequest =
                    new JSONObject();

            orderRequest.put(
                    "amount",
                    request.amount().multiply(
                            java.math.BigDecimal.valueOf(100))
                            .intValue());

            orderRequest.put(
                    "currency",
                    "INR");

            orderRequest.put(
                    "receipt",
                    "receipt_" + System.currentTimeMillis());

            Order order =
                    razorpayClient.orders.create(
                            orderRequest);

            return new RazorpayOrderResponse(
                    order.get("id"),
                    order.get("amount").toString());

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create Razorpay order",
                    e);
        }
    }

    @Override
    public boolean verifySignature(
            String orderId,
            String paymentId,
            String signature) {

        try {

            JSONObject attributes =
                    new JSONObject();

            attributes.put(
                    "razorpay_order_id",
                    orderId);

            attributes.put(
                    "razorpay_payment_id",
                    paymentId);

            attributes.put(
                    "razorpay_signature",
                    signature);

            return Utils.verifyPaymentSignature(
                    attributes,
                    keySecret);

        } catch (Exception e) {

            return false;
        }
    }
}