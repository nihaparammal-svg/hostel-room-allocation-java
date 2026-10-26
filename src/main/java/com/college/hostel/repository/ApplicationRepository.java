package com.college.hostel.repository;

import com.college.hostel.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByUserIdOrderByAppliedAtDesc(Long userId);

    @Query("SELECT a FROM Application a WHERE a.user.id = :userId AND a.status IN ('PENDING', 'APPROVED')")
    List<Application> findActiveApplicationsByUserId(@Param("userId") Long userId);

    List<Application> findAllByOrderByAppliedAtDesc();

    long countByStatus(String status);

    @Query("SELECT a FROM Application a WHERE " +
           "(:status IS NULL OR :status = '' OR a.status = :status) AND " +
           "(:department IS NULL OR :department = '' OR a.user.department = :department) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(a.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.user.studentId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY a.appliedAt DESC")
    List<Application> searchApplications(
            @Param("status") String status,
            @Param("department") String department,
            @Param("keyword") String keyword
    );
}
