package com.hostel.controller;

import com.hostel.dto.VisitorRequest;
import com.hostel.exception.BadRequestException;
import com.hostel.exception.ResourceNotFoundException;
import com.hostel.model.Visitor;
import com.hostel.repository.StudentRepository;
import com.hostel.repository.VisitorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
public class VisitorController {
    private final VisitorRepository visitors;
    private final StudentRepository students;

    @GetMapping
    public List<Visitor> list(@RequestParam(defaultValue = "false") boolean insideOnly) {
        return insideOnly ? visitors.findByOutTimeIsNull() : visitors.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Visitor checkIn(@Valid @RequestBody VisitorRequest r) {
        Visitor v = new Visitor();
        v.setStudent(students.findById(r.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + r.studentId())));
        v.setVisitorName(r.visitorName());
        v.setPurpose(r.purpose());
        v.setInTime(LocalDateTime.now());
        return visitors.save(v);
    }

    @PutMapping("/{id}/checkout")
    public Visitor checkOut(@PathVariable Long id) {
        Visitor v = visitors.findById(id).orElseThrow(() -> new ResourceNotFoundException("Visitor not found: " + id));
        if (v.getOutTime() != null) {
            throw new BadRequestException("Visitor already checked out");
        }
        v.setOutTime(LocalDateTime.now());
        return visitors.save(v);
    }
}
