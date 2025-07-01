package com.tryiton.core.payment.repository;




import com.tryiton.core.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;



public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
