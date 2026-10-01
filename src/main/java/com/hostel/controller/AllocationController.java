package com.hostel.controller;

import com.hostel.dto.AllocationRequest;
import com.hostel.model.Allocation;
import com.hostel.service.AllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
public class AllocationController {
    private final AllocationService service;

    @GetMapping
    public List<Allocation> list(@RequestParam(defaultValue = "true") boolean activeOnly) {
        return service.list(activeOnly);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Allocation allocate(@Valid @RequestBody AllocationRequest r) {
        return service.allocate(r.studentId(), r.roomId());
    }

    @PutMapping("/{id}/vacate")
    public Allocation vacate(@PathVariable Long id) {
        return service.vacate(id);
    }
}
