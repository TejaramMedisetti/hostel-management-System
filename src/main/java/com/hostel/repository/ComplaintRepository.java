package com.hostel.repository;

import com.hostel.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStatus(Complaint.Status status);
    List<Complaint> findByStudentId(Long studentId);
    long countByStatus(Complaint.Status status);
}
