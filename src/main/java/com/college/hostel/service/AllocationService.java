package com.college.hostel.service;

import com.college.hostel.dto.AllocationDto;
import com.college.hostel.entity.Allocation;
import com.college.hostel.entity.Application;
import com.college.hostel.entity.Room;
import com.college.hostel.entity.User;
import com.college.hostel.exception.BusinessException;
import com.college.hostel.exception.ResourceNotFoundException;
import com.college.hostel.repository.AllocationRepository;
import com.college.hostel.repository.ApplicationRepository;
import com.college.hostel.repository.RoomRepository;
import com.college.hostel.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    public AllocationService(AllocationRepository allocationRepository,
                             RoomRepository roomRepository,
                             UserRepository userRepository,
                             ApplicationRepository applicationRepository,
                             NotificationService notificationService) {
        this.allocationRepository = allocationRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Allocates a room bed to an approved student.
     * Uses database transaction with isolation to prevent overbooking.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Allocation allocateBed(AllocationDto dto) {
        User student = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + dto.getUserId()));

        if (!student.isActive()) {
            throw new BusinessException("Cannot allocate room to an inactive student account.");
        }

        // Rule: Check if student already has an active allocation
        if (allocationRepository.existsByUserIdAndStatus(student.getId(), "ACTIVE")) {
            throw new BusinessException("Student " + student.getFullName() + " already has an active room allocation.");
        }

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + dto.getRoomId()));

        if ("MAINTENANCE".equalsIgnoreCase(room.getStatus())) {
            throw new BusinessException("Room " + room.getRoomNumber() + " is currently under maintenance.");
        }

        // Overbooking prevention: calculate current active allocations directly from database
        long activeCount = allocationRepository.countByRoomIdAndStatus(room.getId(), "ACTIVE");
        if (activeCount >= room.getCapacity()) {
            throw new BusinessException("Room " + room.getRoomNumber() + " is fully booked. Capacity is " +
                    room.getCapacity() + " and all beds are currently occupied.");
        }

        String bedNumber = dto.getBedNumber().trim().toUpperCase();
        // Check if bed number is already occupied in this room
        if (allocationRepository.existsByRoomIdAndBedNumberAndStatus(room.getId(), bedNumber, "ACTIVE")) {
            throw new BusinessException("Bed '" + bedNumber + "' in Room " + room.getRoomNumber() + " is already occupied.");
        }

        Application application = null;
        if (dto.getApplicationId() != null) {
            application = applicationRepository.findById(dto.getApplicationId()).orElse(null);
        }

        Allocation allocation = new Allocation();
        allocation.setApplication(application);
        allocation.setUser(student);
        allocation.setRoom(room);
        allocation.setBedNumber(bedNumber);
        allocation.setAllocatedAt(LocalDateTime.now());
        allocation.setStatus("ACTIVE");
        allocation.setRemarks(dto.getRemarks() != null ? dto.getRemarks().trim() : "Allocated by Admin");

        Allocation saved = allocationRepository.save(allocation);

        if (room.getAllocations() != null) {
            room.getAllocations().add(saved);
        }

        // Update room status if full
        if (activeCount + 1 >= room.getCapacity()) {
            room.setStatus("FULL");
        }
        roomRepository.save(room);

        // Send student notification
        notificationService.sendNotification(
                student,
                "Room Allocated: " + room.getBlock().getBlockName() + " Room " + room.getRoomNumber(),
                "Congratulations! You have been allocated " + bedNumber + " in Room " +
                        room.getRoomNumber() + " (" + room.getBlock().getBlockName() + ", Floor " +
                        room.getFloor() + "). Monthly Fee: $" + room.getMonthlyFee() + "."
        );

        return saved;
    }

    /**
     * Vacates a bed when a student leaves or checks out.
     * Sets allocation to VACATED and immediately restores bed availability.
     */
    @Transactional
    public void vacateBed(Long allocationId, String remarks) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation record not found with id: " + allocationId));

        if (!"ACTIVE".equalsIgnoreCase(allocation.getStatus())) {
            throw new BusinessException("This allocation is already vacated.");
        }

        allocation.setStatus("VACATED");
        allocation.setVacatedAt(LocalDateTime.now());
        if (remarks != null && !remarks.trim().isEmpty()) {
            allocation.setRemarks((allocation.getRemarks() != null ? allocation.getRemarks() + " | " : "") + "Vacated: " + remarks.trim());
        }

        allocationRepository.save(allocation);

        // Re-open room status to AVAILABLE if it was FULL
        Room room = allocation.getRoom();
        if (room.getAllocations() != null) {
            for (Allocation a : room.getAllocations()) {
                if (a.getId() != null && a.getId().equals(allocation.getId())) {
                    a.setStatus("VACATED");
                    a.setVacatedAt(allocation.getVacatedAt());
                }
            }
        }
        if ("FULL".equalsIgnoreCase(room.getStatus())) {
            room.setStatus("AVAILABLE");
        }
        roomRepository.save(room);

        notificationService.sendNotification(
                allocation.getUser(),
                "Room Vacated",
                "Your allocation for Room " + room.getRoomNumber() + " (" + allocation.getBedNumber() + ") has been vacated."
        );
    }

    public Optional<Allocation> getActiveStudentAllocation(Long userId) {
        return allocationRepository.findByUserIdAndStatus(userId, "ACTIVE");
    }

    public List<Allocation> getStudentAllocationHistory(Long userId) {
        return allocationRepository.findByUserIdOrderByAllocatedAtDesc(userId);
    }

    public List<Allocation> getAllAllocations() {
        return allocationRepository.findAllByOrderByAllocatedAtDesc();
    }

    public List<Allocation> searchAllocations(String status, String keyword) {
        return allocationRepository.searchAllocations(status, keyword);
    }

    public List<Allocation> getActiveAllocationsForRoom(Long roomId) {
        return allocationRepository.findByRoomIdAndStatus(roomId, "ACTIVE");
    }
}
