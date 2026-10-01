package com.hostel.controller;

import com.hostel.dto.PaymentRequest;
import com.hostel.exception.BadRequestException;
import com.hostel.exception.ResourceNotFoundException;
import com.hostel.model.Payment;
import com.hostel.repository.PaymentRepository;
import com.hostel.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentRepository payments;
    private final StudentRepository students;

    @GetMapping
    public List<Payment> list(@RequestParam(required = false) Payment.Status status) {
        return status == null ? payments.findAll() : payments.findByStatus(status);
    }

    @GetMapping("/student/{studentId}")
    public List<Payment> byStudent(@PathVariable Long studentId) {
        return payments.findByStudentId(studentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Payment create(@Valid @RequestBody PaymentRequest r) {
        Payment p = new Payment();
        p.setStudent(students.findById(r.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + r.studentId())));
        p.setAmount(r.amount());
        p.setForMonth(r.forMonth());
        p.setMethod(r.method());
        p.setStatus(r.status() == null ? Payment.Status.PAID : r.status());
        if (p.getStatus() == Payment.Status.PAID) {
            p.setPaidOn(LocalDate.now());
        }
        return payments.save(p);
    }

    @PutMapping("/{id}/pay")
    public Payment markPaid(@PathVariable Long id, @RequestParam(defaultValue = "CASH") String method) {
        Payment p = payments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        if (p.getStatus() == Payment.Status.PAID) {
            throw new BadRequestException("Payment already marked as paid");
        }
        p.setStatus(Payment.Status.PAID);
        p.setPaidOn(LocalDate.now());
        p.setMethod(method);
        return payments.save(p);
    }
}
