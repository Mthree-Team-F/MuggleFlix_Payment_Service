package com.example.payment_management.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.example.payment_management.entity.Payment;
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
     Optional<Payment> findByRazorpayOrderId(
            String razorpayOrderId);
}
