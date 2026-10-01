package com.hostel.controller;

import com.hostel.dto.ComplaintDtos.AdminCreateComplaint;
import com.hostel.dto.ComplaintDtos.UpdateComplaint;
import com.hostel.exception.ResourceNotFoundException;
import com.hostel.model.Complaint;
import com.hostel.repository.ComplaintRepository;
import com.hostel.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {
    private final ComplaintRepository complaints;
    private final StudentRepository students;

    @GetMapping
    public List<Complaint> list(@RequestParam(required = false) Complaint.Status status) {
        return status == null ? complaints.findAll() : complaints.findByStatus(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Complaint create(@Valid @RequestBody AdminCreateComplaint r) {
        Complaint c = new Complaint();
        c.setStudent(students.findById(r.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + r.studentId())));
        c.setTitle(r.title());
        c.setDescription(r.description());
        c.setStatus(Complaint.Status.OPEN);
        c.setCreatedAt(LocalDateTime.now());
        return complaints.save(c);
    }

    @PutMapping("/{id}")
    public Complaint update(@PathVariable Long id, @Valid @RequestBody UpdateComplaint r) {
        Complaint c = complaints.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));
        c.setStatus(r.status());
        c.setResolution(r.resolution());
        return complaints.save(c);
    }
}
