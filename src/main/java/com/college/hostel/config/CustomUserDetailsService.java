package com.college.hostel.config;

import com.college.hostel.entity.User;
import com.college.hostel.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String cleanIdentifier = identifier != null ? identifier.trim() : "";

        // First attempt by email
        User user = userRepository.findByEmail(cleanIdentifier.toLowerCase())
                // Second attempt by studentId (for student convenience)
                .or(() -> userRepository.findByStudentId(cleanIdentifier))
                .orElseThrow(() -> new UsernameNotFoundException("No user found with identifier: " + cleanIdentifier));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is deactivated. Please contact administration.");
        }

        return new CustomUserDetails(user);
    }
}
