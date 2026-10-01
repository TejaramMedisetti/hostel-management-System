package com.hostel.controller;

import com.hostel.dto.ComplaintDtos.CreateComplaint;
import com.hostel.exception.ResourceNotFoundException;
import com.hostel.model.Allocation;
import com.hostel.model.Complaint;
import com.hostel.model.Payment;
import com.hostel.model.Student;
import com.hostel.repository.AllocationRepository;
import com.hostel.repository.ComplaintRepository;
import com.hostel.repository.PaymentRepository;
import com.hostel.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** Endpoints for a logged-in student to see/manage only their own data. */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {
    private final StudentRepository students;
    private final AllocationRepository allocations;
    private final PaymentRepository payments;
    private final ComplaintRepository complaints;

    private Student current(Authentication auth) {
        return students.findByUserUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("No student profile linked to this account"));
    }

    @GetMapping("/profile")
    public Student profile(Authentication auth) {
        return current(auth);
    }

    @GetMapping("/allocations")
    public List<Allocation> myAllocations(Authentication auth) {
        return allocations.findByStudentId(current(auth).getId());
    }

    @GetMapping("/payments")
    public List<Payment> myPayments(Authentication auth) {
        return payments.findByStudentId(current(auth).getId());
    }

    @GetMapping("/complaints")
    public List<Complaint> myComplaints(Authentication auth) {
        return complaints.findByStudentId(current(auth).getId());
    }

    @PostMapping("/complaints")
    @ResponseStatus(HttpStatus.CREATED)
    public Complaint raiseComplaint(Authentication auth, @Valid @RequestBody CreateComplaint r) {
        Complaint c = new Complaint();
        c.setStudent(current(auth));
        c.setTitle(r.title());
        c.setDescription(r.description());
        c.setStatus(Complaint.Status.OPEN);
        c.setCreatedAt(LocalDateTime.now());
        return complaints.save(c);
    }
}
