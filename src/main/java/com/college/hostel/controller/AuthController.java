package com.college.hostel.controller;

import com.college.hostel.dto.StudentRegisterDto;
import com.college.hostel.exception.BusinessException;
import com.college.hostel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String registered,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email/student ID or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! You can now log in with your credentials.");
        }
        return "auth/login";
    }

    @GetMapping("/student-login")
    public String studentLogin(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String registered,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid credentials. Please verify your Student ID/Email and password.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! Please log in.");
        }
        model.addAttribute("loginRole", "student");
        return "auth/student-login";
    }

    @GetMapping("/admin-login")
    public String adminLogin(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid administrator credentials or unauthorized account.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Admin session closed successfully.");
        }
        model.addAttribute("loginRole", "admin");
        return "auth/admin-login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("student")) {
            model.addAttribute("student", new StudentRegisterDto());
        }
        return "auth/student-register";
    }

    @PostMapping("/register")
    public String registerStudent(
            @Valid @ModelAttribute("student") StudentRegisterDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (!dto.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "error.student", "Password and Confirm Password do not match.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/student-register";
        }

        try {
            userService.registerStudent(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in to access your student portal.");
            return "redirect:/auth/student-login";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/student-register";
        }
    }
}
