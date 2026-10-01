package com.hostel.dto;

import com.hostel.model.Payment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull Long studentId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String forMonth,
        String method,
        Payment.Status status
) {}
