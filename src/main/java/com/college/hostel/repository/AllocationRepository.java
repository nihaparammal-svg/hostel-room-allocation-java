package com.college.hostel.repository;

import com.college.hostel.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    List<Allocation> findByUserIdOrderByAllocatedAtDesc(Long userId);

    Optional<Allocation> findByUserIdAndStatus(Long userId, String status);

    List<Allocation> findByRoomIdAndStatus(Long roomId, String status);

    long countByRoomIdAndStatus(Long roomId, String status);

    long countByStatus(String status);

    boolean existsByRoomIdAndBedNumberAndStatus(Long roomId, String bedNumber, String status);

    boolean existsByRoomIdAndStatus(Long roomId, String status);

    boolean existsByUserIdAndStatus(Long userId, String status);

    List<Allocation> findAllByOrderByAllocatedAtDesc();

    @Query("SELECT a FROM Allocation a WHERE " +
           "(:status IS NULL OR :status = '' OR a.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(a.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.user.studentId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.room.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.room.block.blockName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY a.allocatedAt DESC")
    List<Allocation> searchAllocations(
            @Param("status") String status,
            @Param("keyword") String keyword
    );
}
