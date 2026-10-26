package com.college.hostel;

import com.college.hostel.dto.AllocationDto;
import com.college.hostel.dto.ApplicationDto;
import com.college.hostel.dto.ApplicationReviewDto;
import com.college.hostel.dto.RoomDto;
import com.college.hostel.dto.StudentRegisterDto;
import com.college.hostel.entity.Allocation;
import com.college.hostel.entity.Application;
import com.college.hostel.entity.HostelBlock;
import com.college.hostel.entity.Room;
import com.college.hostel.entity.User;
import com.college.hostel.exception.BusinessException;
import com.college.hostel.repository.HostelBlockRepository;
import com.college.hostel.service.AllocationService;
import com.college.hostel.service.ApplicationService;
import com.college.hostel.service.RoomService;
import com.college.hostel.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class RoomAllocationWorkflowTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private HostelBlockRepository blockRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Test 1: Student registration, validation, and BCrypt hashing")
    void testStudentRegistration() {
        StudentRegisterDto dto = new StudentRegisterDto();
        dto.setFullName("Emma Watson");
        dto.setStudentId("STU2026999");
        dto.setEmail("emma.watson@hostel.edu");
        dto.setPhone("+1-555-8888");
        dto.setDepartment("Computer Science");
        dto.setYearOfStudy("1st Year");
        dto.setPassword("SecretPass123");
        dto.setConfirmPassword("SecretPass123");

        User registered = userService.registerStudent(dto);

        assertNotNull(registered.getId());
        assertEquals("ROLE_STUDENT", registered.getRole());
        assertTrue(registered.isActive());
        assertTrue(passwordEncoder.matches("SecretPass123", registered.getPassword()));
        assertNotEquals("SecretPass123", registered.getPassword());

        // Duplicate registration prevention test
        assertThrows(BusinessException.class, () -> userService.registerStudent(dto));
    }

    @Test
    @DisplayName("Test 2: Room creation, capacity, and bed availability calculation")
    void testRoomCreationAndVacancyCalculation() {
        HostelBlock block = blockRepository.findAll().get(0);

        RoomDto roomDto = new RoomDto();
        roomDto.setBlockId(block.getId());
        roomDto.setRoomNumber("TEST-999");
        roomDto.setFloor(3);
        roomDto.setCapacity(2);
        roomDto.setRoomType("Double");
        roomDto.setFacilities("WiFi, Study Table");
        roomDto.setMonthlyFee(BigDecimal.valueOf(5000.00));
        roomDto.setStatus("AVAILABLE");

        Room room = roomService.addRoom(roomDto);

        assertNotNull(room.getId());
        assertEquals(2, room.getCapacity());
        assertEquals(2, room.getAvailableBeds());
        assertEquals(0, room.getActiveAllocationsCount());
        assertTrue(room.isAvailable());

        // Prevent duplicate room in same block
        assertThrows(BusinessException.class, () -> roomService.addRoom(roomDto));
    }

    @Test
    @DisplayName("Test 3: Complete allocation workflow: Apply -> Review -> Allocate -> Overbooking Prevention -> Vacate")
    void testCompleteAllocationWorkflow() {
        // 1. Create a test student
        StudentRegisterDto regDto = new StudentRegisterDto();
        regDto.setFullName("Lucas Scott");
        regDto.setStudentId("STU2026777");
        regDto.setEmail("lucas.scott@hostel.edu");
        regDto.setPhone("+1-555-7777");
        regDto.setDepartment("Electrical Engineering");
        regDto.setYearOfStudy("2nd Year");
        regDto.setPassword("Student@123");
        regDto.setConfirmPassword("Student@123");
        User student1 = userService.registerStudent(regDto);

        // 2. Create single-bed room (Capacity = 1)
        HostelBlock block = blockRepository.findAll().get(0);
        RoomDto singleRoomDto = new RoomDto();
        singleRoomDto.setBlockId(block.getId());
        singleRoomDto.setRoomNumber("TEST-SINGLE-1");
        singleRoomDto.setFloor(1);
        singleRoomDto.setCapacity(1); // Capacity 1 bed
        singleRoomDto.setRoomType("Single");
        singleRoomDto.setMonthlyFee(BigDecimal.valueOf(6000.00));
        singleRoomDto.setStatus("AVAILABLE");
        Room singleRoom = roomService.addRoom(singleRoomDto);

        assertEquals(1, singleRoom.getAvailableBeds());

        // 3. Student submits application
        ApplicationDto appDto = new ApplicationDto();
        appDto.setPreferredBlockId(block.getId());
        appDto.setPreferredRoomType("Single");
        appDto.setSpecialRequests("First floor room requested");

        Application application = applicationService.submitApplication(student1, appDto);
        assertEquals("PENDING", application.getStatus());

        // Check duplicate active application prevention
        assertThrows(BusinessException.class, () -> applicationService.submitApplication(student1, appDto));

        // 4. Admin reviews and approves application
        ApplicationReviewDto reviewDto = new ApplicationReviewDto();
        reviewDto.setApplicationId(application.getId());
        reviewDto.setStatus("APPROVED");
        reviewDto.setAdminRemarks("Eligible for single room allocation.");
        Application approvedApp = applicationService.reviewApplication(reviewDto);
        assertEquals("APPROVED", approvedApp.getStatus());

        // 5. Admin allocates bed to student1
        AllocationDto allocDto = new AllocationDto();
        allocDto.setApplicationId(approvedApp.getId());
        allocDto.setUserId(student1.getId());
        allocDto.setRoomId(singleRoom.getId());
        allocDto.setBedNumber("Bed-1");
        allocDto.setRemarks("First allocation");

        Allocation allocation = allocationService.allocateBed(allocDto);
        assertNotNull(allocation.getId());
        assertEquals("ACTIVE", allocation.getStatus());

        // Verify available bed count dropped to 0
        Room updatedRoom = roomService.getRoomById(singleRoom.getId());
        assertEquals(0, updatedRoom.getAvailableBeds());
        assertEquals(1, updatedRoom.getActiveAllocationsCount());
        assertFalse(updatedRoom.isAvailable());

        // 6. Overbooking prevention: Another student attempts to be allocated to the full room
        StudentRegisterDto regDto2 = new StudentRegisterDto();
        regDto2.setFullName("Brooke Davis");
        regDto2.setStudentId("STU2026888");
        regDto2.setEmail("brooke.davis@hostel.edu");
        regDto2.setPhone("+1-555-8889");
        regDto2.setDepartment("Mechanical Engineering");
        regDto2.setYearOfStudy("2nd Year");
        regDto2.setPassword("Student@123");
        regDto2.setConfirmPassword("Student@123");
        User student2 = userService.registerStudent(regDto2);

        AllocationDto overbookDto = new AllocationDto();
        overbookDto.setUserId(student2.getId());
        overbookDto.setRoomId(singleRoom.getId());
        overbookDto.setBedNumber("Bed-2");

        BusinessException overbookException = assertThrows(BusinessException.class, () -> allocationService.allocateBed(overbookDto));
        assertTrue(overbookException.getMessage().contains("fully booked"));

        // 7. Safe room deletion safeguard test: cannot delete room with active occupants
        assertThrows(BusinessException.class, () -> roomService.deleteRoom(singleRoom.getId()));

        // 8. Vacate bed when student leaves
        allocationService.vacateBed(allocation.getId(), "Student checked out for semester end");
        Allocation vacatedAllocation = allocationService.getAllAllocations().stream()
                .filter(a -> a.getId().equals(allocation.getId()))
                .findFirst().orElseThrow();
        assertEquals("VACATED", vacatedAllocation.getStatus());
        assertNotNull(vacatedAllocation.getVacatedAt());

        // Verify bed vacancy is automatically restored to 1!
        Room vacatedRoom = roomService.getRoomById(singleRoom.getId());
        assertEquals(1, vacatedRoom.getAvailableBeds());
        assertEquals(0, vacatedRoom.getActiveAllocationsCount());
        assertTrue(vacatedRoom.isAvailable());
    }
}
