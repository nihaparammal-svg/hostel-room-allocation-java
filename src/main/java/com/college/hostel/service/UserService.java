package com.college.hostel.service;

import com.college.hostel.dto.StudentRegisterDto;
import com.college.hostel.entity.User;
import com.college.hostel.exception.BusinessException;
import com.college.hostel.exception.ResourceNotFoundException;
import com.college.hostel.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerStudent(StudentRegisterDto dto) {
        if (!dto.isPasswordMatching()) {
            throw new BusinessException("Password and Confirm Password do not match.");
        }

        if (userRepository.existsByEmail(dto.getEmail().trim().toLowerCase())) {
            throw new BusinessException("A user with email '" + dto.getEmail() + "' already exists.");
        }

        if (userRepository.existsByStudentId(dto.getStudentId().trim())) {
            throw new BusinessException("A student with ID '" + dto.getStudentId() + "' already exists.");
        }

        User user = new User();
        user.setFullName(dto.getFullName().trim());
        user.setStudentId(dto.getStudentId().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPhone(dto.getPhone().trim());
        user.setDepartment(dto.getDepartment().trim());
        user.setYearOfStudy(dto.getYearOfStudy().trim());
        // Always hash password with BCrypt
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // Enforce ROLE_STUDENT; public registration can NEVER create an admin account
        user.setRole("ROLE_STUDENT");
        user.setActive(true);

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ResourceNotFoundException("User identifier is required.");
        }
        String clean = identifier.trim();
        return userRepository.findByEmail(clean.toLowerCase())
                .or(() -> userRepository.findByStudentId(clean))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + clean));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public List<User> findAllStudents() {
        return userRepository.findByRoleOrderByCreatedAtDesc("ROLE_STUDENT");
    }

    public List<User> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllStudents();
        }
        return userRepository.searchStudents("ROLE_STUDENT", keyword.trim());
    }

    @Transactional
    public void toggleStudentStatus(Long id) {
        User user = findById(id);
        if ("ROLE_ADMIN".equals(user.getRole())) {
            throw new BusinessException("Cannot deactivate administrator accounts.");
        }
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public long getTotalStudents() {
        return userRepository.countByRole("ROLE_STUDENT");
    }
}
