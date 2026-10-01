package com.hostel.controller;

import com.hostel.dto.DashboardStats;
import com.hostel.model.Complaint;
import com.hostel.model.Payment;
import com.hostel.model.Room;
import com.hostel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final StudentRepository students;
    private final RoomRepository rooms;
    private final AllocationRepository allocations;
    private final PaymentRepository payments;
    private final ComplaintRepository complaints;
    private final VisitorRepository visitors;

    @GetMapping
    public DashboardStats stats() {
        long beds = rooms.findAll().stream().mapToLong(Room::getCapacity).sum();
        long occupied = allocations.countByActiveTrue();
        return new DashboardStats(
                students.count(),
                rooms.count(),
                beds,
                occupied,
                Math.max(0, beds - occupied),
                payments.countByStatus(Payment.Status.PENDING),
                payments.totalByStatus(Payment.Status.PAID),
                payments.totalByStatus(Payment.Status.PENDING),
                complaints.countByStatus(Complaint.Status.OPEN),
                visitors.findByOutTimeIsNull().size());
    }
}
