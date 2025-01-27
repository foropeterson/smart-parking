package com.parking.com.smart.parking.controller;


import com.parking.com.smart.parking.dtos.UserDTO;
import com.parking.com.smart.parking.entities.Role;
import com.parking.com.smart.parking.entities.User;
import com.parking.com.smart.parking.request.ParkingSpotRequest;
import com.parking.com.smart.parking.response.ApiPaginatedResponse;
import com.parking.com.smart.parking.response.ApiResponse;
import com.parking.com.smart.parking.service.AuditLogService;
import com.parking.com.smart.parking.service.BookingService;
import com.parking.com.smart.parking.service.ParkingSpotService;
import com.parking.com.smart.parking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name="Admin",description = "Admin Management endpoints")
public class AdminController {
    private final UserService userService;
    private final ParkingSpotService parkingSpotService;
    private final BookingService bookingService;
    private final AuditLogService auditLogService;

    public AdminController(UserService userService, ParkingSpotService parkingSpotService, BookingService bookingService, AuditLogService auditLogService) {
        this.userService = userService;
        this.parkingSpotService = parkingSpotService;
        this.bookingService = bookingService;
        this.auditLogService = auditLogService;
    }
    @Operation(summary = "create a parking spot", description = "creating new parking spot")
    @PostMapping("/parking/spots")
    public ApiResponse createParkingSpot(@RequestBody ParkingSpotRequest parkingSpotRequest) {
        return parkingSpotService.createParkingSpot(parkingSpotRequest);
    }
    @Operation(summary ="Get all users", description = "Api to fetch all users")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(),
                HttpStatus.OK);
    }
    @Operation(summary ="Get all Roles", description = "Api to fetch all roles")
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return new ResponseEntity<>(userService.getAllRoles(),
                HttpStatus.OK);
    }
    @Operation(summary ="Update user role", description = "Api to update user roles")
    @PutMapping("/update/roles")
    public ResponseEntity<String> updateUserRole(@RequestParam Integer userId,
                                                 @RequestParam String roleName) {
        userService.updateUserRole(userId, roleName);
        return ResponseEntity.ok("User role updated successfully");
    }
    @Operation(summary ="Get single user", description = "Api to fetch a single user")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Integer id) {
        return new ResponseEntity<>(userService.getUserById(id), HttpStatus.OK);
    }
    @Operation(summary ="count all users", description = "Api to get count of all users")
    @GetMapping("/users/count")
    public int countUsers() {
        return userService.countTotalUsers();
    }
    @Operation(summary ="Get Parking revenue", description = "Api to fetch parking revenue")
    @GetMapping("/parking/revenue")
    public int sumOfRevenue() {
        return bookingService.getSumOfPaidBookingAmounts();
    }
    @Operation(summary ="Get all bookings count", description = "Api to fetch all bookings count")
    @GetMapping("/bookings/count")
    public int countBookings() {
        return bookingService.getTotalBookings();
    }
    @Operation(summary ="Get all parking spots", description = "Api to fetch count of all parking spots")
    @GetMapping("/parking/count")
    public int countParkingSpot() {
        return parkingSpotService.countTotalSpots();
    }
    @Operation(summary ="Get all audit logs", description = "Api to fetch all audit logs")
    @GetMapping("/audit/logs")
    public ApiResponse getAuditLogs() {
        return auditLogService.getAuditLogs();
    }
    @Operation(summary ="Get audit logs  details  ", description = "Api to fetch  audit log details")
    @GetMapping("/audit/logs/{id}")
    public ApiResponse getAuditLogsById(@PathVariable Long id) {
        return auditLogService.getAuditLogsById(id);
    }
}
