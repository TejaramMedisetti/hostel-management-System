package com.hostel.dto;

import com.hostel.model.Complaint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ComplaintDtos {
    public record CreateComplaint(@NotBlank String title, @NotBlank String description) {}
    public record AdminCreateComplaint(@NotNull Long studentId, @NotBlank String title, @NotBlank String description) {}
    public record UpdateComplaint(@NotNull Complaint.Status status, String resolution) {}
}
