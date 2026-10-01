package com.hostel.repository;

import com.hostel.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStatus(Payment.Status status);
    List<Payment> findByStudentId(Long studentId);
    long countByStatus(Payment.Status status);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status")
    BigDecimal totalByStatus(@Param("status") Payment.Status status);
}
