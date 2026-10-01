package com.hostel.repository;

import com.hostel.model.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllocationRepository extends JpaRepository<Allocation, Long> {
    long countByRoomIdAndActiveTrue(Long roomId);
    long countByActiveTrue();
    boolean existsByStudentIdAndActiveTrue(Long studentId);
    boolean existsByRoomIdAndActiveTrue(Long roomId);
    List<Allocation> findByActiveTrue();
    List<Allocation> findByStudentId(Long studentId);
}
