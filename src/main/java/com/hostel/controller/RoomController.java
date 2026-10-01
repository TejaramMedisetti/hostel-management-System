package com.hostel.controller;

import com.hostel.dto.RoomRequest;
import com.hostel.dto.RoomResponse;
import com.hostel.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService service;

    @GetMapping
    public List<RoomResponse> list(@RequestParam(defaultValue = "false") boolean availableOnly) {
        return service.findAll(availableOnly);
    }

    @GetMapping("/{id}")
    public RoomResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@Valid @RequestBody RoomRequest r) {
        return service.create(r);
    }

    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable Long id, @Valid @RequestBody RoomRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
