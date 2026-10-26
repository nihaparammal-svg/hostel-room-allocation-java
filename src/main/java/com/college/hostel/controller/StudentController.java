package com.college.hostel.controller;

import com.college.hostel.config.CustomUserDetails;
import com.college.hostel.dto.ApplicationDto;
import com.college.hostel.entity.Allocation;
import com.college.hostel.entity.Application;
import com.college.hostel.entity.Room;
import com.college.hostel.entity.User;
import com.college.hostel.exception.BusinessException;
import com.college.hostel.service.AllocationService;
import com.college.hostel.service.ApplicationService;
import com.college.hostel.service.NotificationService;
import com.college.hostel.service.RoomService;
import com.college.hostel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final RoomService roomService;
    private final ApplicationService applicationService;
    private final AllocationService allocationService;
    private final NotificationService notificationService;
    private final UserService userService;

    public StudentController(RoomService roomService,
                             ApplicationService applicationService,
                             AllocationService allocationService,
                             NotificationService notificationService,
                             UserService userService) {
        this.roomService = roomService;
        this.applicationService = applicationService;
        this.allocationService = allocationService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(CustomUserDetails userDetails) {
        return userService.findById(userDetails.getId());
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = getAuthenticatedUser(userDetails);
        Long userId = student.getId();

        model.addAttribute("student", student);
        model.addAttribute("totalRooms", roomService.getTotalRooms());
        model.addAttribute("availableBeds", roomService.getAvailableBeds());
        model.addAttribute("occupiedBeds", roomService.getOccupiedBeds());

        // Latest application for this specific student (Strict data isolation)
        Application latestApp = applicationService.getLatestStudentApplication(userId);
        model.addAttribute("latestApplication", latestApp);

        // Active allocation for this student (if allocated)
        Optional<Allocation> activeAlloc = allocationService.getActiveStudentAllocation(userId);
        model.addAttribute("activeAllocation", activeAlloc.orElse(null));

        // Notifications
        model.addAttribute("unreadCount", notificationService.getUnreadCount(userId));
        model.addAttribute("notifications", notificationService.getUserNotifications(userId));

        return "student/dashboard";
    }

    @GetMapping("/rooms")
    public String rooms(
            @RequestParam(required = false) Long blockId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        model.addAttribute("student", getAuthenticatedUser(userDetails));
        model.addAttribute("rooms", roomService.searchRooms(blockId, roomType, status));
        model.addAttribute("blocks", roomService.getAllBlocks());
        model.addAttribute("selectedBlockId", blockId);
        model.addAttribute("selectedRoomType", roomType);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("availableBeds", roomService.getAvailableBeds());
        return "student/rooms";
    }

    @GetMapping("/apply")
    public String showApplyForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = getAuthenticatedUser(userDetails);
        Long userId = student.getId();

        // Check if student already has an active room allocation
        Optional<Allocation> activeAlloc = allocationService.getActiveStudentAllocation(userId);
        if (activeAlloc.isPresent()) {
            model.addAttribute("errorMessage", "You already have an active room allocation: Room " +
                    activeAlloc.get().getRoom().getRoomNumber() + " (" + activeAlloc.get().getBedNumber() + ").");
            model.addAttribute("alreadyAllocated", true);
        }

        // Check if student already has a pending or approved application
        Application latestApp = applicationService.getLatestStudentApplication(userId);
        if (latestApp != null && ("PENDING".equals(latestApp.getStatus()) || "APPROVED".equals(latestApp.getStatus()))) {
            model.addAttribute("activeApplication", latestApp);
        }

        model.addAttribute("student", student);
        model.addAttribute("blocks", roomService.getAllBlocks());
        if (!model.containsAttribute("applicationDto")) {
            model.addAttribute("applicationDto", new ApplicationDto());
        }
        return "student/apply";
    }

    @PostMapping("/apply")
    public String submitApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("applicationDto") ApplicationDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        User student = getAuthenticatedUser(userDetails);

        if (bindingResult.hasErrors()) {
            model.addAttribute("student", student);
            model.addAttribute("blocks", roomService.getAllBlocks());
            return "student/apply";
        }

        try {
            applicationService.submitApplication(student, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Hostel application submitted successfully! Your application is now Pending review.");
            return "redirect:/student/my-application";
        } catch (BusinessException ex) {
            model.addAttribute("student", student);
            model.addAttribute("blocks", roomService.getAllBlocks());
            model.addAttribute("errorMessage", ex.getMessage());
            return "student/apply";
        }
    }

    @GetMapping("/my-application")
    public String myApplication(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = getAuthenticatedUser(userDetails);
        Long userId = student.getId();

        model.addAttribute("student", student);
        model.addAttribute("applications", applicationService.getStudentApplications(userId));
        model.addAttribute("latestApplication", applicationService.getLatestStudentApplication(userId));
        model.addAttribute("activeAllocation", allocationService.getActiveStudentAllocation(userId).orElse(null));

        return "student/my-application";
    }

    @GetMapping("/my-room")
    public String myRoom(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = getAuthenticatedUser(userDetails);
        Long userId = student.getId();

        model.addAttribute("student", student);
        Optional<Allocation> activeAlloc = allocationService.getActiveStudentAllocation(userId);
        model.addAttribute("activeAllocation", activeAlloc.orElse(null));
        model.addAttribute("allocationHistory", allocationService.getStudentAllocationHistory(userId));

        if (activeAlloc.isPresent()) {
            Room room = activeAlloc.get().getRoom();
            model.addAttribute("room", room);
            model.addAttribute("roomOccupants", allocationService.getActiveAllocationsForRoom(room.getId()));
        }

        return "student/my-room";
    }

    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = getAuthenticatedUser(userDetails);
        Long userId = student.getId();

        model.addAttribute("student", student);
        model.addAttribute("notifications", notificationService.getUserNotifications(userId));

        // Mark as read when visited
        notificationService.markAllAsRead(userId);

        return "student/notifications";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = getAuthenticatedUser(userDetails);
        model.addAttribute("student", student);
        model.addAttribute("activeAllocation", allocationService.getActiveStudentAllocation(student.getId()).orElse(null));
        model.addAttribute("totalApplications", applicationService.getStudentApplications(student.getId()).size());
        return "student/profile";
    }
}
