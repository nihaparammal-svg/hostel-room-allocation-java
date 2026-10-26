package com.college.hostel.controller;

import com.college.hostel.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final RoomService roomService;

    public HomeController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("totalRooms", roomService.getTotalRooms());
        model.addAttribute("totalCapacity", roomService.getTotalBedCapacity());
        model.addAttribute("availableBeds", roomService.getAvailableBeds());
        model.addAttribute("occupiedBeds", roomService.getOccupiedBeds());
        model.addAttribute("blocks", roomService.getAllBlocks());
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/rooms")
    public String publicRooms(
            @RequestParam(required = false) Long blockId,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String status,
            Model model
    ) {
        model.addAttribute("rooms", roomService.searchRooms(blockId, roomType, status));
        model.addAttribute("blocks", roomService.getAllBlocks());
        model.addAttribute("selectedBlockId", blockId);
        model.addAttribute("selectedRoomType", roomType);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("availableBeds", roomService.getAvailableBeds());
        model.addAttribute("totalCapacity", roomService.getTotalBedCapacity());
        return "rooms";
    }
}
