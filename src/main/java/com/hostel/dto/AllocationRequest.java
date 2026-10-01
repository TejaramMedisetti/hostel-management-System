package com.hostel.dto;

import jakarta.validation.constraints.NotNull;

public record AllocationRequest(@NotNull Long studentId, @NotNull Long roomId) {}
