package com.college.hostel.dto;

import jakarta.validation.constraints.NotNull;

public class ApplicationDto {

    @NotNull(message = "Please select a preferred hostel block")
    private Long preferredBlockId;

    private String preferredRoomType;

    private String specialRequests;

    public ApplicationDto() {
    }

    public Long getPreferredBlockId() {
        return preferredBlockId;
    }

    public void setPreferredBlockId(Long preferredBlockId) {
        this.preferredBlockId = preferredBlockId;
    }

    public String getPreferredRoomType() {
        return preferredRoomType;
    }

    public void setPreferredRoomType(String preferredRoomType) {
        this.preferredRoomType = preferredRoomType;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }
}
