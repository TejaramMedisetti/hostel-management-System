package com.hostel.dto;

import java.math.BigDecimal;

public record DashboardStats(long totalStudents, long totalRooms, long totalBeds, long occupiedBeds,
                             long availableBeds, long pendingPayments, BigDecimal revenueCollected,
                             BigDecimal revenuePending, long openComplaints, long visitorsInside) {}
