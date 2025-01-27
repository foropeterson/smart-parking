package com.parking.com.smart.parking.controller;

import com.parking.com.smart.parking.request.BookingRequest;
import com.parking.com.smart.parking.response.ApiPaginatedResponse;
import com.parking.com.smart.parking.response.ApiResponse;
import com.parking.com.smart.parking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name="Bookings",description = "Endpoints for managing bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    @Operation(summary = "Book Parking", description = "Api to book a parking spot")
    @PostMapping("/create")
    public ApiResponse createBooking(@RequestBody BookingRequest bookingRequest) {
        return bookingService.createBooking(bookingRequest);
    }
    @Operation(summary = "Exit parking", description = "Api to exit a particular parking spot")
    @PutMapping("/exit/{bookingId}")
    public ApiResponse exitParking(@PathVariable Long bookingId) {
        return bookingService.exitParking(bookingId);
    }

//    @PostMapping("/payments/complete")
//    public ApiResponse completePayment(@RequestParam String paymentRef) {
//        return bookingService.completePayment(paymentRef);
//    }
@Operation(summary = "Get Parking amount", description = "Api to get parking amount")
    @GetMapping("/calculate-amount")
    public ResponseEntity<Double> calculateAmount(
            @RequestParam Integer userId,
            @RequestParam String vehicleType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        double totalAmount = bookingService.calculateTotalAmount(userId, vehicleType, startTime, endTime);
        return ResponseEntity.ok(totalAmount);
    }
    @Operation(summary = "My bookings", description = "Api to get all my bookings")
    @GetMapping("/my-bookings")
    public ApiPaginatedResponse getBookings(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("userId") Integer userId,
            @RequestParam(value = "paymentStatus", required = false) String paymentStatus) {
        return bookingService.getBookings(page, size, userId, paymentStatus);
    }
    @Operation(summary = "Get Booking", description = "Api for getting a booking spot")
    @GetMapping("/{bookingId}")
    public ApiResponse getSingleBooking(@PathVariable("bookingId") Long bookingId) {
        return bookingService.getBookingById(bookingId);
    }
    @Operation(summary = "Get all Bookings", description = "APi to get all bookings")
    @GetMapping("/all")
    public ApiPaginatedResponse getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String paymentStatus) {

        return bookingService.getAllBookings(page, size, paymentStatus);
    }
}

