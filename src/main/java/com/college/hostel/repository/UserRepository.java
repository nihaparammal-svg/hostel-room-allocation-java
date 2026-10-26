package com.college.hostel.repository;

import com.college.hostel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByStudentId(String studentId);

    boolean existsByEmail(String email);

    boolean existsByStudentId(String studentId);

    List<User> findByRoleOrderByCreatedAtDesc(String role);

    long countByRole(String role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND (" +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.studentId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.department) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
           "ORDER BY u.createdAt DESC")
    List<User> searchStudents(@Param("role") String role, @Param("keyword") String keyword);
}
