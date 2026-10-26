package com.college.hostel.service;

import com.college.hostel.dto.ApplicationDto;
import com.college.hostel.dto.ApplicationReviewDto;
import com.college.hostel.entity.Application;
import com.college.hostel.entity.HostelBlock;
import com.college.hostel.entity.User;
import com.college.hostel.exception.BusinessException;
import com.college.hostel.exception.ResourceNotFoundException;
import com.college.hostel.repository.AllocationRepository;
import com.college.hostel.repository.ApplicationRepository;
import com.college.hostel.repository.HostelBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final HostelBlockRepository blockRepository;
    private final AllocationRepository allocationRepository;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              HostelBlockRepository blockRepository,
                              AllocationRepository allocationRepository,
                              NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.blockRepository = blockRepository;
        this.allocationRepository = allocationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Application submitApplication(User student, ApplicationDto dto) {
        // Rule: Verify student is active
        if (!student.isActive()) {
            throw new BusinessException("Your account is currently inactive. Please contact the hostel administration.");
        }

        // Rule: Check if student already has an active room allocation
        if (allocationRepository.existsByUserIdAndStatus(student.getId(), "ACTIVE")) {
            throw new BusinessException("You already have an active room allocation. Please vacate your current room before submitting a new application.");
        }

        // Rule: Prevent duplicate active applications (PENDING or APPROVED)
        List<Application> activeApps = applicationRepository.findActiveApplicationsByUserId(student.getId());
        if (!activeApps.isEmpty()) {
            throw new BusinessException("You already have an active hostel application with status: " + activeApps.get(0).getStatus() + ".");
        }

        HostelBlock block = null;
        if (dto.getPreferredBlockId() != null) {
            block = blockRepository.findById(dto.getPreferredBlockId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hostel block not found"));
        }

        Application app = new Application();
        app.setUser(student);
        app.setPreferredBlock(block);
        app.setPreferredRoomType(dto.getPreferredRoomType());
        app.setSpecialRequests(dto.getSpecialRequests() != null ? dto.getSpecialRequests().trim() : "");
        app.setStatus("PENDING");

        Application saved = applicationRepository.save(app);

        notificationService.sendNotification(
                student,
                "Application Submitted",
                "Your hostel room application #" + saved.getId() + " has been submitted successfully and is under review."
        );

        return saved;
    }

    public List<Application> getStudentApplications(Long userId) {
        return applicationRepository.findByUserIdOrderByAppliedAtDesc(userId);
    }

    public Application getLatestStudentApplication(Long userId) {
        List<Application> apps = applicationRepository.findByUserIdOrderByAppliedAtDesc(userId);
        return apps.isEmpty() ? null : apps.get(0);
    }

    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAllByOrderByAppliedAtDesc();
    }

    public List<Application> searchApplications(String status, String department, String keyword) {
        return applicationRepository.searchApplications(status, department, keyword);
    }

    @Transactional
    public Application reviewApplication(ApplicationReviewDto dto) {
        Application application = getApplicationById(dto.getApplicationId());

        String newStatus = dto.getStatus().trim().toUpperCase();
        if (!List.of("APPROVED", "REJECTED", "WAITLISTED").contains(newStatus)) {
            throw new BusinessException("Invalid application status: " + newStatus);
        }

        application.setStatus(newStatus);
        application.setAdminRemarks(dto.getAdminRemarks() != null ? dto.getAdminRemarks().trim() : "");

        Application updated = applicationRepository.save(application);

        String message = switch (newStatus) {
            case "APPROVED" -> "Congratulations! Your hostel application #" + updated.getId() + " has been APPROVED. Room allocation will be processed shortly. Remarks: " + (dto.getAdminRemarks() != null ? dto.getAdminRemarks() : "None");
            case "REJECTED" -> "Your hostel application #" + updated.getId() + " was REJECTED. Remarks: " + (dto.getAdminRemarks() != null ? dto.getAdminRemarks() : "No remarks provided.");
            case "WAITLISTED" -> "Your hostel application #" + updated.getId() + " has been WAITLISTED pending bed availability. Remarks: " + (dto.getAdminRemarks() != null ? dto.getAdminRemarks() : "None");
            default -> "Status updated to " + newStatus;
        };

        notificationService.sendNotification(
                updated.getUser(),
                "Application Status Update: " + newStatus,
                message
        );

        return updated;
    }

    public long getPendingCount() {
        return applicationRepository.countByStatus("PENDING");
    }

    public long getApprovedCount() {
        return applicationRepository.countByStatus("APPROVED");
    }
}
