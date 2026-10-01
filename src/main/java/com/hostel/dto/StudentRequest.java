package com.hostel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StudentRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        String phone,
        String course,
        Integer academicYear,
        String guardianName,
        String guardianPhone,
        String address,
        String password   // initial login password (only used when creating)
) {}
