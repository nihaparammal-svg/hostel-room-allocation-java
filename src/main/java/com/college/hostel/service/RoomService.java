package com.college.hostel.service;

import com.college.hostel.dto.RoomDto;
import com.college.hostel.entity.HostelBlock;
import com.college.hostel.entity.Room;
import com.college.hostel.exception.BusinessException;
import com.college.hostel.exception.ResourceNotFoundException;
import com.college.hostel.repository.AllocationRepository;
import com.college.hostel.repository.HostelBlockRepository;
import com.college.hostel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final HostelBlockRepository blockRepository;
    private final AllocationRepository allocationRepository;

    public RoomService(RoomRepository roomRepository,
                       HostelBlockRepository blockRepository,
                       AllocationRepository allocationRepository) {
        this.roomRepository = roomRepository;
        this.blockRepository = blockRepository;
        this.allocationRepository = allocationRepository;
    }

    public List<HostelBlock> getAllBlocks() {
        return blockRepository.findAll();
    }

    public HostelBlock getBlockById(Long id) {
        return blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel block not found with id: " + id));
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    public List<Room> searchRooms(Long blockId, String roomType, String status) {
        return roomRepository.searchRooms(blockId, roomType, status);
    }

    @Transactional
    public Room addRoom(RoomDto dto) {
        HostelBlock block = getBlockById(dto.getBlockId());

        String trimmedRoomNumber = dto.getRoomNumber().trim().toUpperCase();
        if (roomRepository.existsByBlockIdAndRoomNumber(block.getId(), trimmedRoomNumber)) {
            throw new BusinessException("Room number " + trimmedRoomNumber + " already exists in " + block.getBlockName() + ".");
        }

        Room room = new Room();
        room.setBlock(block);
        room.setRoomNumber(trimmedRoomNumber);
        room.setFloor(dto.getFloor());
        room.setCapacity(dto.getCapacity());
        room.setRoomType(dto.getRoomType());
        room.setFacilities(dto.getFacilities() != null ? dto.getFacilities().trim() : "");
        room.setMonthlyFee(dto.getMonthlyFee());
        room.setStatus(dto.getStatus() != null ? dto.getStatus() : "AVAILABLE");

        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoom(Long roomId, RoomDto dto) {
        Room room = getRoomById(roomId);
        HostelBlock block = getBlockById(dto.getBlockId());

        String trimmedRoomNumber = dto.getRoomNumber().trim().toUpperCase();
        if (roomRepository.existsByBlockIdAndRoomNumberAndIdNot(block.getId(), trimmedRoomNumber, roomId)) {
            throw new BusinessException("Room number " + trimmedRoomNumber + " already exists in " + block.getBlockName() + ".");
        }

        long activeAllocations = allocationRepository.countByRoomIdAndStatus(roomId, "ACTIVE");
        if (dto.getCapacity() < activeAllocations) {
            throw new BusinessException("Capacity cannot be reduced below the current number of active occupants (" + activeAllocations + ").");
        }

        room.setBlock(block);
        room.setRoomNumber(trimmedRoomNumber);
        room.setFloor(dto.getFloor());
        room.setCapacity(dto.getCapacity());
        room.setRoomType(dto.getRoomType());
        room.setFacilities(dto.getFacilities() != null ? dto.getFacilities().trim() : "");
        room.setMonthlyFee(dto.getMonthlyFee());
        room.setStatus(dto.getStatus() != null ? dto.getStatus() : "AVAILABLE");

        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = getRoomById(roomId);

        // Strict rule: Delete rooms only when no active allocations exist
        boolean hasActiveAllocations = allocationRepository.existsByRoomIdAndStatus(roomId, "ACTIVE");
        if (hasActiveAllocations) {
            throw new BusinessException("Cannot delete Room " + room.getRoomNumber() + " because it currently has active student allocations. Vacate all beds first.");
        }

        roomRepository.delete(room);
    }

    public long getTotalRooms() {
        return roomRepository.count();
    }

    public long getTotalBedCapacity() {
        return roomRepository.getTotalBedCapacity();
    }

    public long getOccupiedBeds() {
        return allocationRepository.countByStatus("ACTIVE");
    }

    public long getAvailableBeds() {
        long totalCapacity = getTotalBedCapacity();
        long occupied = getOccupiedBeds();
        return Math.max(0, totalCapacity - occupied);
    }
}
