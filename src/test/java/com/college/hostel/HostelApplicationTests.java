package com.college.hostel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class HostelApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring application context boots and entities/repositories are configured
    }
}
