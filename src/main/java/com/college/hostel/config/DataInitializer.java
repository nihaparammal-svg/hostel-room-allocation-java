package com.college.hostel.config;

import com.college.hostel.entity.*;
import com.college.hostel.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final HostelBlockRepository blockRepository;
    private final RoomRepository roomRepository;
    private final ApplicationRepository applicationRepository;
    private final AllocationRepository allocationRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           HostelBlockRepository blockRepository,
                           RoomRepository roomRepository,
                           ApplicationRepository applicationRepository,
                           AllocationRepository allocationRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.blockRepository = blockRepository;
        this.roomRepository = roomRepository;
        this.applicationRepository = applicationRepository;
        this.allocationRepository = allocationRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized with data. Skipping seed.");
            return;
        }

        log.info("Initializing Hostel Management System sample database...");

        // 1. Seed Admin User
        User admin = new User();
        admin.setFullName("Hostel Chief Warden");
        admin.setStudentId("ADM001");
        admin.setEmail("admin@hostel.edu");
        admin.setPhone("+1-555-0100");
        admin.setDepartment("Hostel Administration");
        admin.setYearOfStudy("Faculty/Staff");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole("ROLE_ADMIN");
        admin.setActive(true);
        userRepository.save(admin);

        // 2. Seed Sample Students
        User student1 = new User();
        student1.setFullName("Alex Morgan");
        student1.setStudentId("STU2024001");
        student1.setEmail("alex.student@hostel.edu");
        student1.setPhone("+1-555-0101");
        student1.setDepartment("Computer Science");
        student1.setYearOfStudy("3rd Year");
        student1.setPassword(passwordEncoder.encode("Student@123"));
        student1.setRole("ROLE_STUDENT");
        student1.setActive(true);
        userRepository.save(student1);

        User student2 = new User();
        student2.setFullName("Sarah Connor");
        student2.setStudentId("STU2024002");
        student2.setEmail("sarah.student@hostel.edu");
        student2.setPhone("+1-555-0102");
        student2.setDepartment("Information Technology");
        student2.setYearOfStudy("2nd Year");
        student2.setPassword(passwordEncoder.encode("Student@123"));
        student2.setRole("ROLE_STUDENT");
        student2.setActive(true);
        userRepository.save(student2);

        // 3. Seed Hostel Blocks
        HostelBlock blockA = new HostelBlock("Block A - Boys Residency", "MALE", "Modern block with high-speed WiFi, recreational room, and reading lounges.", 4);
        HostelBlock blockB = new HostelBlock("Block B - Girls Residency", "FEMALE", "Secure block with 24/7 security, gym facility, solar heating, and study halls.", 4);
        HostelBlock blockC = new HostelBlock("Block C - Post-Graduate Wing", "CO-ED", "Quiet research-oriented residency with en-suite bathrooms and air conditioning.", 3);

        blockRepository.save(blockA);
        blockRepository.save(blockB);
        blockRepository.save(blockC);

        // 4. Seed Rooms
        // Block A Rooms
        Room rA101 = new Room(blockA, "101", 1, 2, "Double", "WiFi, Study Table, Attached Bath, Fan", BigDecimal.valueOf(4500.00));
        Room rA102 = new Room(blockA, "102", 1, 1, "Single", "WiFi, Study Table, AC, Attached Bath, Balcony", BigDecimal.valueOf(7000.00));
        Room rA201 = new Room(blockA, "201", 2, 3, "Triple", "WiFi, Study Tables, Fan, Wardrobe", BigDecimal.valueOf(3500.00));
        Room rA202 = new Room(blockA, "202", 2, 2, "Double", "WiFi, Study Table, Attached Bath", BigDecimal.valueOf(4500.00));

        // Block B Rooms
        Room rB101 = new Room(blockB, "101", 1, 2, "Double", "High-speed WiFi, Reading Table, Geyser, Attached Bath", BigDecimal.valueOf(4800.00));
        Room rB102 = new Room(blockB, "102", 1, 1, "Single", "High-speed WiFi, AC, Reading Desk, Balcony, Attached Bath", BigDecimal.valueOf(7200.00));
        Room rB201 = new Room(blockB, "201", 2, 3, "Triple", "WiFi, Wardrobe, Study Tables, Balcony", BigDecimal.valueOf(3600.00));

        // Block C Rooms
        Room rC101 = new Room(blockC, "101", 1, 1, "Single", "Central AC, High-speed Fiber, Ergonomic Desk, Attached Bath", BigDecimal.valueOf(8500.00));
        Room rC102 = new Room(blockC, "102", 1, 2, "Double", "Central AC, High-speed Fiber, Study Desks, Attached Bath", BigDecimal.valueOf(5500.00));

        roomRepository.save(rA101);
        roomRepository.save(rA102);
        roomRepository.save(rA201);
        roomRepository.save(rA202);
        roomRepository.save(rB101);
        roomRepository.save(rB102);
        roomRepository.save(rB201);
        roomRepository.save(rC101);
        roomRepository.save(rC102);

        // 5. Seed Alex Morgan's Application & Allocation
        Application appAlex = new Application();
        appAlex.setUser(student1);
        appAlex.setPreferredBlock(blockA);
        appAlex.setPreferredRoomType("Double");
        appAlex.setStatus("APPROVED");
        appAlex.setSpecialRequests("Prefer ground or first floor if possible.");
        appAlex.setAdminRemarks("Approved for Block A Double occupancy.");
        appAlex.setAppliedAt(LocalDateTime.now().minusDays(3));
        applicationRepository.save(appAlex);

        Allocation allocAlex = new Allocation();
        allocAlex.setApplication(appAlex);
        allocAlex.setUser(student1);
        allocAlex.setRoom(rA101);
        allocAlex.setBedNumber("Bed-1");
        allocAlex.setAllocatedAt(LocalDateTime.now().minusDays(2));
        allocAlex.setStatus("ACTIVE");
        allocAlex.setRemarks("Allocated on approval.");
        allocationRepository.save(allocAlex);

        Notification notifAlex = new Notification(
                student1,
                "Room Allocation Complete",
                "You have been allocated Bed-1 in Room 101, Block A - Boys Residency. Please collect your keys from the warden office."
        );
        notificationRepository.save(notifAlex);

        // 6. Seed Sarah Connor's Application (Pending)
        Application appSarah = new Application();
        appSarah.setUser(student2);
        appSarah.setPreferredBlock(blockB);
        appSarah.setPreferredRoomType("Double");
        appSarah.setStatus("PENDING");
        appSarah.setSpecialRequests("Quiet study corner requested.");
        appSarah.setAdminRemarks(null);
        appSarah.setAppliedAt(LocalDateTime.now().minusHours(5));
        applicationRepository.save(appSarah);

        Notification notifSarah = new Notification(
                student2,
                "Application Received",
                "Your hostel room application #2 is currently under review by the administration."
        );
        notificationRepository.save(notifSarah);

        log.info("Hostel Management System database successfully initialized with sample data!");
    }
}
