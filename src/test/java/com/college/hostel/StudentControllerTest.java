package com.college.hostel;

import com.college.hostel.entity.User;
import com.college.hostel.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Verify /student/apply renders without 500 error for a student with no prior application")
    @WithMockUser(username = "alex.student@hostel.edu", authorities = {"ROLE_STUDENT"})
    void testShowApplyForm() throws Exception {
        mockMvc.perform(get("/student/apply"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/apply"));
    }
}
