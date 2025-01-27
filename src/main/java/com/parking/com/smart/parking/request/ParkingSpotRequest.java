package com.parking.com.smart.parking.request;

import com.parking.com.smart.parking.entities.SpotStatus;
import com.parking.com.smart.parking.entities.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParkingSpotRequest {
    private String spotLocation;
    private String vehicleType;
}
