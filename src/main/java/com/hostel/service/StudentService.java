package com.hostel.service;

import com.hostel.dto.StudentRequest;
import com.hostel.exception.BadRequestException;
import com.hostel.exception.ResourceNotFoundException;
import com.hostel.model.Student;
import com.hostel.model.User;
import com.hostel.repository.AllocationRepository;
import com.hostel.repository.StudentRepository;
import com.hostel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository students;
    private final UserRepository users;
    private final AllocationRepository allocations;
    private final PasswordEncoder encoder;

    public List<Student> findAll(String search) {
        return (search == null || search.isBlank())
                ? students.findAll()
                : students.findByFullNameContainingIgnoreCase(search);
    }

    public Student get(Long id) {
        return students.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    @Transactional
    public Student create(StudentRequest r) {
        if (students.existsByEmail(r.email()) || users.existsByUsername(r.email())) {
            throw new BadRequestException("A student with this email already exists");
        }
        User u = new User();
        u.setUsername(r.email());
        u.setPassword(encoder.encode(r.password() == null || r.password().isBlank() ? "student123" : r.password()));
        u.setRole(User.Role.STUDENT);
        users.save(u);

        Student s = new Student();
        apply(s, r);
        s.setEmail(r.email());
        s.setUser(u);
        return students.save(s);
    }

    @Transactional
    public Student update(Long id, StudentRequest r) {
        Student s = get(id);
        apply(s, r);
        return students.save(s);
    }

    @Transactional
    public void delete(Long id) {
        Student s = get(id);
        if (allocations.existsByStudentIdAndActiveTrue(id)) {
            throw new BadRequestException("Vacate the student's room before deleting the student");
        }
        User u = s.getUser();
        students.delete(s);
        students.flush();
        if (u != null) {
            users.delete(u);
        }
    }

    private void apply(Student s, StudentRequest r) {
        s.setFullName(r.fullName());
        s.setPhone(r.phone());
        s.setCourse(r.course());
        s.setAcademicYear(r.academicYear());
        s.setGuardianName(r.guardianName());
        s.setGuardianPhone(r.guardianPhone());
        s.setAddress(r.address());
    }
}
