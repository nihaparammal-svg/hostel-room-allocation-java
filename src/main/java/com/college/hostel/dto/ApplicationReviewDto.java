package com.college.hostel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ApplicationReviewDto {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotBlank(message = "Action status is required")
    private String status; // APPROVED, REJECTED, WAITLISTED

    private String adminRemarks;

    public ApplicationReviewDto() {
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }
}
