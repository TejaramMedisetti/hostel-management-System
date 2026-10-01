package com.hostel.service;

import com.hostel.exception.BadRequestException;
import com.hostel.exception.ResourceNotFoundException;
import com.hostel.model.Allocation;
import com.hostel.model.Room;
import com.hostel.model.Student;
import com.hostel.repository.AllocationRepository;
import com.hostel.repository.RoomRepository;
import com.hostel.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllocationService {
    private final AllocationRepository allocations;
    private final StudentRepository students;
    private final RoomRepository rooms;

    public List<Allocation> list(boolean onlyActive) {
        return onlyActive ? allocations.findByActiveTrue() : allocations.findAll();
    }

    @Transactional
    public Allocation allocate(Long studentId, Long roomId) {
        Student s = students.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        Room r = rooms.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
        if (allocations.existsByStudentIdAndActiveTrue(studentId)) {
            throw new BadRequestException("Student already has an active room");
        }
        if (allocations.countByRoomIdAndActiveTrue(roomId) >= r.getCapacity()) {
            throw new BadRequestException("Room " + r.getRoomNumber() + " is full");
        }
        Allocation a = new Allocation();
        a.setStudent(s);
        a.setRoom(r);
        a.setAllocatedOn(LocalDate.now());
        a.setActive(true);
        return allocations.save(a);
    }

    @Transactional
    public Allocation vacate(Long allocationId) {
        Allocation a = allocations.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found: " + allocationId));
        if (!a.isActive()) {
            throw new BadRequestException("Allocation already closed");
        }
        a.setActive(false);
        a.setVacatedOn(LocalDate.now());
        return allocations.save(a);
    }
}
