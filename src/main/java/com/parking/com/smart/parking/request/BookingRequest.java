package com.parking.com.smart.parking.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    private Integer userId;
    private Long spotId;
    private String vehicleRegistration;
    private String vehicleType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double amount;
    private String paymentStatus;
    public long getDurationHours() {
        return java.time.Duration.between(startTime, endTime).toHours();
    }
}
