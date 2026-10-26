package com.college.hostel.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "rooms",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"block_id", "room_number"})
    }
)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "block_id", nullable = false)
    private HostelBlock block;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Column(name = "floor", nullable = false)
    private int floor;

    @Column(name = "capacity", nullable = false)
    private int capacity; // e.g. 1, 2, 3, 4

    @Column(name = "room_type", nullable = false, length = 50)
    private String roomType; // Single, Double, Triple, Dormitory

    @Column(name = "facilities", length = 255)
    private String facilities; // "WiFi, Attached Bath, Study Table, AC"

    @Column(name = "monthly_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyFee = BigDecimal.valueOf(4500.00);

    @Column(name = "status", nullable = false, length = 30)
    private String status = "AVAILABLE"; // AVAILABLE, FULL, MAINTENANCE

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Allocation> allocations = new ArrayList<>();

    public Room() {
    }

    public Room(HostelBlock block, String roomNumber, int floor, int capacity, String roomType, String facilities, BigDecimal monthlyFee) {
        this.block = block;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.capacity = capacity;
        this.roomType = roomType;
        this.facilities = facilities;
        this.monthlyFee = monthlyFee;
        this.status = "AVAILABLE";
    }

    /**
     * Calculates active allocations dynamically from database records.
     */
    public int getActiveAllocationsCount() {
        if (allocations == null) {
            return 0;
        }
        return (int) allocations.stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .count();
    }

    /**
     * Available beds = Room capacity - Active allocations.
     */
    public int getAvailableBeds() {
        int activeCount = getActiveAllocationsCount();
        return Math.max(0, capacity - activeCount);
    }

    public boolean isAvailable() {
        return getAvailableBeds() > 0 && !"MAINTENANCE".equalsIgnoreCase(status);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HostelBlock getBlock() {
        return block;
    }

    public void setBlock(HostelBlock block) {
        this.block = block;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public BigDecimal getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(BigDecimal monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Allocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<Allocation> allocations) {
        this.allocations = allocations;
    }
}
