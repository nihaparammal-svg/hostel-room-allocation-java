package com.college.hostel.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hostel_blocks")
public class HostelBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "block_name", nullable = false, unique = true, length = 50)
    private String blockName;

    @Column(name = "gender_type", nullable = false, length = 20)
    private String genderType; // MALE, FEMALE, CO-ED

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_floors", nullable = false)
    private int totalFloors = 4;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms = new ArrayList<>();

    public HostelBlock() {
    }

    public HostelBlock(String blockName, String genderType, String description, int totalFloors) {
        this.blockName = blockName;
        this.genderType = genderType;
        this.description = description;
        this.totalFloors = totalFloors;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getGenderType() {
        return genderType;
    }

    public void setGenderType(String genderType) {
        this.genderType = genderType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(int totalFloors) {
        this.totalFloors = totalFloors;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
