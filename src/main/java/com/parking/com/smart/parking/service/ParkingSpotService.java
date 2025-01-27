package com.parking.com.smart.parking.service;

import com.parking.com.smart.parking.entities.SpotStatus;
import com.parking.com.smart.parking.request.ParkingSpotRequest;
import com.parking.com.smart.parking.response.ApiPaginatedResponse;
import com.parking.com.smart.parking.response.ApiResponse;

public interface ParkingSpotService {
    ApiResponse createParkingSpot(ParkingSpotRequest parkingSpotRequest);
    ApiPaginatedResponse getAvailableSpots(int page, int size, String location);
    ApiResponse updateParkingSpotStatus(Long spotId, SpotStatus newStatus);
    ApiResponse getParkingSpotsLocation();
    ApiPaginatedResponse getAllSpots(int page, int size, String location);
    int countTotalSpots();

}
