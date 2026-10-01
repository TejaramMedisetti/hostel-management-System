package com.hostel.dto;

import com.hostel.model.Room;

import java.math.BigDecimal;

public record RoomResponse(Long id, String roomNumber, String block, int floor, int capacity,
                           long occupied, long available, Room.RoomType type, BigDecimal monthlyRent) {
    public static RoomResponse of(Room r, long occupied) {
        return new RoomResponse(r.getId(), r.getRoomNumber(), r.getBlock(), r.getFloor(), r.getCapacity(),
                occupied, Math.max(0, r.getCapacity() - occupied), r.getType(), r.getMonthlyRent());
    }
}
