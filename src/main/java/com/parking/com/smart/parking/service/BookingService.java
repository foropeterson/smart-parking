package com.parking.com.smart.parking.service;

import com.parking.com.smart.parking.entities.PaymentStatus;
import com.parking.com.smart.parking.request.BookingRequest;
import com.parking.com.smart.parking.response.ApiPaginatedResponse;
import com.parking.com.smart.parking.response.ApiResponse;

import java.time.LocalDateTime;

public interface BookingService {
    ApiResponse createBooking(BookingRequest bookingRequest);

    ApiResponse exitParking(Long bookingId);

//    ApiResponse completePayment(String paymentRef);

    double calculateTotalAmount(Integer userId, String vehicleType, LocalDateTime startTime, LocalDateTime endTime);

    ApiPaginatedResponse getBookings(int page, int size, Integer userId, String paymentStatus);

    ApiResponse getBookingById(Long bookingId);

    ApiPaginatedResponse getAllBookings(int page, int size, String paymentStatus);
    int getSumOfPaidBookingAmounts();

    int getTotalBookings();

    }
