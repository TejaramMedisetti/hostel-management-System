package com.hostel.service;

import com.hostel.dto.RoomRequest;
import com.hostel.dto.RoomResponse;
import com.hostel.exception.BadRequestException;
import com.hostel.exception.ResourceNotFoundException;
import com.hostel.model.Room;
import com.hostel.repository.AllocationRepository;
import com.hostel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository rooms;
    private final AllocationRepository allocations;

    public List<RoomResponse> findAll(boolean onlyAvailable) {
        return rooms.findAll().stream()
                .map(this::toResponse)
                .filter(r -> !onlyAvailable || r.available() > 0)
                .toList();
    }

    public RoomResponse get(Long id) {
        return toResponse(find(id));
    }

    public RoomResponse create(RoomRequest r) {
        if (rooms.existsByRoomNumber(r.roomNumber())) {
            throw new BadRequestException("Room number already exists: " + r.roomNumber());
        }
        Room room = new Room();
        apply(room, r);
        return toResponse(rooms.save(room));
    }

    public RoomResponse update(Long id, RoomRequest r) {
        Room room = find(id);
        long occupied = allocations.countByRoomIdAndActiveTrue(id);
        if (r.capacity() < occupied) {
            throw new BadRequestException("Capacity cannot be lower than current occupancy (" + occupied + ")");
        }
        apply(room, r);
        return toResponse(rooms.save(room));
    }

    public void delete(Long id) {
        Room room = find(id);
        if (allocations.existsByRoomIdAndActiveTrue(id)) {
            throw new BadRequestException("Room still has residents");
        }
        rooms.delete(room);
    }

    private Room find(Long id) {
        return rooms.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));
    }

    private RoomResponse toResponse(Room r) {
        return RoomResponse.of(r, allocations.countByRoomIdAndActiveTrue(r.getId()));
    }

    private void apply(Room room, RoomRequest r) {
        room.setRoomNumber(r.roomNumber());
        room.setBlock(r.block());
        room.setFloor(r.floor());
        room.setCapacity(r.capacity());
        room.setType(r.type());
        room.setMonthlyRent(r.monthlyRent());
    }
}
