package com.college.hostel.controller;

import com.college.hostel.dto.AllocationDto;
import com.college.hostel.dto.ApplicationReviewDto;
import com.college.hostel.dto.RoomDto;
import com.college.hostel.entity.Application;
import com.college.hostel.entity.Room;
import com.college.hostel.entity.User;
import com.college.hostel.exception.BusinessException;
import com.college.hostel.service.AllocationService;
import com.college.hostel.service.ApplicationService;
import com.college.hostel.service.RoomService;
import com.college.hostel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final RoomService roomService;
    private final ApplicationService applicationService;
    private final AllocationService allocationService;
    private final UserService userService;

    public AdminController(RoomService roomService,
                           ApplicationService applicationService,
                           AllocationService allocationService,
                           UserService userService) {
        this.roomService = roomService;
        this.applicationService = applicationService;
        this.allocationService = allocationService;
        this.userService = userService;
    }

    // ==========================================
    // 1. Admin Dashboard Overview
    // ==========================================

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalStudents", userService.getTotalStudents());
        model.addAttribute("totalRooms", roomService.getTotalRooms());
        model.addAttribute("totalCapacity", roomService.getTotalBedCapacity());
        model.addAttribute("availableBeds", roomService.getAvailableBeds());
        model.addAttribute("occupiedBeds", roomService.getOccupiedBeds());
        model.addAttribute("pendingApplications", applicationService.getPendingCount());
        model.addAttribute("approvedApplications", applicationService.getApprovedCount());
        model.addAttribute("recentApplications", applicationService.getAllApplications().stream().limit(5).toList());
        model.addAttribute("recentAllocations", allocationService.getAllAllocations().stream().limit(5).toList());
        return "admin/dashboard";
    }

    // ==========================================
    // 2. Room Management
    // ==========================================

    @GetMapping("/rooms")
    public String listRooms(
            @RequestParam(required = false) Long blockId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String status,
            Model model
    ) {
        model.addAttribute("rooms", roomService.searchRooms(blockId, roomType, status));
        model.addAttribute("blocks", roomService.getAllBlocks());
        model.addAttribute("selectedBlockId", blockId);
        model.addAttribute("selectedRoomType", roomType);
        model.addAttribute("selectedStatus", status);
        return "admin/rooms";
    }

    @GetMapping("/rooms/add")
    public String showAddRoomForm(Model model) {
        if (!model.containsAttribute("roomDto")) {
            model.addAttribute("roomDto", new RoomDto());
        }
        model.addAttribute("blocks", roomService.getAllBlocks());
        model.addAttribute("isEdit", false);
        return "admin/room-form";
    }

    @PostMapping("/rooms/add")
    public String addRoom(
            @Valid @ModelAttribute("roomDto") RoomDto roomDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("blocks", roomService.getAllBlocks());
            model.addAttribute("isEdit", false);
            return "admin/room-form";
        }

        try {
            roomService.addRoom(roomDto);
            redirectAttributes.addFlashAttribute("successMessage", "Room " + roomDto.getRoomNumber() + " created successfully.");
            return "redirect:/admin/rooms";
        } catch (BusinessException ex) {
            model.addAttribute("blocks", roomService.getAllBlocks());
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/room-form";
        }
    }

    @GetMapping("/rooms/edit/{id}")
    public String showEditRoomForm(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomById(id);
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setBlockId(room.getBlock().getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setFloor(room.getFloor());
        dto.setCapacity(room.getCapacity());
        dto.setRoomType(room.getRoomType());
        dto.setFacilities(room.getFacilities());
        dto.setMonthlyFee(room.getMonthlyFee());
        dto.setStatus(room.getStatus());

        model.addAttribute("roomDto", dto);
        model.addAttribute("blocks", roomService.getAllBlocks());
        model.addAttribute("isEdit", true);
        model.addAttribute("roomId", id);
        return "admin/room-form";
    }

    @PostMapping("/rooms/edit/{id}")
    public String updateRoom(
            @PathVariable Long id,
            @Valid @ModelAttribute("roomDto") RoomDto roomDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("blocks", roomService.getAllBlocks());
            model.addAttribute("isEdit", true);
            model.addAttribute("roomId", id);
            return "admin/room-form";
        }

        try {
            roomService.updateRoom(id, roomDto);
            redirectAttributes.addFlashAttribute("successMessage", "Room " + roomDto.getRoomNumber() + " updated successfully.");
            return "redirect:/admin/rooms";
        } catch (BusinessException ex) {
            model.addAttribute("blocks", roomService.getAllBlocks());
            model.addAttribute("isEdit", true);
            model.addAttribute("roomId", id);
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/room-form";
        }
    }

    @PostMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/rooms";
    }

    // ==========================================
    // 3. Application Management
    // ==========================================

    @GetMapping("/applications")
    public String listApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        model.addAttribute("applications", applicationService.searchApplications(status, department, keyword));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("searchKeyword", keyword);
        return "admin/applications";
    }

    @PostMapping("/applications/review")
    public String reviewApplication(
            @Valid @ModelAttribute ApplicationReviewDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid review parameters submitted.");
            return "redirect:/admin/applications";
        }

        try {
            Application app = applicationService.reviewApplication(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Application #" + app.getId() + " status updated to " + app.getStatus() + ".");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/applications";
    }

    // ==========================================
    // 4. Room Allocation Management
    // ==========================================

    @GetMapping("/allocations")
    public String listAllocations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long preselectAppId,
            Model model
    ) {
        model.addAttribute("allocations", allocationService.searchAllocations(status, keyword));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchKeyword", keyword);

        // Pre-requisites for allocation modal/form:
        // Approved applications ready for allocation
        List<Application> approvedApps = applicationService.getAllApplications().stream()
                .filter(a -> "APPROVED".equalsIgnoreCase(a.getStatus()))
                .filter(a -> allocationService.getActiveStudentAllocation(a.getUser().getId()).isEmpty())
                .toList();
        model.addAttribute("approvedApplications", approvedApps);

        // Available rooms
        List<Room> availableRooms = roomService.getAllRooms().stream()
                .filter(Room::isAvailable)
                .toList();
        model.addAttribute("availableRooms", availableRooms);

        if (!model.containsAttribute("allocationDto")) {
            AllocationDto allocDto = new AllocationDto();
            if (preselectAppId != null) {
                try {
                    Application app = applicationService.getApplicationById(preselectAppId);
                    allocDto.setApplicationId(app.getId());
                    allocDto.setUserId(app.getUser().getId());
                } catch (Exception ignored) {}
            }
            model.addAttribute("allocationDto", allocDto);
        }

        return "admin/allocations";
    }

    @PostMapping("/allocations/allocate")
    public String allocateBed(
            @Valid @ModelAttribute("allocationDto") AllocationDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "All required fields must be filled to allocate a bed.");
            return "redirect:/admin/allocations";
        }

        try {
            allocationService.allocateBed(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Bed allocated successfully! Student and room records updated.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/allocations";
    }

    @PostMapping("/allocations/vacate/{id}")
    public String vacateBed(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Vacated by Administrator") String remarks,
            RedirectAttributes redirectAttributes
    ) {
        try {
            allocationService.vacateBed(id, remarks);
            redirectAttributes.addFlashAttribute("successMessage", "Bed vacated successfully. Room availability recalculated.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/allocations";
    }

    // ==========================================
    // 5. Student Management
    // ==========================================

    @GetMapping("/students")
    public String listStudents(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("students", userService.searchStudents(keyword));
        model.addAttribute("searchKeyword", keyword);
        return "admin/students";
    }

    @GetMapping("/students/{id}/history")
    public String studentHistory(@PathVariable Long id, Model model) {
        User student = userService.findById(id);
        model.addAttribute("student", student);
        model.addAttribute("applications", applicationService.getStudentApplications(id));
        model.addAttribute("allocations", allocationService.getStudentAllocationHistory(id));
        model.addAttribute("activeAllocation", allocationService.getActiveStudentAllocation(id).orElse(null));
        return "admin/student-history";
    }

    @PostMapping("/students/{id}/toggle-status")
    public String toggleStudentStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleStudentStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student account status updated.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/students";
    }
}
