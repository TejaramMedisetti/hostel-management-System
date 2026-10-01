package com.hostel.repository;

import com.hostel.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserUsername(String username);
    List<Student> findByFullNameContainingIgnoreCase(String name);
    boolean existsByEmail(String email);
}
