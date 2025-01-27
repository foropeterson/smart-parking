package com.parking.com.smart.parking.controller;

import com.parking.com.smart.parking.entities.SpotStatus;
import com.parking.com.smart.parking.response.ApiPaginatedResponse;
import com.parking.com.smart.parking.response.ApiResponse;
import com.parking.com.smart.parking.service.ParkingSpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parking")
@Tag(name="Parking spots",description = "Apis for managing parking spot")
public class ParkingSpotController {
    private final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }
    @Operation(summary = "Get all Parking spots", description = "Api to get all parking spots")
    @GetMapping("/spots")
    public ApiPaginatedResponse getAllParkingSpots(@RequestParam(value = "page", defaultValue = "1") int page,
                                                   @RequestParam(value = "size", defaultValue = "10") int size,
                                                   @RequestParam(value = "location", required = false) String location) {
        return parkingSpotService.getAllSpots(page, size, location);
    }

    @Operation(summary = "Get Parking spot location", description = "Api to get parking spots locations")
    @GetMapping("/spots/with-location")
    public ApiResponse getAllParkingSpotsWithLocation() {
        return parkingSpotService.getParkingSpotsLocation();
    }

    @Operation(summary = "Get all Parking Available Parking spots", description = "Api to get all Available parking  spots")
    @GetMapping("/spots/available")
    public ApiPaginatedResponse getAvailableSpots(@RequestParam(value = "page", defaultValue = "1") int page,
                                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                                  @RequestParam(value = "location", required = false) String location
    ) {
        return parkingSpotService.getAvailableSpots(page, size, location);
    }
    @Operation(summary = "Filter Parking spots", description = "Api to filter parking spots")
    @PutMapping("/{spotId}/status")
    public ApiResponse updateParkingSpotStatus(
            @PathVariable Long spotId,
            @RequestParam("status") String status) {
        try {
            SpotStatus newStatus = SpotStatus.valueOf(status.toUpperCase());
            return parkingSpotService.updateParkingSpotStatus(spotId, newStatus);
        } catch (IllegalArgumentException e) {
            return new ApiResponse("400", "Invalid status value", null);
        }
    }
}
