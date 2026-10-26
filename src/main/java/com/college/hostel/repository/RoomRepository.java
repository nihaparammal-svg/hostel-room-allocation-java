package com.college.hostel.repository;

import com.college.hostel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByBlockIdAndRoomNumber(Long blockId, String roomNumber);

    boolean existsByBlockIdAndRoomNumber(Long blockId, String roomNumber);

    boolean existsByBlockIdAndRoomNumberAndIdNot(Long blockId, String roomNumber, Long id);

    List<Room> findByBlockId(Long blockId);

    List<Room> findByRoomType(String roomType);

    List<Room> findByBlockIdAndRoomType(Long blockId, String roomType);

    @Query("SELECT r FROM Room r WHERE " +
           "(:blockId IS NULL OR r.block.id = :blockId) AND " +
           "(:roomType IS NULL OR :roomType = '' OR r.roomType = :roomType) AND " +
           "(:status IS NULL OR :status = '' OR r.status = :status) " +
           "ORDER BY r.block.blockName ASC, r.floor ASC, r.roomNumber ASC")
    List<Room> searchRooms(
            @Param("blockId") Long blockId,
            @Param("roomType") String roomType,
            @Param("status") String status
    );

    @Query("SELECT COALESCE(SUM(r.capacity), 0) FROM Room r")
    long getTotalBedCapacity();
}
