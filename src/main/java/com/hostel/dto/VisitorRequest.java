package com.hostel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VisitorRequest(@NotNull Long studentId, @NotBlank String visitorName, String purpose) {}
