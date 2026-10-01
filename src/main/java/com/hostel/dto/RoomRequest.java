package com.hostel.dto;

import com.hostel.model.Room;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank String roomNumber,
        @NotBlank String block,
        @Min(0) int floor,
        @Min(1) int capacity,
        @NotNull Room.RoomType type,
        @NotNull @Positive BigDecimal monthlyRent
) {}
