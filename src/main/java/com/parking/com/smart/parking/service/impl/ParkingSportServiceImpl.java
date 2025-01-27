package com.parking.com.smart.parking.service.impl;

import com.parking.com.smart.parking.dtos.ParkingSpotDTO;
import com.parking.com.smart.parking.entities.ParkingSpot;
import com.parking.com.smart.parking.entities.SpotStatus;
import com.parking.com.smart.parking.entities.VehicleType;
import com.parking.com.smart.parking.repository.ParkingSpotRepository;
import com.parking.com.smart.parking.request.ParkingSpotRequest;
import com.parking.com.smart.parking.response.ApiPaginatedResponse;
import com.parking.com.smart.parking.response.ApiResponse;
import com.parking.com.smart.parking.service.ParkingSpotService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ParkingSportServiceImpl implements ParkingSpotService {
    private final ParkingSpotRepository parkingSpotRepository;

    public ParkingSportServiceImpl(ParkingSpotRepository parkingSpotRepository) {
        this.parkingSpotRepository = parkingSpotRepository;
    }

    @Override
    public ApiResponse createParkingSpot(ParkingSpotRequest parkingSpotRequest) {
        VehicleType vehicleType;
        try {
            vehicleType = VehicleType.valueOf(parkingSpotRequest.getVehicleType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return new ApiResponse("400", "Invalid vehicle type provided", null);
        }
        ParkingSpot parkingSpot = new ParkingSpot();
        parkingSpot.setSpotLocation(parkingSpotRequest.getSpotLocation());
        parkingSpot.setVehicleType(vehicleType);
        parkingSpot.setStatus(SpotStatus.AVAILABLE);
        ParkingSpot savedSpot = parkingSpotRepository.save(parkingSpot);

        return new ApiResponse("201", "Parking spot created successfully", savedSpot);
    }

    @Override
    public ApiPaginatedResponse getAvailableSpots(int page, int size, String location) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<ParkingSpot> availableParkingSpotsPage;
        if (location != null && !location.isEmpty()) {
            availableParkingSpotsPage = parkingSpotRepository.findByStatusAndSpotLocation(SpotStatus.AVAILABLE, location, pageable);
        } else {
            availableParkingSpotsPage = parkingSpotRepository.findByStatus(SpotStatus.AVAILABLE, pageable);
        }
        if (availableParkingSpotsPage.isEmpty()) {
            return new ApiPaginatedResponse("404", "No available parking spots", null, 0, 0, 0);
        }
        return new ApiPaginatedResponse(
                "200",
                "Available parking spots retrieved successfully",
                availableParkingSpotsPage.getContent(),
                availableParkingSpotsPage.getTotalElements(),
                availableParkingSpotsPage.getTotalPages(),
                availableParkingSpotsPage.getNumber() + 1
        );
    }


    @Override
    public ApiResponse updateParkingSpotStatus(Long spotId, SpotStatus newStatus) {
        Optional<ParkingSpot> parkingSpotOptional = parkingSpotRepository.findById(spotId);

        if (parkingSpotOptional.isPresent()) {
            ParkingSpot parkingSpot = parkingSpotOptional.get();
            parkingSpot.setStatus(newStatus);
            parkingSpotRepository.save(parkingSpot);

            return new ApiResponse("200", "Parking spot status updated successfully", parkingSpot);
        }

        return new ApiResponse("404", "Parking spot not found", null);
    }

    @Override
    public ApiResponse getParkingSpotsLocation() {
        List<ParkingSpot> parkingSpots = parkingSpotRepository.findAll();

        if (parkingSpots.isEmpty()) {
            return new ApiResponse("404", "No parking spots available", null);
        }

        List<ParkingSpotDTO> availableSpots = parkingSpots.stream()
                .filter(spot -> spot.getStatus() == SpotStatus.AVAILABLE)
                .map(spot -> new ParkingSpotDTO(
                        spot.getSpotId() + " - " + spot.getSpotLocation(),
                        spot.getVehicleType().toString()
                ))
                .collect(Collectors.toList());

        if (availableSpots.isEmpty()) {
            return new ApiResponse("404", "No available parking spots found", null);
        }

        return new ApiResponse("200", "Available parking spots retrieved successfully", availableSpots);
    }

    @Override
    public ApiPaginatedResponse getAllSpots(int page, int size, String location) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<ParkingSpot> availableParkingSpotsPage;
        if (location != null && !location.isEmpty()) {
            availableParkingSpotsPage = parkingSpotRepository.findBySpotLocation(location, pageable);
        } else {
            availableParkingSpotsPage = parkingSpotRepository.findAll(pageable);
        }

        if (availableParkingSpotsPage.isEmpty()) {
            return new ApiPaginatedResponse("404", "No parking spots found", null, 0, 0, 0);
        }
        return new ApiPaginatedResponse(
                "200",
                "parking spots retrieved successfully",
                availableParkingSpotsPage.getContent(),
                availableParkingSpotsPage.getTotalElements(),
                availableParkingSpotsPage.getTotalPages(),
                availableParkingSpotsPage.getNumber() + 1
        );
    }

    @Override
    public int countTotalSpots() {
        return (int) parkingSpotRepository.count();
    }
}
